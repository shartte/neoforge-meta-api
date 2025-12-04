package net.neoforged.meta.jobs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for creating a fake Maven repository HTTP server for testing.
 * <p>
 * Example usage:
 * <pre>
 * FakeMavenRepository repo = new FakeMavenRepository();
 * repo.start();
 * repo.addArtifact("net.neoforged", "neoforge")
 *     .withVersion("21.3.0")
 *     .withVersion("21.3.1")
 *     .withLatest("21.3.1")
 *     .withRelease("21.3.1");
 *
 * // Use repo.getBaseUrl() to get the repository URL
 *
 * repo.stop();
 * </pre>
 */
public class FakeMavenRepository implements AutoCloseable {
    private final HttpServer server;
    private final Map<String, ArtifactMetadata> artifacts = new HashMap<>();

    public FakeMavenRepository() {
        try {
            this.server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create fake maven repository.", e);
        }
        server.createContext("/", new RequestHandler());
        start();
    }

    /**
     * Start the HTTP server.
     */
    public void start() {
        server.setExecutor(null); // Use default executor
        server.start();
    }

    /**
     * Stop the HTTP server.
     */
    public void stop() {
        server.stop(0);
    }

    @Override
    public void close() {
        stop();
    }

    public void clear() {
        this.artifacts.clear();
    }

    /**
     * Get the base URL of this repository.
     */
    public URI getBaseUrl() {
        return URI.create("http://localhost:" + server.getAddress().getPort());
    }

    /**
     * Add an artifact to this repository.
     *
     * @param groupId    Maven group ID
     * @param artifactId Maven artifact ID
     * @return A builder for configuring the artifact metadata
     */
    public ArtifactMetadataBuilder addArtifact(String groupId, String artifactId) {
        String key = groupId + ":" + artifactId;
        ArtifactMetadata metadata = new ArtifactMetadata(groupId, artifactId);
        artifacts.put(key, metadata);

        return new ArtifactMetadataBuilder(metadata);
    }

    /**
     * Configure the server to return a 404 for a specific artifact.
     */
    public void addMissingArtifact(String groupId, String artifactId) {
        String path = "/" + groupId.replace('.', '/') + "/" + artifactId + "/maven-metadata.xml";
        server.createContext(path, exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
    }

    /**
     * Configure the server to return invalid XML for a specific artifact.
     */
    public void addInvalidArtifact(String groupId, String artifactId) {
        String path = "/" + groupId.replace('.', '/') + "/" + artifactId + "/maven-metadata.xml";
        server.createContext(path, exchange -> {
            byte[] response = "this is not valid XML".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
    }

    /**
     * Builder for artifact metadata.
     */
    public static class ArtifactMetadataBuilder {
        private final ArtifactMetadata metadata;

        private ArtifactMetadataBuilder(ArtifactMetadata metadata) {
            this.metadata = metadata;
        }

        public ArtifactMetadataBuilder withVersion(String version) {
            metadata.versions.add(version);
            return this;
        }

        public ArtifactMetadataBuilder withLatest(String version) {
            metadata.latest = version;
            return this;
        }

        public ArtifactMetadataBuilder withRelease(String version) {
            metadata.release = version;
            return this;
        }

        public ArtifactMetadataBuilder withLastUpdated(String timestamp) {
            metadata.lastUpdated = timestamp;
            return this;
        }
    }

    /**
     * Metadata for a Maven artifact.
     */
    private static class ArtifactMetadata {
        final String groupId;
        final String artifactId;
        final List<String> versions = new ArrayList<>();
        String latest;
        String release;
        String lastUpdated;

        ArtifactMetadata(String groupId, String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            // Default lastUpdated to current timestamp
            this.lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }

        String toXml() {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<metadata>\n");
            xml.append("  <groupId>").append(groupId).append("</groupId>\n");
            xml.append("  <artifactId>").append(artifactId).append("</artifactId>\n");
            xml.append("  <versioning>\n");

            if (latest != null) {
                xml.append("    <latest>").append(latest).append("</latest>\n");
            }
            if (release != null) {
                xml.append("    <release>").append(release).append("</release>\n");
            }

            xml.append("    <versions>\n");
            for (String version : versions) {
                xml.append("      <version>").append(version).append("</version>\n");
            }
            xml.append("    </versions>\n");

            xml.append("    <lastUpdated>").append(lastUpdated).append("</lastUpdated>\n");
            xml.append("  </versioning>\n");
            xml.append("</metadata>\n");

            return xml.toString();
        }
    }

    /**
     * HTTP handler that serves maven-metadata.xml.
     */
    private static class MavenMetadataHandler implements HttpHandler {
        private final ArtifactMetadata metadata;

        MavenMetadataHandler(ArtifactMetadata metadata) {
            this.metadata = metadata;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] response = metadata.toXml().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }

    private class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            System.out.println();
        }
    }
}
