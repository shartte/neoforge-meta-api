package net.neoforged.meta.db;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Entity
public class MinecraftVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String version;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Instant released;

    /**
     * Whether this version was added automatically (i.e. by the job to sync with the launcher manifest).
     */
    @Column(nullable = false)
    private boolean imported;

    /**
     * The Java version declared by this Minecraft version in its launcher manifest.
     */
    @Column(nullable = false)
    private int javaVersion;

    @OneToOne(mappedBy = "minecraftVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private @Nullable MinecraftVersionManifest manifest;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getReleased() {
        return released;
    }

    public void setReleased(Instant released) {
        this.released = released;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public @Nullable MinecraftVersionManifest getManifest() {
        return manifest;
    }

    public void setManifest(@Nullable MinecraftVersionManifest manifest) {
        this.manifest = manifest;
        if (manifest != null) {
            manifest.setMinecraftVersion(this);
        }
    }

    public int getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(int javaVersion) {
        this.javaVersion = javaVersion;
    }
}
