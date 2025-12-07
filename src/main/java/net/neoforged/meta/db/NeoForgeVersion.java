package net.neoforged.meta.db;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "neoforge_version")
public class NeoForgeVersion extends SoftwareComponentVersion {
    @ManyToOne
    @Fetch(FetchMode.JOIN)
    private MinecraftVersion minecraftVersion;

    @Column(nullable = false)
    private String launcherProfileId;

    /**
     * The vanilla launcher profile stored directly in the database.
     * Automatically compressed using deflate.
     */
    @Column(nullable = false, columnDefinition = "BLOB")
    @Convert(converter = DeflateConverter.class)
    private String launcherProfile;

    /**
     * The legacyinstaller profile content stored directly in the database.
     * Automatically compressed using deflate.
     */
    @Column(nullable = false, columnDefinition = "BLOB")
    @Convert(converter = DeflateConverter.class)
    private String installerProfile;

    public MinecraftVersion getMinecraftVersion() {
        return minecraftVersion;
    }

    public void setMinecraftVersion(MinecraftVersion minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public String getLauncherProfileId() {
        return launcherProfileId;
    }

    public void setLauncherProfileId(String launcherProfileId) {
        this.launcherProfileId = launcherProfileId;
    }

    public String getLauncherProfile() {
        return launcherProfile;
    }

    public void setLauncherProfile(String launcherProfile) {
        this.launcherProfile = launcherProfile;
    }

    public String getInstallerProfile() {
        return installerProfile;
    }

    public void setInstallerProfile(String installerProfile) {
        this.installerProfile = installerProfile;
    }
}
