package net.neoforged.meta.triggers;

import net.neoforged.meta.config.trigger.TriggersProperties;
import net.neoforged.meta.db.event.EventDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
@EnableConfigurationProperties(TriggersProperties.class)
public class TriggerConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(TriggerConfiguration.class);

    @Bean
    EventDeliveryController eventDeliveryController(TriggersProperties properties,
                                                    EventReceiverStateStore stateStore,
                                                    EventDao eventDao,
                                                    EventReceiverFactory factory) {
        LOG.info("{} event receivers are configured", properties.getGithubWorkflows().size());

        var receivers = new ArrayList<EventReceiver>();
        for (var entry : properties.getGithubWorkflows().entrySet()) {
            receivers.add(factory.createGitHubWorkflow(entry.getKey(), entry.getValue()));
        }

        return new EventDeliveryController(receivers, stateStore, eventDao);
    }

    @Bean
    ControllerLoop triggerControllerLoop(TriggersProperties properties, EventDeliveryController eventDeliveryController) {
        return new ControllerLoop(properties.getCheckWorkInterval(), eventDeliveryController::runIteration);
    }

    @Bean
    public TriggerService triggerService(ControllerLoop triggerControllerLoop) {
        return new TriggerService(triggerControllerLoop);
    }

}
