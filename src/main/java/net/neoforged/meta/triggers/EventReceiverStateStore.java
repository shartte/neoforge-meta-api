package net.neoforged.meta.triggers;

import net.neoforged.meta.db.EventReceiverState;
import net.neoforged.meta.db.EventReceiverStateDao;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is a facade for the trigger system to remember state for each configured event receiver.
 */
@Component
public class EventReceiverStateStore {
    private final EventReceiverStateDao dao;

    public EventReceiverStateStore(EventReceiverStateDao dao) {
        this.dao = dao;
    }

    @Transactional(readOnly = true, propagation = Propagation.NEVER)
    public Map<String, EventReceiverState> getAll() {
        return dao.findAll()
                .stream()
                .collect(Collectors.toMap(
                        EventReceiverState::getId,
                        Function.identity()
                ));
    }

    @Transactional(propagation = Propagation.NEVER)
    public EventReceiverState create(String eventReceiverId) {
        var state = new EventReceiverState();
        state.setId(eventReceiverId);
        dao.saveAndFlush(state);
        return state;
    }

    @Transactional(propagation = Propagation.NEVER)
    public void recordSuccess(String eventReceiverId, long highestEventIdSeen, long eventsDelivered) {
        var state = dao.findById(eventReceiverId).orElse(null);
        if (state == null) {
            state = create(eventReceiverId);
        }
        state.setHighestEventIdSeen(highestEventIdSeen);
        state.setEventsDelivered(state.getEventsDelivered() + eventsDelivered);
        state.setDeliveryState(EventReceiverState.DeliveryState.OK);
        state.setLastSuccess(Instant.now());
        // Reset error state
        state.setFailingSince(null);
        state.setConsecutiveFailures(0);
        state.setLastFailureMessage(null);
        dao.saveAndFlush(state);
    }

    @Transactional(propagation = Propagation.NEVER)
    public void recordFailure(String eventReceiverId, Throwable error) {
        var state = dao.findById(eventReceiverId).orElse(null);
        if (state == null) {
            state = create(eventReceiverId);
        }
        var writer = new StringWriter();
        error.printStackTrace(new PrintWriter(writer));
        state.setDeliveryState(EventReceiverState.DeliveryState.FAILING);
        state.setLastFailureMessage(writer.toString());
        state.setLastFailure(Instant.now());
        state.setConsecutiveFailures(state.getConsecutiveFailures() + 1);
        dao.saveAndFlush(state);
    }

    @Transactional(propagation = Propagation.NEVER)
    public void pause(String eventReceiverId, @Nullable Instant pausedUntil) {
        var state = dao.findById(eventReceiverId).orElse(null);
        if (state == null) {
            state = create(eventReceiverId);
        }
        if (!state.isPaused() || !Objects.equals(state.getPausedUntil(), pausedUntil)) {
            state.setPaused(true);
            state.setPausedUntil(pausedUntil);
            dao.saveAndFlush(state);
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    public void resume(String eventReceiverId) {
        var state = dao.findById(eventReceiverId).orElse(null);
        if (state != null && state.isPaused()) {
            state.setPaused(false);
            state.setPausedUntil(null);
            dao.saveAndFlush(state);
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.NEVER)
    @Nullable
    public EventReceiverState getById(String eventReceiverId) {
        return dao.findById(eventReceiverId).orElse(null);
    }
}
