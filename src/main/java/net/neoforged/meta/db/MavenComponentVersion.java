package net.neoforged.meta.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Represents a version of a Maven artifact (e.g., net.neoforged:neoforge:21.0.1).
 */
@Entity
@Table(
        indexes = {
                @Index(name = "idx_maven_group_artifact", columnList = "groupId, artifactId"),
                @Index(name = "idx_maven_gav", columnList = "groupId, artifactId, version", unique = true)
        }
)
public class MavenComponentVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Maven group ID (e.g., "net.neoforged")
     */
    @Column(nullable = false)
    private String groupId;

    /**
     * Maven artifact ID (e.g., "neoforge")
     */
    @Column(nullable = false)
    private String artifactId;

    /**
     * Version string (e.g., "21.0.1", "21.0.2-beta")
     */
    @Column(nullable = false)
    private String version;

    /**
     * Whether this is a snapshot version
     */
    @Column(nullable = false)
    private boolean snapshot;

    /**
     * Repository where this version was found (e.g., "releases", "snapshots")
     */
    @Column(nullable = false)
    private String repository;

    /**
     * When this version was released. This can be hard to determine for Maven artifacts.
     * We HEAD the POM to detect when it was created.
     */
    @Column(nullable = false)
    private Instant released;

    /**
     * When this version was first discovered
     */
    @Column(nullable = false)
    private Instant discovered = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    public void setSnapshot(boolean snapshot) {
        this.snapshot = snapshot;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public Instant getReleased() {
        return released;
    }

    public void setReleased(Instant released) {
        this.released = released;
    }

    public Instant getDiscovered() {
        return discovered;
    }

    public void setDiscovered(Instant discovered) {
        this.discovered = discovered;
    }
}
