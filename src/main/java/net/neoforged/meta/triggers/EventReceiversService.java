package net.neoforged.meta.triggers;

import net.neoforged.meta.db.EventReceiverState;
import net.neoforged.meta.db.event.EventDao;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventReceiversService {
    private static final Logger LOG = LoggerFactory.getLogger(EventReceiversService.class);

    private final List<EventReceiver> receivers;
    private final EventDeliveryController controller;
    private final EventReceiverStateStore stateStore;
    private final EventDao eventDao;

    public EventReceiversService(EventReceivers receivers,
                                 EventDeliveryController controller,
                                 EventReceiverStateStore stateStore, EventDao eventDao) {
        this.receivers = receivers.receivers();
        this.controller = controller;
        this.stateStore = stateStore;
        this.eventDao = eventDao;
    }

    public @Nullable EventReceiver getReceiver(String receiverId) {
        return receivers.stream().filter(r -> r.getId().equals(receiverId)).findFirst().orElse(null);
    }

    public @Nullable EventReceiverState getState(String receiverId) {
        assertReceiverIdValid(receiverId);
        return stateStore.getById(receiverId);
    }

    /**
     * {@return summary of event receivers and their status. Intended for UI}
     */
    public List<EventReceiverSummary> getSummary() {
        var highestEvent = eventDao.getHighestEventId();
        var states = stateStore.getAll();
        var result = new ArrayList<EventReceiverSummary>(receivers.size());

        for (var receiver : receivers) {
            var state = states.get(receiver.getId());
            long backlog = 0;
            if (highestEvent != null && state != null && state.getHighestEventIdSeen() != null) {
                backlog = highestEvent - state.getHighestEventIdSeen();
            }
            result.add(new EventReceiverSummary(
                    receiver,
                    state,
                    backlog
            ));
        }

        return result;
    }

    public void pauseReceiver(String receiverId, @Nullable Instant until) {
        assertReceiverIdValid(receiverId);

        if (until != null) {
            LOG.info("Pausing receiver {} until {}", receiverId, until);
        } else {
            LOG.info("Pausing receiver {}", receiverId);
        }
        stateStore.pause(receiverId, until);
    }

    public void resumeReceiver(String receiverId) {
        assertReceiverIdValid(receiverId);

        LOG.info("Resuming receiver {}", receiverId);
        stateStore.resume(receiverId);
    }

    private void assertReceiverIdValid(String receiverId) {
        if (receivers.stream().noneMatch(r -> r.getId().equals(receiverId))) {
            throw new IllegalArgumentException("Invalid receiver id " + receiverId);
        }
    }

    public record EventReceiverSummary(
            EventReceiver receiver,
            EventReceiverState state,
            long eventBacklog
    ) {
    }
}
