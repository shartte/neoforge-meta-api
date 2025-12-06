package net.neoforged.meta.config;

import jakarta.validation.constraints.NotNull;

public record ScheduledTaskProperties(boolean enabled, @NotNull String cronPattern) {
}
