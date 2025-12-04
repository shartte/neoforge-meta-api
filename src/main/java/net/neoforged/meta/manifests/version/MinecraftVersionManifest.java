package net.neoforged.meta.manifests.version;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record MinecraftVersionManifest(String id, Map<String, MinecraftDownload> downloads,
                                       List<MinecraftLibrary> libraries,
                                       AssetIndexReference assetIndex, String assets, JavaVersionReference javaVersion,
                                       String mainClass, MinecraftArguments arguments) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static MinecraftVersionManifest from(Path path) throws IOException {
        return MAPPER.readValue(path.toFile(), MinecraftVersionManifest.class);
    }

    public static MinecraftVersionManifest from(String jsonContent) throws IOException {
        return MAPPER.readValue(jsonContent, MinecraftVersionManifest.class);
    }
}
