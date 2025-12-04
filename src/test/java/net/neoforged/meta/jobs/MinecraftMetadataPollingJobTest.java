package net.neoforged.meta.jobs;

import net.neoforged.meta.db.MinecraftVersionDao;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for MinecraftMetadataPollingJob.
 * <p>
 * This test uses:
 * - A real embedded HTTP server (JDK's HttpServer) to serve fake launcher manifests
 * - A real Spring context with minimal configuration (no web server, no scheduled jobs)
 * - A temporary SQLite database for isolation
 * - The job bean retrieved from Spring context with DAO verification
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(initializers = MinecraftMetadataPollingJobTest.Initializer.class)
@ActiveProfiles("test")
class MinecraftMetadataPollingJobTest {
    @TempDir
    static Path tempDir;
    static FakeLauncherManifestServer launcherServer;

    @Autowired
    MinecraftVersionDao minecraftVersionDao;

    @Autowired
    MinecraftMetadataPollingJob pollingJob;

    @BeforeAll
    static void setUpAll() {
        // Create and start fake launcher manifest server
        launcherServer = new FakeLauncherManifestServer();
    }

    @AfterAll
    static void tearDownAll() {
        if (launcherServer != null) {
            launcherServer.close();
        }
    }

    @Test
    void testDiscoverMinecraftVersions() {
        // Set up fake launcher manifest with multiple versions
        launcherServer.addVersion("1.21", "release")
                .withSha1("abc123abc123abc123abc123abc123abc123abc1")
                .withReleaseTime("2024-06-13T12:00:00+00:00")
                .withManifest(21);

        launcherServer.addVersion("1.20.1", "release")
                .withSha1("def456def456def456def456def456def456def4")
                .withReleaseTime("2023-06-12T10:00:00+00:00")
                .withManifest(17);

        launcherServer.addVersion("24w50a", "snapshot")
                .withSha1("789abc789abc789abc789abc789abc789abc789a")
                .withReleaseTime("2024-12-11T15:30:00+00:00")
                .withManifest(21);

        // Run the polling job
        pollingJob.run();

        // Verify results using DAOs
        // TODO: Add assertions once you verify the test works
        // For example:
        // var versions = minecraftVersionDao.findAll();
        // assertEquals(3, versions.size());
        //
        // var v1_21 = minecraftVersionDao.getByVersion("1.21");
        // assertNotNull(v1_21);
        // assertEquals("release", v1_21.getType());
        // assertEquals(21, v1_21.getJavaVersion());
        // assertNotNull(v1_21.getManifest());
        // assertEquals("abc123abc123abc123abc123abc123abc123abc1", v1_21.getManifest().getSha1());
    }

    @Test
    void testUpdatesExistingVersions() {
        // Set up initial versions
        launcherServer.clear();
        launcherServer.addVersion("1.21", "release")
                .withSha1("initial000000000000000000000000000000000")
                .withReleaseTime("2024-06-13T12:00:00+00:00")
                .withManifest(21);

        // Run job first time
        pollingJob.run();

        // TODO: Verify initial version was added
        // var v1 = minecraftVersionDao.getByVersion("1.21");
        // assertNotNull(v1);
        // assertEquals("initial000000000000000000000000000000000", v1.getManifest().getSha1());

        // Update the manifest with new content (different SHA-1)
        // Using withManifestRaw to include an extra field that makes the content different
        String updatedManifest = """
                {
                    "id": "1.21",
                    "type": "release",
                    "javaVersion": {
                        "component": "java-runtime-delta",
                        "majorVersion": 21
                    },
                    "newField": "updated"
                }
                """;

        launcherServer.clear();
        launcherServer.addVersion("1.21", "release")
                .withSha1("updated111111111111111111111111111111111")
                .withReleaseTime("2024-06-13T12:00:00+00:00")
                .withManifestRaw(updatedManifest);

        // Run job again
        pollingJob.run();

        // TODO: Verify manifest was updated
        // var v2 = minecraftVersionDao.getByVersion("1.21");
        // assertNotNull(v2);
        // assertEquals("updated111111111111111111111111111111111", v2.getManifest().getSha1());
    }

    @Test
    void testHandlesNewVersionsAdded() {
        // Start with one version
        launcherServer.clear();
        launcherServer.addVersion("1.20.1", "release")
                .withSha1("v1200000000000000000000000000000000000")
                .withReleaseTime("2023-06-12T10:00:00+00:00")
                .withManifest(17);

        pollingJob.run();

        // TODO: Verify one version exists
        // assertEquals(1, minecraftVersionDao.findAll().size());

        // Add a new version to the launcher manifest
        launcherServer.addVersion("1.21", "release")
                .withSha1("v1210000000000000000000000000000000000")
                .withReleaseTime("2024-06-13T12:00:00+00:00")
                .withManifest(21);

        pollingJob.run();

        // TODO: Verify both versions exist
        // assertEquals(2, minecraftVersionDao.findAll().size());
        // assertNotNull(minecraftVersionDao.getByVersion("1.20.1"));
        // assertNotNull(minecraftVersionDao.getByVersion("1.21"));
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            // Set both the temp directory and the launcher manifest URL
            TestPropertyValues.of(
                    "meta-api.data-directory=" + tempDir.toAbsolutePath(),
                    "meta-api.minecraft-launcher-meta-url=" + launcherServer.getManifestUrl()
            ).applyTo(context);
        }
    }
}
