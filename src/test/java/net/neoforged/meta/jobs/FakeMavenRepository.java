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
 * Mocks the NeoForged Maven API (/api/maven/versions endpoint).
 * <p>
 * Example usage:
 * <pre>
 * FakeMavenRepository repo = new FakeMavenRepository();
 * repo.addArtifact("releases", "net.neoforged", "neoforge")
 *     .withVersion("21.3.0")
 *     .withVersion("21.3.1")
 *     .withVersion("21.3.2");
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
     * @param repository Maven repository (e.g., "releases", "snapshots")
     * @param groupId    Maven group ID
     * @param artifactId Maven artifact ID
     * @return A builder for configuring the artifact metadata
     */
    public ArtifactMetadataBuilder addArtifact(String repository, String groupId, String artifactId) {
        String key = repository + ":" + groupId + ":" + artifactId;
        ArtifactMetadata metadata = new ArtifactMetadata(repository, groupId, artifactId);
        artifacts.put(key, metadata);

        return new ArtifactMetadataBuilder(metadata);
    }

    /**
     * Configure the server to return a 404 for a specific artifact.
     */
    public void addMissingArtifact(String repository, String groupId, String artifactId) {
        String key = repository + ":" + groupId + ":" + artifactId;
        artifacts.put(key, null); // null indicates 404
    }

    /**
     * Configure the server to return invalid JSON for a specific artifact.
     */
    public void addInvalidArtifact(String repository, String groupId, String artifactId) {
        String key = repository + ":" + groupId + ":" + artifactId;
        ArtifactMetadata metadata = new ArtifactMetadata(repository, groupId, artifactId);
        metadata.invalid = true;
        artifacts.put(key, metadata);
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

        public ArtifactMetadataBuilder withSnapshot(boolean snapshot) {
            metadata.snapshot = snapshot;
            return this;
        }
    }

    /**
     * Metadata for a Maven artifact.
     */
    private static class ArtifactMetadata {
        final String repository;
        final String groupId;
        final String artifactId;
        final List<String> versions = new ArrayList<>();
        boolean snapshot = false;
        boolean invalid = false;

        ArtifactMetadata(String repository, String groupId, String artifactId) {
            this.repository = repository;
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        String toJson() {
            if (invalid) {
                return "this is not valid JSON";
            }

            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"isSnapshot\": ").append(snapshot).append(",\n");
            json.append("  \"versions\": [\n");

            for (int i = 0; i < versions.size(); i++) {
                json.append("    \"").append(versions.get(i)).append("\"");
                if (i < versions.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("  ]\n");
            json.append("}\n");

            return json.toString();
        }
    }

    /**
     * HTTP handler that routes requests to the appropriate artifact metadata.
     */
    private class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // Expected format: /api/maven/versions/{repository}/{groupId}/{artifactId}
            if (!path.startsWith("/api/maven/versions/")) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            // Remove prefix
            String remainder = path.substring("/api/maven/versions/".length());
            String[] parts = remainder.split("/", 2);

            if (parts.length < 2) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            String repository = parts[0];
            String gavPath = parts[1]; // e.g., "net/neoforged/neoforge"

            // Convert GAV path back to groupId:artifactId
            int lastSlash = gavPath.lastIndexOf('/');
            if (lastSlash == -1) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            String groupId = gavPath.substring(0, lastSlash).replace('/', '.');
            String artifactId = gavPath.substring(lastSlash + 1);

            String key = repository + ":" + groupId + ":" + artifactId;
            ArtifactMetadata metadata = artifacts.get(key);

            if (metadata == null) {
                // Check if we have this key but it's null (indicating 404)
                if (artifacts.containsKey(key)) {
                    exchange.sendResponseHeaders(404, -1);
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
                exchange.close();
                return;
            }

            byte[] response = metadata.toJson().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }
}
