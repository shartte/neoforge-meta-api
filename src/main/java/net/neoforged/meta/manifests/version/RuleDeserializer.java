package net.neoforged.meta.manifests.version;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;

public class RuleDeserializer extends JsonDeserializer<Rule> {
    @Override
    public Rule deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (!node.isObject()) {
            throw new IOException("Rule must be an object: " + node);
        }

        RuleAction action = ctxt.readTreeAsValue(node.get("action"), RuleAction.class);
        Map<String, Boolean> features = null;
        if (node.has("features")) {
            JsonParser featuresParser = node.get("features").traverse(p.getCodec());
            featuresParser.nextToken(); // Move to START_OBJECT
            features = p.getCodec().readValue(featuresParser, new TypeReference<Map<String, Boolean>>() {
            });
        }
        OsCondition os = node.has("os") ? ctxt.readTreeAsValue(node.get("os"), OsCondition.class) : null;

        return new Rule(action, features, os);
    }
}
