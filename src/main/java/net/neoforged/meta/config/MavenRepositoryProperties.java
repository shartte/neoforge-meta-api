package net.neoforged.meta.config;

import jakarta.validation.constraints.NotNull;
import net.neoforged.meta.db.SoftwareComponentArtifact;
import org.jspecify.annotations.Nullable;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Validated
public class MavenRepositoryProperties {
    @NotNull
    private String id;
    @NotNull
    private URI url;
    @NotNull
    private Map<String, String> headers = new HashMap<>();
    @Nullable
    private URI externalUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public @Nullable URI getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(@Nullable URI externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getDownloadUrl(SoftwareComponentArtifact artifact) {
        var repositoryUrl = Objects.requireNonNullElse(externalUrl, url).toString();
        if (!repositoryUrl.endsWith("/")) {
            repositoryUrl += "/";
        }
        return repositoryUrl + artifact.getRelativePath();
    }
}
