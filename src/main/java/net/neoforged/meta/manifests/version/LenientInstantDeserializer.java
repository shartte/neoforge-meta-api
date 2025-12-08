package net.neoforged.meta.manifests.version;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.Instant;

public class LenientInstantDeserializer extends ValueDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String value = p.getValueAsString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        // Truncate nanoseconds to microseconds if needed
        if (value.contains(".") && !value.endsWith("Z")) {
            String[] parts = value.split("\\.");
            if (parts.length == 2 && parts[1].length() > 6) {
                value = parts[0] + "." + parts[1].substring(0, 6) + "Z";
            } else {
                value = value + "Z"; // Assume UTC
            }
        } else if (!value.endsWith("Z")) {
            value = value + "Z"; // Assume UTC
        }
        
        return Instant.parse(value);
    }
}