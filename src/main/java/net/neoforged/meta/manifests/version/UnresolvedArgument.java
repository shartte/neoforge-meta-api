package net.neoforged.meta.manifests.version;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.List;

@JsonSerialize(using = UnresolvedArgument.Serializer.class)
@JsonDeserialize(using = UnresolvedArgument.Deserializer.class)
public sealed interface UnresolvedArgument {

    class Serializer extends JsonSerializer<UnresolvedArgument> {
        @Override
        public void serialize(UnresolvedArgument value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            switch (value) {
                case ConditionalValue conditionalValue -> serializers.defaultSerializeValue(conditionalValue, gen);
                case Value(String stringValue) -> gen.writeString(stringValue);
                case null -> gen.writeNull();
            }
        }
    }

    class Deserializer extends JsonDeserializer<UnresolvedArgument> {
        @Override
        public UnresolvedArgument deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken token = p.currentToken();

            if (token == JsonToken.VALUE_NULL) {
                return null;
            } else if (token == JsonToken.VALUE_STRING) {
                return new Value(p.getValueAsString());
            } else if (token == JsonToken.START_OBJECT) {
                // Parse as tree to handle value normalization
                var obj = (com.fasterxml.jackson.databind.node.ObjectNode) p.readValueAsTree();

                if (obj.has("value") && obj.get("value").isTextual()) {
                    // Convert single string value to array
                    var arrayNode = obj.putArray("value");
                    arrayNode.add(obj.get("value").asText());
                }

                return ctxt.readTreeAsValue(obj, ConditionalValue.class);
            }

            throw new IOException("Expected string, null or object, got: " + token);
        }
    }

    record Value(String value) implements UnresolvedArgument {
    }

    record ConditionalValue(List<String> value, List<Rule> rules) implements UnresolvedArgument {
    }
}
