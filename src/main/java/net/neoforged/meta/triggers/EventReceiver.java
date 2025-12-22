package net.neoforged.meta.triggers;

import jakarta.persistence.EntityManager;
import net.neoforged.meta.db.event.Event;
import net.neoforged.meta.triggers.delivery.EventDeliveryStrategy;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.PredicateSpecification;

public class EventReceiver {
    private final String id;
    private final boolean compactEvents;
    private final Integer maxBatchSize;
    private final PredicateSpecification<Event> eventFilter;
    private final EventDeliveryStrategy deliveryStrategy;

    public EventReceiver(String id,
                         boolean compactEvents,
                         Integer maxBatchSize,
                         PredicateSpecification<Event> eventFilter,
                         EventDeliveryStrategy deliveryStrategy) {
        this.id = id;
        this.compactEvents = compactEvents;
        this.maxBatchSize = maxBatchSize == null ? deliveryStrategy.getMaxBatchSize() : maxBatchSize;
        this.eventFilter = eventFilter;
        this.deliveryStrategy = deliveryStrategy;
    }

    public final String getId() {
        return id;
    }

    public boolean isCompactEvents() {
        return compactEvents;
    }

    public PredicateSpecification<Event> getEventFilter() {
        return eventFilter;
    }

    public EventDeliveryStrategy getDeliveryStrategy() {
        return deliveryStrategy;
    }

    @Nullable
    public Integer getMaxBatchSize() {
        return maxBatchSize;
    }
}
