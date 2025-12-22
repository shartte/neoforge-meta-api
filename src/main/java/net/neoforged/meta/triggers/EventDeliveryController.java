package net.neoforged.meta.triggers;

import jakarta.persistence.criteria.Path;
import net.neoforged.meta.db.event.Event;
import net.neoforged.meta.db.event.EventDao;
import net.neoforged.meta.db.event.ModifiedComponentVersionEvent;
import net.neoforged.meta.db.event.NewComponentVersionEvent;
import net.neoforged.meta.db.event.SoftwareComponentVersionEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EventDeliveryController {
    private static final Logger LOG = LoggerFactory.getLogger(EventDeliveryController.class);
    private final Map<String, EventReceiver> receivers;
    private final Map<String, Future<?>> runningDeliveries = new HashMap<>();
    private final EventReceiverStateStore stateStore;
    private final EventDao eventDao;

    public EventDeliveryController(EventReceivers receivers, EventReceiverStateStore stateStore, EventDao eventDao) {
        this.receivers = receivers.receivers().stream().collect(Collectors.toMap(EventReceiver::getId, Function.identity()));
        this.stateStore = stateStore;
        this.eventDao = eventDao;
    }

    public void runIteration() {
        runIteration(false);
    }

    public void runIteration(boolean waitForCompletion) {
        var savedStates = stateStore.getAll();

        // Get the highest assigned event id to determine the deltas per receiver. Null if no events exist at all.
        var highestEventId = eventDao.getHighestEventId();
        if (highestEventId != null) {
            LOG.debug("Highest known event id is {}", highestEventId);
        }

        for (var receiver : receivers.values()) {
            var previousTask = runningDeliveries.get(receiver.getId());
            if (previousTask != null) {
                // Skip this event receiver if still running
                if (!previousTask.isDone()) {
                    LOG.debug("Skipping event receiver {} since it's still processing work from a previous iteration.", receiver.getId());
                    continue;
                }
                runningDeliveries.remove(receiver.getId());
            }

            var state = savedStates.computeIfAbsent(receiver.getId(), stateStore::create);

            // Ignore receivers that are paused
            if (state.isPaused()) {
                // Unpause the receiver if its pause time has elapsed
                if (state.getPausedUntil() != null && state.getPausedUntil().isBefore(Instant.now())) {
                    LOG.info("Unpausing receiver {} after its pause time ({}) has elapsed", receiver.getId(), state.getPausedUntil());
                    stateStore.resume(receiver.getId());
                } else {
                    LOG.trace("Skipping paused receiver {}", receiver.getId());
                    continue;
                }
            }

            Long lastEventIdSeen = state.getHighestEventIdSeen();
            if (highestEventId != null && (lastEventIdSeen == null || lastEventIdSeen < highestEventId)) {
                // Schedule a task to update this receiver
                LOG.debug("Scheduling task to update event receiver {} from event {} to {}", receiver.getId(), state.getHighestEventIdSeen(), highestEventId);
                var future = CompletableFuture.supplyAsync(() -> updateReceiver(receiver, lastEventIdSeen, highestEventId), getExecutor(receiver))
                        .whenComplete((result, throwable) -> afterReceiverUpdated(receiver, result, throwable));
                runningDeliveries.put(receiver.getId(), future);
            }
        }

        if (waitForCompletion) {
            RuntimeException overallError = null;
            for (var entry : runningDeliveries.entrySet()) {
                try {
                    entry.getValue().get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException e) {
                    var wrapped = new RuntimeException("Update of event receiver " + entry.getKey() + " failed.", e);
                    if (overallError == null) {
                        overallError = wrapped;
                    } else {
                        overallError.addSuppressed(wrapped);
                    }
                }
            }
            if (overallError != null) {
                throw overallError;
            }
        }
    }

    record UpdateResult(long highestEventId, long eventsDelivered) {
    }

    private UpdateResult updateReceiver(EventReceiver receiver, Long lastSeenEventId, long highestEventId) {
        var page = Pageable.unpaged();

        if (receiver.getMaxBatchSize() != null) {
            page = Pageable.ofSize(receiver.getMaxBatchSize());
        }

        LOG.info("Querying events with ids ({},{}] for event receiver {}.", lastSeenEventId != null ? lastSeenEventId : "", highestEventId, receiver.getId());
        var eventsPage = eventDao.findAll(Specification.where(eventIdRange(lastSeenEventId, highestEventId))
                .and(receiver.getEventFilter()), page);

        var events = eventsPage.getContent();

        if (events.isEmpty()) {
            LOG.debug("Found no matching events.");
            return new UpdateResult(highestEventId, 0);
        } else if (receiver.getMaxBatchSize() != null && eventsPage.hasNext()) {
            // If we have a maximum batch size that limited the DB query, and the query reached its maximum size,
            // it is not assured that we've actually seen the highest event id overall. That is only guaranteed
            // if we did *not* reach the query limit.
            highestEventId = events.getLast().getId();
        }

        if (receiver.isCompactEvents()) {
            events = compactEvents(events);
        }

        LOG.debug("Found {} events.", events.size());
        receiver.getDeliveryStrategy().sendEvents(events);
        return new UpdateResult(highestEventId, events.size());
    }

    private static PredicateSpecification<Event> eventIdRange(Long lowerBoundExclusive, long upperBound) {
        return (from, criteriaBuilder) -> {
            Path<Long> idAttr = from.get("id");
            if (lowerBoundExclusive == null) {
                return criteriaBuilder.lessThanOrEqualTo(idAttr, upperBound);
            } else {
                return criteriaBuilder.between(idAttr, lowerBoundExclusive + 1, upperBound);
            }
        };
    }

    /**
     * Event compaction will remove superfluous events from the list, if they match the same group/artifact/version.
     * This only applies to events related to software components.
     * <p>
     * Scans events from beginning to end. For each version, we track the last event index.
     * When we see a delete after a create, we keep both events and reset the state,
     * so subsequent events (representing recreation) start fresh with normal compaction.
     */
    private List<Event> compactEvents(List<Event> events) {
        Map<String, Integer> lastEventIdxByVersion = new HashMap<>();

        int compacted = 0;
        for (int i = 0; i < events.size(); i++) {
            var event = events.get(i);
            if (event instanceof SoftwareComponentVersionEvent componentEvent) {
                var key = componentEvent.getGroupId() + ":" + componentEvent.getArtifactId() + ":" + componentEvent.getVersion();

                var lastEventIdx = lastEventIdxByVersion.get(key);

                if (lastEventIdx != null) {
                    var lastEvent = events.get(lastEventIdx);

                    // Normal compaction: remove previous event, keep current
                    if (compacted++ == 0) {
                        events = new ArrayList<>(events); // Make list mutable
                    }

                    // If we encounter create followed by modify, we just keep the create.
                    if (lastEvent instanceof NewComponentVersionEvent && event instanceof ModifiedComponentVersionEvent) {
                        events.set(i, null);
                        continue;
                    } else {
                        events.set(lastEventIdx, null);
                    }
                }

                lastEventIdxByVersion.put(key, i);
            }
        }

        if (compacted > 0) {
            LOG.debug("Removed {} events through compaction", compacted);
            events.removeIf(Objects::isNull);
        }

        return events;
    }

    private void afterReceiverUpdated(EventReceiver receiver, @Nullable UpdateResult result, @Nullable Throwable throwable) {
        if (result != null && throwable == null) {
            stateStore.recordSuccess(receiver.getId(), result.highestEventId, result.eventsDelivered);
        } else {
            if (throwable == null) {
                throwable = new NullPointerException("No result produced, but no error either.");
            }
            LOG.error("Update of event receiver {} has failed.", receiver.getId(), throwable);
            stateStore.recordFailure(receiver.getId(), throwable);
        }
    }

    private static Executor getExecutor(EventReceiver receiver) {
        return command -> Thread.ofVirtual()
                .name("eventreceiver-" + receiver.getId())
                .start(command);
    }
}
