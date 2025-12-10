package net.neoforged.meta.manifests.version;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;

class LenientInstantDeserializerTest {

    @Test
    void parseMinecraftDateTime() {
        var mapper = JsonMapper.builder()
                .addModule(new SimpleModule().addDeserializer(Instant.class, new LenientInstantDeserializer()))
                .build();

        mapper.readValue("\"2014-03-06T14:23:04+00:00\"", Instant.class);
    }

    @Test
    void parseNeoForgeDateTime() {
        var mapper = JsonMapper.builder()
                .addModule(new SimpleModule().addDeserializer(Instant.class, new LenientInstantDeserializer()))
                .build();

        mapper.readValue("\"2014-03-06T14:23:04.123456789\"", Instant.class);
        mapper.readValue("\"2014-03-06T14:23:04.12345678\"", Instant.class);
    }
}