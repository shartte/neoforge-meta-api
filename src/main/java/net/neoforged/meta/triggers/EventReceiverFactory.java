package net.neoforged.meta.triggers;

import net.neoforged.meta.config.trigger.GitHubWorkflowTriggerProperties;
import org.springframework.stereotype.Component;

@Component
public class EventReceiverFactory {
    private final PayloadFactory payloadFactory;

    public EventReceiverFactory(PayloadFactory payloadFactory) {
        this.payloadFactory = payloadFactory;
    }

    public EventReceiver createGitHubWorkflow(String id, GitHubWorkflowTriggerProperties properties) {
        return new GitHubWorkflowEventReceiver(id, properties, payloadFactory);
    }
}
