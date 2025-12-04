package net.neoforged.meta.jobs;

import io.micrometer.core.instrument.MeterRegistry;
import net.neoforged.meta.db.MinecraftVersion;
import net.neoforged.meta.db.MinecraftVersionDao;
import net.neoforged.meta.db.MinecraftVersionManifest;
import net.neoforged.meta.manifests.launcher.LauncherManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Component
public class MinecraftMetadataPollingJob implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MinecraftMetadataPollingJob.class);

    private static final String MINECRAFT_VERSION_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    private final RestClient restClient;

    private final MinecraftVersionDao minecraftVersionDao;

    public MinecraftMetadataPollingJob(MinecraftVersionDao minecraftVersionDao, MeterRegistry meterRegistry) {
        this.minecraftVersionDao = minecraftVersionDao;
        this.restClient = RestClient.builder()
                .baseUrl(MINECRAFT_VERSION_MANIFEST)
                .build();
    }

    @Override
    @Transactional
    public void run() {
        try {
            logger.info("Starting Minecraft metadata polling job");

            var launcherManifest = restClient.get()
                    .retrieve()
                    .body(LauncherManifest.class);

            var existingVersions = minecraftVersionDao.getAllVersions();

            logger.info("Discovered {} versions. {} are already known.", launcherManifest.versions().size(), existingVersions.size());
            var versionsAdded = 0;
            var versionsChanged = 0;

            for (var discoveredVersion : launcherManifest.versions()) {
                logger.trace("Working on version {}", discoveredVersion.id());
                var existingVersion = minecraftVersionDao.getByVersion(discoveredVersion.id());
                if (existingVersion != null) {
                    // Check if manifest changed
                    var existingManifest = existingVersion.getManifest();
                    boolean manifestChanged = existingManifest == null ||
                            !existingManifest.getSha1().equals(discoveredVersion.sha1());

                    if (manifestChanged) {
                        updateVersion(discoveredVersion, existingVersion);
                        versionsChanged++;
                    }
                } else {
                    existingVersion = new MinecraftVersion();
                    existingVersion.setVersion(discoveredVersion.id());
                    existingVersion.setImported(true);
                    updateVersion(discoveredVersion, existingVersion);
                    minecraftVersionDao.save(existingVersion);
                    versionsAdded++;
                }
            }

            logger.info("Completed Minecraft metadata polling job. Versions added: {}, changed: {}", versionsAdded, versionsChanged);
        } catch (Exception e) {
            logger.error("Error polling Minecraft metadata", e);
        }
    }

    private void updateVersion(LauncherManifest.Version discoveredVersion, MinecraftVersion version) {
        version.setType(discoveredVersion.type());
        version.setReleased(discoveredVersion.releaseTime().toInstant());

        // Fetch the manifest content from the URL
        String manifestContent;
        try {
            manifestContent = RestClient.create()
                    .get()
                    .uri(discoveredVersion.url())
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            logger.error("Failed to fetch manifest for version {}", discoveredVersion.id(), e);
            return;
        }

        // Create or update the manifest
        var manifest = version.getManifest();
        boolean isNew = manifest == null;
        if (isNew) {
            manifest = new MinecraftVersionManifest();
            version.setManifest(manifest);
        }

        // Only update lastModified if content actually changed
        boolean contentChanged = isNew || !manifestContent.equals(manifest.getContent());

        manifest.setImported(true);
        manifest.setSha1(discoveredVersion.sha1());
        manifest.setSourceUrl(discoveredVersion.url().toString());
        manifest.setContent(manifestContent);

        if (contentChanged) {
            manifest.setLastModified(Instant.now());
        }
    }
}
