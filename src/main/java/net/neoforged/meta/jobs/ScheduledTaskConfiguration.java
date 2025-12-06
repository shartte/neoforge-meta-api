package net.neoforged.meta.jobs;

import net.neoforged.meta.MetaApiApplication;
import net.neoforged.meta.config.ScheduledTasksProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ScheduledTasksProperties.class)
@Profile("!" + MetaApiApplication.CLI_PROFILE)
public class ScheduledTaskConfiguration implements SchedulingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskConfiguration.class);

    private final ScheduledTasksProperties properties;
    private final MinecraftVersionDiscoveryJob minecraftVersionDiscoveryJob;
    private final MavenVersionDiscoveryJob mavenVersionDiscoveryJob;

    public ScheduledTaskConfiguration(ScheduledTasksProperties properties,
                                      MinecraftVersionDiscoveryJob minecraftVersionDiscoveryJob,
                                      MavenVersionDiscoveryJob mavenVersionDiscoveryJob) {
        this.properties = properties;
        this.minecraftVersionDiscoveryJob = minecraftVersionDiscoveryJob;
        this.mavenVersionDiscoveryJob = mavenVersionDiscoveryJob;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (properties.minecraftVersionDiscovery().enabled()) {
            String cronExpression = properties.minecraftVersionDiscovery().cronPattern();
            logger.info("Registering Minecraft metadata polling job with cron expression: {}", cronExpression);

            taskRegistrar.addTriggerTask(minecraftVersionDiscoveryJob, new CronTrigger(cronExpression));
        } else {
            logger.info("Minecraft metadata polling job is disabled");
        }

        if (properties.mavenVersionDiscovery().enabled()) {
            String cronExpression = properties.mavenVersionDiscovery().cronPattern();
            logger.info("Registering Maven version discovery job with cron expression: {}", cronExpression);

            taskRegistrar.addTriggerTask(mavenVersionDiscoveryJob, new CronTrigger(cronExpression));
        } else {
            logger.info("Maven version discovery job is disabled");
        }
    }
}
