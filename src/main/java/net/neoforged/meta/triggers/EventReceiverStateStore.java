package net.neoforged.meta.triggers;

import net.neoforged.meta.db.EventReceiverState;
import net.neoforged.meta.db.EventReceiverStateDao;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public EventReceiverState save(String eventReceiverId, @Nullable Long highestEventReceived) {
        var state = dao.findById(eventReceiverId).orElse(null);
        if (state == null) {
            state = new EventReceiverState();
            state.setId(eventReceiverId);
        }
        state.setLastEventIdSeen(highestEventReceived);
        dao.saveAndFlush(state);
        return state;
    }

    @Transactional(readOnly = true, propagation = Propagation.NEVER)
    @Nullable
    public EventReceiverState getById(String eventReceiverId) {
        return dao.findById(eventReceiverId).orElse(null);
    }
}
