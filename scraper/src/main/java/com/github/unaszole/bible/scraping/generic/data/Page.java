package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.downloading.SourceFile;

import java.util.Optional;
import java.util.function.Function;

public class Page extends PatternContainer {

    /**
     * Evaluate a pattern for this page.
     * @param defaults The parent context to default to for patterns and arguments.
     * @param patternName The name of the pattern to evaluate.
     * @param argEvaluator A function to evaluate arguments before substituting them in the pattern.
     * @return The result of pattern evaluation, or an empty optional if no pattern with the requested name exists.
     */
    public Optional<String> evaluate(PatternContainer defaults, String patternName, Function<String, String> argEvaluator) {
        PatternContainer finalContainer = this.defaultedBy(defaults);
        return finalContainer.evaluate(patternName, argEvaluator);
    }

    /**
     * Try to build a page file by evaluating the patterns of this container.
     * @param defaults The parent context to default to for patterns and arguments.
     * @param argEvaluator A function to evaluate arguments before substituting them in the pattern.
     * @param patternPrefix A prefix for the properties expected by the sourceFileBuilder.
     * @param sourceFileBuilder Logic to extract a source file from a set of properties.
     * @return The built source file, or empty optional if no source file could be found from the given properties.
     */
    public Optional<PageData> evaluateFile(PatternContainer defaults, Function<String, String> argEvaluator,
                                           String patternPrefix, SourceFile.Builder sourceFileBuilder) {
        final PatternContainer finalContainer = this.defaultedBy(defaults);

        return sourceFileBuilder.buildFrom(
                patternName -> finalContainer.evaluate(patternPrefix + patternName, argEvaluator)
        ).map(sf -> new PageData(
                sf,
                finalContainer.evaluateAllArgs(argEvaluator),
                finalContainer.evaluate(patternPrefix + "Parser", argEvaluator).orElse("main")
        ));
    }
}
