package net.neoforged.meta.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("meta-api")
@Validated
public class MetaApiProperties {
    @NotNull
    private File dataDirectory;

    @NotNull
    private URI minecraftLauncherMetaUrl;

    @NotNull
    @Valid
    private List<MavenRepositoryProperties> mavenRepositories = new ArrayList<>();

    @NotNull
    @Valid
    private List<SoftwareComponentProperties> components = new ArrayList<>();

    public File getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public URI getMinecraftLauncherMetaUrl() {
        return minecraftLauncherMetaUrl;
    }

    public void setMinecraftLauncherMetaUrl(URI minecraftLauncherMetaUrl) {
        this.minecraftLauncherMetaUrl = minecraftLauncherMetaUrl;
    }

    public List<MavenRepositoryProperties> getMavenRepositories() {
        return mavenRepositories;
    }

    public void setMavenRepositories(List<MavenRepositoryProperties> mavenRepositories) {
        this.mavenRepositories = mavenRepositories;
    }

    public List<SoftwareComponentProperties> getComponents() {
        return components;
    }

    public void setComponents(List<SoftwareComponentProperties> components) {
        this.components = components;
    }

    public static class MavenDiscoveryProperties {
        private boolean enabled = true;
        private String cron = "0 */10 * * * *"; // Every 10 minutes by default
        private URI baseUrl = URI.create("https://maven.neoforged.net");

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public URI getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(URI baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

}
