package net.neoforged.meta.triggers.delivery;

import net.neoforged.meta.db.event.Event;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The strategy for delivering a batch of events to a receiver.
 */
public interface EventDeliveryStrategy {
    void sendEvents(List<Event> events);

    /**
     * {@return null indicates no limit imposed by the delivery strategy, otherwise it limits how many events can be in one batch}
     */
    @Nullable
    default Integer getMaxBatchSize() {
        return null;
    }
}
