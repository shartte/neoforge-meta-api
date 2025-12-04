package net.neoforged.meta.util;

import java.util.Objects;

public record MavenCoordinate(String group, String artifact, String version, String classifier, String extension) {

    public MavenCoordinate {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(artifact, "artifact");
        Objects.requireNonNull(version, "version");
    }

    public static MavenCoordinate parse(String coordinate) {
        String[] parts = coordinate.split(":");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid Maven coordinate: " + coordinate);
        }

        String group = parts[0];
        String artifact = parts[1];
        String version = parts[2];
        String classifier = parts.length > 3 ? parts[3] : null;
        String extension = parts.length > 4 ? parts[4] : "jar";

        return new MavenCoordinate(group, artifact, version, classifier, extension);
    }

    public MavenCoordinate withClassifier(String classifier) {
        return new MavenCoordinate(group, artifact, version, classifier, extension);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(group).append(':').append(artifact).append(':').append(version);
        if (classifier != null) {
            sb.append(':').append(classifier);
        }
        if (extension != null && !"jar".equals(extension)) {
            sb.append('@').append(extension);
        }
        return sb.toString();
    }
}
