package net.neoforged.meta.manifests.version;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.neoforged.meta.util.DownloadSpec;

import java.net.URI;

public record AssetIndexReference(String id, @JsonProperty("sha1") String checksum, int size, long totalSize,
                                  @JsonProperty("url") URI uri) implements DownloadSpec {
    @Override
    public String checksumAlgorithm() {
        return "sha1";
    }
}
