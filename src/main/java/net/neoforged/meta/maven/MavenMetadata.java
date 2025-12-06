package net.neoforged.meta.maven;

import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Collections;
import java.util.List;

/**
 * Model for Maven's maven-metadata.xml file.
 */
@JsonRootName("metadata")
public class MavenMetadata {
    @JacksonXmlProperty(localName = "groupId")
    private String groupId;

    @JacksonXmlProperty(localName = "artifactId")
    private String artifactId;

    @JacksonXmlProperty(localName = "versioning")
    private Versioning versioning;

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

    public Versioning getVersioning() {
        return versioning;
    }

    public void setVersioning(Versioning versioning) {
        this.versioning = versioning;
    }

    public static class Versioning {
        @JacksonXmlProperty(localName = "latest")
        private String latest;

        @JacksonXmlProperty(localName = "release")
        private String release;

        @JacksonXmlProperty(localName = "version")
        @JacksonXmlElementWrapper(localName = "versions")
        private List<String> versions = Collections.emptyList();

        @JacksonXmlProperty(localName = "lastUpdated")
        private String lastUpdated;

        public String getLatest() {
            return latest;
        }

        public void setLatest(String latest) {
            this.latest = latest;
        }

        public String getRelease() {
            return release;
        }

        public void setRelease(String release) {
            this.release = release;
        }

        public List<String> getVersions() {
            return versions;
        }

        public void setVersions(List<String> versions) {
            this.versions = versions;
        }

        public String getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    }
}
