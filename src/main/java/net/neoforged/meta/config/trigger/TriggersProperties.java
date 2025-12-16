package net.neoforged.meta.config.trigger;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "triggers")
public class TriggersProperties {
    /**
     * Re-Check for work every 30 seconds by default.
     * This is independent of being woken up explicitly.
     */
    private Duration checkWorkInterval = Duration.ofSeconds(30);

    /**
     * GitHub workflows that should be triggered on specific conditions.
     */
    @NotNull
    @Valid
    private Map<String, GitHubWorkflowTriggerProperties> githubWorkflows = new HashMap<>();

    public Duration getCheckWorkInterval() {
        return checkWorkInterval;
    }

    public void setCheckWorkInterval(Duration checkWorkInterval) {
        this.checkWorkInterval = checkWorkInterval;
    }

    public Map<String, GitHubWorkflowTriggerProperties> getGithubWorkflows() {
        return githubWorkflows;
    }

    public void setGithubWorkflows(Map<String, GitHubWorkflowTriggerProperties> githubWorkflows) {
        this.githubWorkflows = githubWorkflows;
    }

    @AssertTrue(message = "All trigger ids must be unique across types.")
    public boolean assertAllKeysUnique() {
        return true; // Currently only one type exists
    }
}
