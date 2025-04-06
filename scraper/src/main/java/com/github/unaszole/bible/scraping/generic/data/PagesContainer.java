package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.downloading.SourceFile;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class PagesContainer extends PatternContainer {

    public List<Page> pages;

    private List<Page> getPages() {
        if(pages != null) {
            // If pages are explicitly specified, return them.
            return pages;
        }

        // Else, return an implicit page.
        return List.of(new Page());
    }

    /**
     * Get the list of values for all pages in this container.
     * @param defaults The default pattern and argument values inherited from a higher level.
     * @param patternName The name of the pattern to evaluate.
     * @param argEvaluator A function to evaluate arguments before substituting them in the patten.
     *                     Can be the identity function if the arguments are given as literal.
     * @return The built URLs with all arguments substituted.
     */
    protected List<String> getPageValues(PatternContainer defaults, final String patternName,
                                         final Function<String, String> argEvaluator) {
        final PatternContainer containerDefaults = this.defaultedBy(defaults);
        return getPages().stream()
                .map(p -> p.evaluate(containerDefaults, patternName, argEvaluator))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get the list of URLs for all pages in this container.
     * @param defaults The default pattern and argument values inherited from a higher level.
     * @param argEvaluator A function to evaluate arguments before substituting them in the patten.
     *                     Can be the identity function if the arguments are given as literal.
     * @param patternPrefix Prefix to prepend to the pattern names compared to those expected by the builder
     * @param sourceFileBuilder Builder to extract sources files based on the properties available.
     * @return The built source files.
     */
    protected List<PageData> getPageFiles(PatternContainer defaults, final Function<String, String> argEvaluator,
                                          final String patternPrefix, final SourceFile.Builder sourceFileBuilder) {
        final PatternContainer containerDefaults = this.defaultedBy(defaults);
        return getPages().stream()
                .map(p -> p.evaluateFile(containerDefaults, argEvaluator, patternPrefix, sourceFileBuilder))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
