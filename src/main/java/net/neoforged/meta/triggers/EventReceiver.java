package net.neoforged.meta.triggers;

import net.neoforged.meta.config.trigger.CommonEventReceiverProperties;
import net.neoforged.meta.db.event.Event;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class EventReceiver {
    private final String id;
    private final CommonEventReceiverProperties properties;

    public EventReceiver(String id, CommonEventReceiverProperties properties) {
        this.id = id;
        this.properties = properties;
    }

    public final String getId() {
        return id;
    }

    public final CommonEventReceiverProperties getProperties() {
        return properties;
    }

    @Nullable
    public Integer getMaxBatchSize() {
        return properties.getMaxBatchSize();
    }

    public abstract void sendEvents(List<Event> events);
}
