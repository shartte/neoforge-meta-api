package net.neoforged.meta.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.jspecify.annotations.Nullable;

@Entity
public class EventReceiverState {
    @Id
    private String id;

    private Long lastEventIdSeen;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public Long getLastEventIdSeen() {
        return lastEventIdSeen;
    }

    public void setLastEventIdSeen(@Nullable Long lastEventDelivered) {
        this.lastEventIdSeen = lastEventDelivered;
    }
}
