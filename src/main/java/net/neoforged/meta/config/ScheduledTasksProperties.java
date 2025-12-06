package net.neoforged.meta.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("scheduled-tasks")
@Validated
public record ScheduledTasksProperties(@NotNull @Valid ScheduledTaskProperties minecraftVersionDiscovery,
                                       @NotNull @Valid ScheduledTaskProperties mavenVersionDiscovery) {
}
