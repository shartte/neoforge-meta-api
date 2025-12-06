package net.neoforged.meta.maven;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from the NeoForged Maven API's /api/maven/versions endpoint.
 */
public record MavenVersionsResponse(
        @JsonProperty("isSnapshot")
        boolean snapshot,

        @JsonProperty("versions")
        List<String> versions
) {
}
