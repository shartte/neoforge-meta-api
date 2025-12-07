package net.neoforged.meta.config;

import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SoftwareComponentPublicationPropertiesRuleTest {
    @Test
    void testMatch() throws Exception {
        var rule = new SoftwareComponentPublicationPropertiesRule(
                List.of(
                        VersionRange.createFromVersionSpec("[20.5-alpha,20.6)")
                ),
                Set.of()
        );
        assertTrue(rule.matchesVersion("20.5.0-beta"));
    }
}