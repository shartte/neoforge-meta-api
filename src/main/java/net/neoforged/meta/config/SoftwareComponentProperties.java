package net.neoforged.meta.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SoftwareComponentProperties {
    @NotEmpty
    private String groupId;

    @NotEmpty
    private String artifactId;

    @Nullable
    private String mavenRepositoryId;

    @NotNull
    @Valid
    private List<SoftwareComponentPublicationPropertiesRule> publicationRules = new ArrayList<>();

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public @Nullable String getMavenRepositoryId() {
        return mavenRepositoryId;
    }

    public void setMavenRepositoryId(@Nullable String mavenRepositoryId) {
        this.mavenRepositoryId = mavenRepositoryId;
    }

    public List<SoftwareComponentPublicationPropertiesRule> getPublicationRules() {
        return publicationRules;
    }

    public void setPublicationRules(List<SoftwareComponentPublicationPropertiesRule> publicationRules) {
        this.publicationRules = publicationRules;
    }
}
