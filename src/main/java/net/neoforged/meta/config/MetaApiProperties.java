package net.neoforged.meta.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.net.URI;

@ConfigurationProperties("meta-api")
@Validated
public class MetaApiProperties {
    @NotNull
    private File dataDirectory;

    private PollingJobProperties minecraftMetadataPolling = new PollingJobProperties();

    @NotNull
    private URI minecraftLauncherMetaUrl;

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

    public PollingJobProperties getMinecraftMetadataPolling() {
        return minecraftMetadataPolling;
    }

    public void setMinecraftMetadataPolling(PollingJobProperties minecraftMetadataPolling) {
        this.minecraftMetadataPolling = minecraftMetadataPolling;
    }

    public static class PollingJobProperties {
        private boolean enabled = true;
        private String cron = "0 */5 * * * *"; // Every 5 minutes by default

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
    }
}
