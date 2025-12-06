package net.neoforged.meta.maven;

import net.neoforged.meta.config.MavenRepositoryProperties;
import net.neoforged.meta.config.MetaApiProperties;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.dataformat.xml.XmlMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MavenRepositoriesFacade {
    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoriesFacade.class);

    private final Map<String, RestClient> restClients;
    private final tools.jackson.dataformat.xml.XmlMapper xmlMapper;

    public MavenRepositoriesFacade(MetaApiProperties properties) {
        this.xmlMapper = new XmlMapper();
        this.restClients = properties.getMavenRepositories().stream()
                .collect(Collectors.toMap(
                        MavenRepositoryProperties::getId,
                        mr -> RestClient.builder()
                                .baseUrl(mr.getUrl())
                                .build()
                ));
    }

    /**
     * Retrieve the list of versions for a Maven artifact from maven-metadata.xml.
     *
     * @param repositoryId The repository ID (must be configured in maven-repositories)
     * @param groupId      The Maven group ID (e.g., "net.neoforged")
     * @param artifactId   The Maven artifact ID (e.g., "neoforge")
     * @return List of version strings, or empty list if not found or on error
     */
    public List<String> listComponentVersions(String repositoryId, String groupId, String artifactId) {
        try {
            String metadataPath = getComponentPath(groupId, artifactId) + "/maven-metadata.xml";

            logger.debug("Fetching maven-metadata.xml from repository '{}' for {}:{} at path: {}",
                    repositoryId, groupId, artifactId, metadataPath);

            // Fetch maven-metadata.xml as String
            String xmlContent = getRestClient(repositoryId)
                    .get()
                    .uri(metadataPath)
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve()
                    .body(String.class);

            if (xmlContent == null || xmlContent.isBlank()) {
                logger.warn("Empty or null maven-metadata.xml returned for {}:{} in repository '{}'",
                        groupId, artifactId, repositoryId);
                return Collections.emptyList();
            }

            // Parse the XML using Jackson XmlMapper
            MavenMetadata metadata = xmlMapper.readValue(xmlContent, MavenMetadata.class);

            if (metadata == null || metadata.getVersioning() == null) {
                logger.warn("No metadata found for {}:{} in repository '{}'", groupId, artifactId, repositoryId);
                return Collections.emptyList();
            }

            List<String> versions = metadata.getVersioning().getVersions();
            if (versions == null) {
                logger.warn("No versions found in metadata for {}:{} in repository '{}'", groupId, artifactId, repositoryId);
                return Collections.emptyList();
            }

            logger.info("Found {} versions for {}:{} in repository '{}'", versions.size(), groupId, artifactId, repositoryId);
            return versions;

        } catch (Exception e) {
            logger.error("Error fetching versions for {}:{} from repository '{}': {}",
                    groupId, artifactId, repositoryId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public HttpHeaders headArtifact(String repositoryId, String groupId, String artifactId, String version, @Nullable String classifier, @Nullable String extension) {
        String url = getArtifactPath(groupId, artifactId, version, classifier, extension);
        try {
            return getRestClient(repositoryId)
                    .head()
                    .uri(url)
                    .header("Accept-Encoding", "") // Workaround for
                    .retrieve()
                    .toBodilessEntity()
                    .getHeaders();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve headers from URL " + url, e);
        }
    }

    private RestClient getRestClient(String repositoryId) {
        return Objects.requireNonNull(restClients.get(repositoryId), () -> "No repository is configured for id '" + repositoryId + "'");
    }

    private static String getComponentPath(String groupId, String artifactId) {
        return Stream.concat(
                        Arrays.stream(groupId.split("\\.")),
                        Stream.of(artifactId)
                ).map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));

    }

    private static String getArtifactPath(String groupId, String artifactId, String version, @Nullable String classifier, @Nullable String extension) {
        var encodedVersion = URLEncoder.encode(version, StandardCharsets.UTF_8);
        var path = getComponentPath(groupId, artifactId) + "/" + encodedVersion + "/" + URLEncoder.encode(artifactId, StandardCharsets.UTF_8) + "-" + encodedVersion;
        if (classifier != null) {
            path += "-" + URLEncoder.encode(classifier, StandardCharsets.UTF_8);
        }
        if (extension == null) {
            path += ".jar";
        } else {
            path += "." + URLEncoder.encode(extension, StandardCharsets.UTF_8);
        }
        return path;
    }
}
