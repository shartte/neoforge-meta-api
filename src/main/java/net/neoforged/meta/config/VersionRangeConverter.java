package net.neoforged.meta.config;

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

final class VersionRangeConverter implements Converter<String, VersionRange> {
    @Override
    public @Nullable VersionRange convert(String source) {
        try {
            return source == null ? null : VersionRange.createFromVersionSpec(source);
        } catch (InvalidVersionSpecificationException e) {
            throw new IllegalArgumentException("Failed to parse version range " + source + ": " + e);
        }
    }
}
