package com.github.unaszole.bible.scraping.generic;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @param patterns Named patterns, that can reference arguments using the syntax {argumentName}.
 * @param arguments Named arguments, of two kinds.
 *                  - Iteration arguments, which names start with $, cannot be referenced by patterns, but only by
 *                  argument expressions.
 *                  - Standard arguments, which names are alphanumeric, can be referenced by patterns. If they start
 *                  with "=", they are treated as "argument expressions", that can compute basic arithmetic operations
 *                  on the iteration arguments.
 * @param patternPrefix A prefix to prepend to requested pattern names before searching them inside the "patterns".
 *                      This allows inheriting patterns for multiple areas, while selecting one specific area for
 *                      evaluation.
 */
public record PatternConfig(
        @Nonnull Map<String, String> patterns,
        @Nonnull Map<String, String> arguments,
        @Nullable String patternPrefix
) {
    public @Nonnull PatternConfig withArgument(@Nonnull String name, @Nonnull String value) {
        Map<String, String> newArgs = new HashMap<>(arguments);
        newArgs.put(name, value);
        return new PatternConfig(patterns, newArgs, patternPrefix);
    }

    /**
     * Return a combination of this config defaulted by another - meaning that patterns and arguments of the other
     * config will be used when no pattern or argument of the same name exists in this container.
     *
     * @param other The config with default values.
     * @return The new config with values defaulted by the given config.
     */
    public @Nonnull PatternConfig defaultedBy(@Nonnull PatternConfig other) {
        Map<String, String> newPatterns = new HashMap<>();
        newPatterns.putAll(other.patterns);
        newPatterns.putAll(patterns);

        Map<String, String> newArgs = new HashMap<>();
        newArgs.putAll(other.arguments);
        newArgs.putAll(arguments);

        String newPatternPrefix = Optional.ofNullable(patternPrefix).orElse(other.patternPrefix);

        return new PatternConfig(newPatterns, newArgs, newPatternPrefix);
    }

    private static final Pattern BASIC_EXPR = Pattern.compile("^\\s*([0-9]+)\\s*(([+-])\\s*([0-9]+)\\s*)?$");
    public @Nonnull String evaluateExpression(@Nonnull String expression) {
        // Substitute all $ placeholders found in the arguments.
        final String[] substitutedExpr = new String[] {expression};
        arguments.entrySet().stream()
                .filter(e -> e.getKey().startsWith("$"))
                .forEach(e -> substitutedExpr[0] = substitutedExpr[0].replace(e.getKey(), e.getValue()));

        // Evaluate basic arithmetic expression.
        Matcher expr = BASIC_EXPR.matcher(substitutedExpr[0]);
        if (expr.matches()) {
            int val = Integer.parseInt(expr.group(1));
            if (expr.group(2) != null) {
                if ("+".equals(expr.group(3))) {
                    val += Integer.parseInt(expr.group(4));
                } else {
                    val -= Integer.parseInt(expr.group(4));
                }
            }
            return Integer.toString(val);
        }
        throw new IllegalArgumentException("Failed to evaluate " + expression);
    }

    public @Nonnull String getArgument(@Nonnull String argumentName) {
        String arg = Optional.ofNullable(arguments.get(argumentName))
                .orElseThrow(() -> new IllegalArgumentException("Missing argument " + argumentName + " in " + arguments));
        if(arg.startsWith("=")) {
            arg = evaluateExpression(arg.substring(1));
        }
        return arg;
    }

    public @Nonnull Optional<String> evaluate(@Nonnull String patternName) {
        String fullPatternName = patternPrefix != null ? patternPrefix + patternName : patternName;
        return Optional.ofNullable(patterns.get(fullPatternName))
                .map(p -> VarSubstitution.substituteVars(p, this::getArgument));
    }
}
