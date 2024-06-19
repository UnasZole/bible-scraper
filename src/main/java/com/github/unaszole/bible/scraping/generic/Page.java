package com.github.unaszole.bible.scraping.generic;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

public class Page extends PatternContainer {

    public static URL toUrl(String str) {
        try {
            // Try to fix the URL almost as a browser would do, so that users can input URLs like visible
            // in their browser, even if not properly encoded.
            // Taken from https://stackoverflow.com/a/30640843
            URL rawUrl = new URL(str);
            return new URL(
                    new URI(rawUrl.getProtocol(), rawUrl.getUserInfo(), rawUrl.getHost(), rawUrl.getPort(),
                            rawUrl.getPath(), rawUrl.getQuery(), rawUrl.getRef()
                    ).toString().replace("%25", "%")
            );
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

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
}
