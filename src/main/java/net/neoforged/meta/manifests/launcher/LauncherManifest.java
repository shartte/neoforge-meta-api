package net.neoforged.meta.manifests.launcher;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

public record LauncherManifest(List<Version> versions) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static LauncherManifest from(Path path) {
        return mapper.readValue(path.toFile(), LauncherManifest.class);
    }

    public record Version(String id, String type, URI url, String sha1, OffsetDateTime releaseTime) {
    }
}
