package com.github.unaszole.bible.scraping.generic;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class PagesContainer extends PatternContainer {

    public List<Page> pages;

    private List<Page> getPages(String withPatternName, PatternContainer containerDefaults) {
        if(pages != null) {
            // If pages are explicitly specified, return them.
            return pages;
        }

        if(containerDefaults.hasPattern(withPatternName)) {
            // If the requested pattern is set on the container, generate an implicit page.
            return List.of(new Page());
        }

        // Else, return no page at all.
        return List.of();
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
        return getPages(patternName, containerDefaults).stream()
                .map(p -> p.evaluate(containerDefaults, patternName, argEvaluator))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get the list of URLs for all pages in this container.
     * @param defaults The default pattern and argument values inherited from a higher level.
     * @param patternName The name of the pattern to evaluate.
     * @param argEvaluator A function to evaluate arguments before substituting them in the patten.
     *                     Can be the identity function if the arguments are given as literal.
     * @return The built URLs with all arguments substituted.
     */
    protected List<URL> getPageUrls(PatternContainer defaults, final String patternName,
                                    final Function<String, String> argEvaluator) {
        return getPageValues(defaults, patternName, argEvaluator).stream()
                .map(Page::toUrl)
                .collect(Collectors.toList());
    }
}
