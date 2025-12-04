package net.neoforged.meta.jobs;

import net.neoforged.meta.MetaApiApplication;
import net.neoforged.meta.config.MetaApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Configuration
@EnableScheduling
@Profile("!" + MetaApiApplication.CLI_PROFILE)
public class ScheduledTaskConfiguration implements SchedulingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskConfiguration.class);

    private final MetaApiProperties properties;
    private final MinecraftMetadataPollingJob minecraftMetadataPollingJob;

    public ScheduledTaskConfiguration(MetaApiProperties properties, MinecraftMetadataPollingJob minecraftMetadataPollingJob) {
        this.properties = properties;
        this.minecraftMetadataPollingJob = minecraftMetadataPollingJob;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (properties.getMinecraftMetadataPolling().isEnabled()) {
            String cronExpression = properties.getMinecraftMetadataPolling().getCron();
            logger.info("Registering Minecraft metadata polling job with cron expression: {}", cronExpression);

            taskRegistrar.addTriggerTask(minecraftMetadataPollingJob, new CronTrigger(cronExpression));
        } else {
            logger.info("Minecraft metadata polling job is disabled");
        }
    }
}
