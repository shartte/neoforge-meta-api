package net.neoforged.meta.extract;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangelogExtractorTest {
    static final String FIRST_CHANGELOG_ENTRY = """
            Redo conditional recipe datagen (#250)
            
            - Remove `ConditionalRecipeBuilder`.
            - Add `RecipeOutput.withConditions(...)` to add conditions to recipes.
            - Use `dispatchUnsafe` for the attachment codec to avoid nesting values
              when it's unnecessary.""";
    byte[] changelogBody;

    @BeforeEach
    void setUp() throws IOException {
        try (var in = ChangelogExtractorTest.class.getResourceAsStream("/neoforge-20.2.39-beta-changelog.txt")) {
            changelogBody = in.readAllBytes();
        }
    }

    @Test
    void testExtractMultiLine() {
        var changelogEntry = ChangelogExtractor.extract(changelogBody);
        assertEquals(FIRST_CHANGELOG_ENTRY, changelogEntry);
    }
}
