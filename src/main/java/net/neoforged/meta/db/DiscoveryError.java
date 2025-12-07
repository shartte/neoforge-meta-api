package net.neoforged.meta.db;

import jakarta.persistence.Embeddable;

/**
 * Describes a specific error that occurred during version discovery.
 */
@Embeddable
public class DiscoveryError {
    private String details;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
