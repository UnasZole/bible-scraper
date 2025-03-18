package com.github.unaszole.bible.writing;

import org.junit.jupiter.api.Test;

import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

public class TypographyTest {

    @Test
    public void testFrenchFixer() {

        UnaryOperator<String> french = Typography.getFixer(Typography.Fixer.FRENCH);

        // Test non-breakable space before colon.
        assertEquals("A : B.", french.apply("A : B."));
        assertEquals("A : B.", french.apply("A : B."));
        assertEquals("A : B.", french.apply("A : B."));
        assertEquals("A : B.", french.apply("A   : B."));
        assertEquals("A : B.", french.apply("A     \t: B."));
    }
}
