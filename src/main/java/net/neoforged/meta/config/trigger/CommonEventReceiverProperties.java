package net.neoforged.meta.config.trigger;

import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class CommonEventReceiverProperties {
    /**
     * Software components, this trigger is notified of.
     * If empty, and no other filters are set, it is notified for all.
     * Note that changes to Minecraft versions are notified using the "virtual" component "net.minecraft:minecraft".
     * The group id and artifact id are separated using {@code :}, and {@code *} can be used as a wildcard for both.
     */
    @Pattern(regexp = "(?:\\*|\\w+):(?:\\*|\\w+)")
    private Set<String> components = new HashSet<>();

    /**
     * Include the necessary filters for NeoForge versions in {@link #components}.
     */
    private boolean neoforgeVersions;

    /**
     * Include the necessary filters for Minecraft versions in {@link #components}.
     */
    private boolean minecraftVersions;

    private boolean compactEvents = true;

    /**
     * The maximum number of events to batch together in one request for this receiver.
     */
    @Nullable
    private Integer maxBatchSize = null;

    public Set<String> getComponents() {
        return components;
    }

    public void setComponents(Set<String> components) {
        this.components = components;
    }

    public boolean isCompactEvents() {
        return compactEvents;
    }

    public void setCompactEvents(boolean compactEvents) {
        this.compactEvents = compactEvents;
    }

    public @Nullable Integer getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(@Nullable Integer maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public boolean isNeoforgeVersions() {
        return neoforgeVersions;
    }

    public void setNeoforgeVersions(boolean neoforgeVersions) {
        this.neoforgeVersions = neoforgeVersions;
    }

    public boolean isMinecraftVersions() {
        return minecraftVersions;
    }

    public void setMinecraftVersions(boolean minecraftVersions) {
        this.minecraftVersions = minecraftVersions;
    }
}
