package net.neoforged.meta.config.trigger;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * See https://docs.github.com/en/rest/actions/workflows?apiVersion=2022-11-28#create-a-workflow-dispatch-event
 */
@Validated
public class GitHubWorkflowTriggerProperties extends CommonEventReceiverProperties {
    /**
     * Base URL of the GitHub REST API.
     */
    @NotNull
    private URI apiBaseUrl = URI.create("https://api.github.com");

    /**
     * Token of a GitHub actor that can trigger the workflow.
     */
    @NotEmpty
    private String token;

    /**
     * Repository owner (i.e. organization)
     */
    @NotEmpty
    private String owner;

    /**
     * Name of the repository containing the workflow.
     */
    @NotEmpty
    private String repository;

    /**
     * The workflow to trigger, it must have a workflow_dispatch event.
     * The workflow filename is also a valid workflow id.
     */
    @NotEmpty
    private String workflowId;

    /**
     * Branch or ref to trigger the workflow on.
     */
    @NotEmpty
    private String ref;

    @NotEmpty
    @Size(max = 25)
    private Map<String, String> inputs = new HashMap<>();

    public URI getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(URI apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, String> inputs) {
        this.inputs = inputs;
    }
}
