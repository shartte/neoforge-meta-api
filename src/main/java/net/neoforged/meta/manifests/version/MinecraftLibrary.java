package net.neoforged.meta.manifests.version;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.neoforged.meta.util.MavenCoordinate;
import net.neoforged.meta.util.OsType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record MinecraftLibrary(@JsonProperty("name") String artifactId, Downloads downloads, List<Rule> rules,
                               Map<OsType, String> natives) {
    public MinecraftLibrary {
        Objects.requireNonNull(artifactId, "name");
        rules = Objects.requireNonNullElseGet(rules, List::of);
    }

    public boolean rulesMatch() {
        if (rules.isEmpty()) {
            return true;
        }

        for (Rule rule : rules) {
            var ruleApplies = rule.evaluate();
            if (!ruleApplies && rule.action() == RuleAction.ALLOWED) {
                return false;
            } else if (ruleApplies && rule.action() == RuleAction.DISALLOWED) {
                return false;
            }
        }

        return true;
    }

    public MinecraftDownload getArtifactDownload() {
        if (downloads == null) {
            return null;
        }

        if (natives != null) {
            var classifier = natives.get(OsType.current());
            if (classifier != null) {
                var download = downloads.classifiers.get(classifier);
                if (download == null) {
                    throw new IllegalStateException("Download for " + artifactId + " references classifier " + classifier
                                                    + " for natives for OS " + OsType.current() + " but it doesn't exist.");
                }
                return download;
            }
        }

        return downloads.artifact;
    }

    public MavenCoordinate getMavenCoordinate() {
        var coordinate = MavenCoordinate.parse(artifactId);

        if (natives != null) {
            String classifier = natives.get(OsType.current());
            if (classifier != null) {
                coordinate = coordinate.withClassifier(classifier);
            }
        }

        return coordinate;
    }

    public record Downloads(MinecraftDownload artifact, Map<String, MinecraftDownload> classifiers) {
        public Downloads {
            if (classifiers == null) {
                classifiers = Map.of();
            }
        }
    }

    @Override
    public String toString() {
        return artifactId;
    }
}
