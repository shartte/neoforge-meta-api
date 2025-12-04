package net.neoforged.meta.jobs;

import net.neoforged.meta.db.MinecraftVersionDao;
import org.junit.jupiter.api.AutoClose;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration test for MavenVersionDiscoveryJob.
 * <p>
 * This test uses:
 * - A real embedded HTTP server (JDK's HttpServer) to serve fake Maven metadata
 * - A real Spring context with minimal configuration (no web server, no scheduled jobs)
 * - A temporary SQLite database for isolation
 * - Manual job execution with DAO verification
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(initializers = MavenVersionDiscoveryJobTest.Initializer.class)
@ActiveProfiles("test")
class MavenVersionDiscoveryJobTest {
    @TempDir
    static Path tempDir;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    MinecraftVersionDao minecraftVersionDao;

    @AutoClose
    private FakeMavenRepository mavenRepo = new FakeMavenRepository();

    @Test
    void testDiscoverVersionsFromMavenMetadata() {
        // Set up fake Maven repository with multiple versions
        mavenRepo.addArtifact("net.neoforged", "neoforge")
                .withVersion("21.3.0-beta")
                .withVersion("21.3.1-beta")
                .withVersion("21.3.2")
                .withVersion("21.3.3")
                .withVersion("21.3.4")
                .withVersion("21.3.5-beta")
                .withLatest("21.3.5-beta")
                .withRelease("21.3.4");

        // Create and run the job
        MavenVersionDiscoveryJob job = new MavenVersionDiscoveryJob(
                mavenRepo.getBaseUrl(),
                "net.neoforged",
                "neoforge"
        );

        job.run();

        // Verify results using DAOs
        // TODO: Add assertions once the job implementation is complete
        // For example:
        // var versions = neoforgeVersionDao.findAll();
        // assertEquals(6, versions.size());
        // assertTrue(versions.stream().anyMatch(v -> v.getVersion().equals("21.3.5-beta")));
    }

    @Test
    void testHandlesMissingMavenMetadata() {
        // Configure repository to return 404
        mavenRepo.addMissingArtifact("net.neoforged", "neoforge");

        MavenVersionDiscoveryJob job = new MavenVersionDiscoveryJob(
                mavenRepo.getBaseUrl(),
                "net.neoforged",
                "neoforge"
        );

        // Should not throw exception
        assertDoesNotThrow(job::run);

        // Verify no versions were added
        // TODO: Add assertions
    }

    @Test
    void testHandlesInvalidXml() {
        // Configure repository to return invalid XML
        mavenRepo.addInvalidArtifact("net.neoforged", "neoforge");

        MavenVersionDiscoveryJob job = new MavenVersionDiscoveryJob(
                mavenRepo.getBaseUrl(),
                "net.neoforged",
                "neoforge"
        );

        // Should handle gracefully
        assertDoesNotThrow(job::run);

        // Verify no versions were added
        // TODO: Add assertions
    }

    @Test
    void testUpdatesExistingVersions() {
        // Set up repository with initial versions
        mavenRepo.addArtifact("net.neoforged", "neoforge")
                .withVersion("21.3.0")
                .withVersion("21.3.1")
                .withLatest("21.3.1")
                .withRelease("21.3.1");

        MavenVersionDiscoveryJob job = new MavenVersionDiscoveryJob(
                mavenRepo.getBaseUrl(),
                "net.neoforged",
                "neoforge"
        );

        // Run job first time
        job.run();

        // TODO: Verify initial versions were added

        // Stop and restart with updated versions
        mavenRepo.clear();
        mavenRepo.addArtifact("net.neoforged", "neoforge")
                .withVersion("21.3.0")
                .withVersion("21.3.1")
                .withVersion("21.3.2")  // New version
                .withLatest("21.3.2")
                .withRelease("21.3.2");

        // Run job again with updated repository
        job = new MavenVersionDiscoveryJob(
                mavenRepo.getBaseUrl(),
                "net.neoforged",
                "neoforge"
        );
        job.run();

        // TODO: Verify new version was added
        // var versions = neoforgeVersionDao.findAll();
        // assertEquals(3, versions.size());
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "meta-api.data-directory=" + tempDir.toAbsolutePath()
            ).applyTo(context);
        }
    }
}
