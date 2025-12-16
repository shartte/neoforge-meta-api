package net.neoforged.meta.triggers;

import net.neoforged.meta.config.trigger.GitHubWorkflowTriggerProperties;
import net.neoforged.meta.db.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitHubWorkflowEventReceiver extends EventReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubWorkflowEventReceiver.class);
    private final GitHubWorkflowTriggerProperties properties;
    private final RestClient restClient;
    private final PayloadFactory payloadFactory;

    public GitHubWorkflowEventReceiver(String id, GitHubWorkflowTriggerProperties properties, PayloadFactory payloadFactory) {
        super(id, properties);
        this.payloadFactory = payloadFactory;
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getApiBaseUrl().toString())
                .defaultHeader("Authorization", "Bearer " + properties.getToken())
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();

        // Test ahead of time, that inputs only use valid placeholders
        for (String value : properties.getInputs().values()) {
            payloadFactory.interpolatePayloads(value, List.of());
        }
    }

    @Override
    public void sendEvents(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        LOG.info("Triggering GitHub workflow for {} events", events.size());

        // Build the request body
        Map<String, String> inputs = new HashMap<>();
        for (var entry : properties.getInputs().entrySet()) {
            inputs.put(entry.getKey(), payloadFactory.interpolatePayloads(entry.getValue(), events));
        }
        var requestBody = Map.of(
                "ref", properties.getRef(),
                "inputs", inputs
        );

        // Make the API call
        restClient.post()
                .uri("/repos/{owner}/{repo}/actions/workflows/{workflowId}/dispatches", properties.getOwner(), properties.getRepository(), properties.getWorkflowId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();

        LOG.info("Successfully triggered GitHub workflow {}/{} (workflow: {})",
                properties.getOwner(), properties.getRepository(), properties.getWorkflowId());
    }

    @Override
    public Integer getMaxBatchSize() {
        // GitHub batch size is limited by the maximum size of the inputs payload (~65kb).
        return properties.getMaxBatchSize() != null ? Math.min(100, properties.getMaxBatchSize()) : 100;
    }
}
