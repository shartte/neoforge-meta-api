package net.neoforged.meta.manifests.version;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.neoforged.meta.util.DownloadSpec;

import java.net.URI;

public record MinecraftDownload(@JsonProperty("sha1") String checksum, int size,
                                @JsonProperty("url") URI uri, String path) implements DownloadSpec {
    @Override
    public String checksumAlgorithm() {
        return "sha1";
    }
}
