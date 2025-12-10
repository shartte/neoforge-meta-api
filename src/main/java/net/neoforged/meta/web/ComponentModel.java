package net.neoforged.meta.web;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record ComponentModel(
        String groupId,
        String artifactId,
        int versions,
        @Nullable String highestVersion,
        @Nullable Instant highestVersionReleased,
        @Nullable String latestVersion,
        @Nullable Instant latestVersionReleased
) {
}
