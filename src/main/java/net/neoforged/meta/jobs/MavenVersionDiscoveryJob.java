package net.neoforged.meta.jobs;

import net.neoforged.meta.config.MetaApiProperties;
import net.neoforged.meta.config.SoftwareComponentProperties;
import net.neoforged.meta.config.SoftwareComponentPublicationPropertiesRule;
import net.neoforged.meta.db.MavenComponentVersion;
import net.neoforged.meta.db.MavenComponentVersionDao;
import net.neoforged.meta.maven.MavenRepositoriesFacade;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Discover new versions of a Maven component using the NeoForged Maven API.
 * <p>
 * This job fetches version information from the /api/maven/versions endpoint
 * and stores discovered versions in the database.
 */
@Component
public class MavenVersionDiscoveryJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MavenVersionDiscoveryJob.class);

    private final MavenComponentVersionDao versionDao;
    private final List<SoftwareComponentProperties> components;
    private final MavenRepositoriesFacade mavenRepositories;
    private final TransactionTemplate transactionTemplate;

    public MavenVersionDiscoveryJob(
            MavenComponentVersionDao versionDao,
            MetaApiProperties apiProperties,
            MavenRepositoriesFacade mavenRepositories,
            TransactionTemplate transactionTemplate
    ) {
        this.versionDao = versionDao;
        this.components = apiProperties.getComponents();
        this.mavenRepositories = mavenRepositories;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run() {
        for (var component : components) {
            transactionTemplate.executeWithoutResult(ignored -> discoverComponent(component));
        }
    }

    public void discoverComponent(SoftwareComponentProperties component) {
        var groupId = component.getGroupId();
        var artifactId = component.getArtifactId();
        var repository = component.getMavenRepositoryId();

        try {
            logger.info("Discovering Maven versions for {}:{} in repository {}", groupId, artifactId, repository);

            var discoveredVersions = mavenRepositories.listComponentVersions(component.getMavenRepositoryId(), component.getGroupId(), component.getArtifactId());

            logger.info("Found {} versions for component {}:{}", discoveredVersions.size(), groupId, artifactId);

            // Get existing versions to avoid duplicates
            var existingVersions = Set.copyOf(versionDao.findAllVersionsByGA(groupId, artifactId));

            int newVersions = 0;
            for (String version : discoveredVersions) {
                if (!existingVersions.contains(version)) {
                    var entity = new MavenComponentVersion();
                    entity.setGroupId(groupId);
                    entity.setArtifactId(artifactId);
                    entity.setVersion(version);
                    entity.setSnapshot(version.endsWith("-SNAPSHOT"));
                    entity.setRepository(repository);

                    // Use last-modified of the .pom file which every maven publication should have as the last-modified timestamp of the release and thus the release time
                    var pomHeaders = mavenRepositories.headArtifact(repository, groupId, artifactId, version, null, "pom");
                    long lastModified = pomHeaders.getLastModified();
                    if (lastModified != -1) {
                        entity.setReleased(Instant.ofEpochMilli(lastModified));
                    } else {
                        entity.setReleased(entity.getDiscovered());
                    }

                    var publicationRule = findMatchingPublicationRule(component, version);
                    if (publicationRule != null) {
                        for (var artifact : publicationRule.artifacts()) {
                            var artifactHeaders = mavenRepositories.headArtifact(repository, groupId, artifactId, version, artifact.classifier(), artifact.extension());

                            var artifactContentLength = artifactHeaders.getContentLength();
                            var artifactLastModified = artifactHeaders.getLastModified();
                            var etag = artifactHeaders.getETag();
                        }
                    }

                    versionDao.saveAndFlush(entity);
                    newVersions++;
                    logger.info("Discovered new version: {}:{}:{}", groupId, artifactId, version);
                }
            }

            logger.info("Completed Maven version discovery for {}:{}. Found {} new versions out of {} total",
                    groupId, artifactId, newVersions, discoveredVersions.size());

        } catch (Exception e) {
            logger.error("Error discovering Maven versions for {}:{}", groupId, artifactId, e);
        }
    }

    private static @Nullable SoftwareComponentPublicationPropertiesRule findMatchingPublicationRule(SoftwareComponentProperties component, String version) {
        // Find the appropriate rule to apply
        for (var rule : component.getPublicationRules()) {
            if (rule.matchesVersion(version)) {
                return rule;
            }
        }
        return null;
    }
}
