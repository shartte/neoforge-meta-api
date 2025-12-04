package net.neoforged.meta.manifests.version;

import java.util.Map;
import java.util.Objects;

public record Rule(
        RuleAction action,
        Map<String, Boolean> features,
        OsCondition os
) {
    public Rule {
        Objects.requireNonNull(action);
        features = Objects.requireNonNullElseGet(features, Map::of);
    }

    public boolean evaluate() {
        return features.isEmpty() && (os == null || os.platformMatches());
    }
}

