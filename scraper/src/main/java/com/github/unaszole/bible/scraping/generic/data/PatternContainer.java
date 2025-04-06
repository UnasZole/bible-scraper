package com.github.unaszole.bible.scraping.generic.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Element that sets or overrides some patterns, or some arguments used in these patterns.
 * This element may override args or patterns provided by its containers, and be overridden by any of its children.
 * It's possible to evaluate a pattern at this level, using the current arguments, with the {@link #evaluate} method.
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

    private <K, V> void putAllIfNotNull(Map<K, V> target, Map<K,V> source) {
        assert target != null;
        if(source != null) {
            target.putAll(source);
        }
    }

    /**
     * Return a combination of this container defaulted by another - meaning that patterns and arguments of the other
     * container will be used when no pattern or argument of the same name exists in this container.
     *
     * @param other The container with default values.
     * @return The new container with values defaulted by the given container.
     */
    public final PatternContainer defaultedBy(PatternContainer other) {
        Map<String, String> newPatterns = new HashMap<>();
        putAllIfNotNull(newPatterns, other.patterns);
        putAllIfNotNull(newPatterns, patterns);

        Map<String, String> newArgs = new HashMap<>();
        putAllIfNotNull(newArgs, other.args);
        putAllIfNotNull(newArgs, args);

        PatternContainer newContainer = new PatternContainer();
        newContainer.patterns = newPatterns;
        newContainer.args = newArgs;
        return newContainer;
    }

    public PatternContainer overriddenBy(PatternContainer other) {
        return other.defaultedBy(this);
    }

    public final boolean hasPattern(String patternName) {
        return patterns != null && patterns.containsKey(patternName);
    }

    /**
     * Evaluate a pattern from this container, using the argument values contained in this container.
     * @param patternName Name of the pattern to evaluate.
     * @param argEvaluator A function to evaluate argument values before feeding them to the pattern.
     *                     Can be the identity function if the arguments at this level are provided as literals,
     *                     or can be an expression evaluation function.
     * @return The result of pattern evaluation, or an empty optional if no pattern of the requested name exists.
     */
    public final Optional<String> evaluate(String patternName, Function<String, String> argEvaluator) {
        String pattern = patterns.get(patternName);
        if(pattern == null) {
            return Optional.empty();
        }
        return Optional.of(VarSubstitution.substituteVars(pattern, a -> argEvaluator.apply(args.get(a))));
    }
}
