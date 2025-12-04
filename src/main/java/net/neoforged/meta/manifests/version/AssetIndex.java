package net.neoforged.meta.manifests.version;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public record AssetIndex(Map<String, AssetObject> objects) {
    public static AssetIndex from(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(path.toFile(), AssetIndex.class);
    }
}
