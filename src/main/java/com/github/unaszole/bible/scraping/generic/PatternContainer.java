package com.github.unaszole.bible.scraping.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Element that sets or overrides some patterns, or some arguments used in these patterns.
 * This element may override sets or patterns provided by its containers, and be overridden by any of its children.
 * It's possible to evaluate a pattern at this level, using the current arguments, with the {@link #getValue} method.
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
    /**
     * Name of the pattern to use to resolve page URLs for this container and its children (until overridden).
     */
    public String pagePattern;
    /**
     * Name of the pattern to use to resolve context values for this container and its children (until overridden).
     */
    public String valuePattern;

    private <K, V> void putAllIfNotNull(Map<K, V> target, Map<K,V> source) {
        assert target != null;
        if(source != null) {
            target.putAll(source);
        }
    }

    public PatternContainer defaultedBy(PatternContainer other) {
        Map<String, String> newPatterns = new HashMap<>();
        putAllIfNotNull(newPatterns, other.patterns);
        putAllIfNotNull(newPatterns, patterns);

        Map<String, String> newArgs = new HashMap<>();
        putAllIfNotNull(newArgs, other.args);
        putAllIfNotNull(newArgs, args);

        PatternContainer newContainer = new PatternContainer();
        newContainer.patterns = newPatterns;
        newContainer.args = newArgs;
        newContainer.pagePattern = pagePattern != null ? pagePattern : other.pagePattern;
        newContainer.valuePattern = valuePattern != null ? valuePattern : other.valuePattern;
        return newContainer;
    }

    public PatternContainer overriddenBy(PatternContainer other) {
        return other.defaultedBy(this);
    }

    private static final Pattern ARG_REFERENCE = Pattern.compile("\\{([A-Z0-9_]+)}");
    private String substituteArgs(String str, final Function<String, String> argGetter) {
        Matcher argRefs = ARG_REFERENCE.matcher(str);
        return argRefs.replaceAll(r -> argGetter.apply(r.group(1)));
    }

    /**
     * Evaluate a pattern at this level, defaulting to a parent level where needed, and evaluating
     * the arguments.
     * @param patternName Name of the pattern to substitute.
     * @param argEvaluator A function to evaluate argument values before feeding them to the pattern.
     * @return The final value of the source variable.
     */
    public String getValue(String patternName, Function<String, String> argEvaluator) {
        String pattern = patterns.get(patternName);
        if(pattern == null) {
            return null;
        }
        return substituteArgs(pattern, a -> argEvaluator.apply(args.get(a)));
    }

    /**
     * Evaluate a pattern at this level, defaulting to a parent level where needed, and evaluating
     * the arguments.
     * @param patternName Name of the pattern to substitute.
     * @return The final value of the source variable.
     */
    public String getValue(String patternName) {
        return getValue(patternName, s -> s);
    }
}
