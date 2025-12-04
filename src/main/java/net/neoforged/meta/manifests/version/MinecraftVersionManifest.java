package net.neoforged.meta.manifests.version;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record MinecraftVersionManifest(String id, Map<String, MinecraftDownload> downloads,
                                       List<MinecraftLibrary> libraries,
                                       AssetIndexReference assetIndex, String assets, JavaVersionReference javaVersion,
                                       String mainClass, MinecraftArguments arguments) {
    public static MinecraftVersionManifest from(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(path.toFile(), MinecraftVersionManifest.class);
    }
}
