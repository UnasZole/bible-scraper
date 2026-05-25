package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.scraping.generic.PatternConfig;

import java.util.Map;
import java.util.Optional;

/**
 * Element that sets or overrides some patterns, or some arguments used in these patterns.
 * This element may override args or patterns provided by its containers, and be overridden by any of its children.
 */
public class PatternContainer {
    /**
     * Patterns set or overridden at this level.
     * May reference any argument (set at this level, or at an overriding or defaulting level) with the syntax {ARG_NAME}.
     */
    public Map<String, String> patterns;
    /**
     * Arguments set or overridden at this level.
     */
    public Map<String, String> args;

    public PatternConfig asPatternConfig() {
        return new PatternConfig(
                Optional.ofNullable(patterns).orElse(Map.of()),
                Optional.ofNullable(args).orElse(Map.of()),
                null
        );
    }
}
