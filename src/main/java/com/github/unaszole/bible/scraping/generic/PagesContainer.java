package com.github.unaszole.bible.scraping.generic;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PagesContainer extends PatternContainer {

    public static class Page extends PatternContainer {

        public URL getUrl(PatternContainer defaults) {
            PatternContainer finalContainer = this.defaultedBy(defaults);
            if(finalContainer.pagePattern == null) {
                throw new IllegalArgumentException("No default pattern provided for " + this);
            }

            try {
                return new URL(finalContainer.getValue(pagePattern));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * List of pages.
     */
    public List<Page> pages;

    private List<String> getPageValues(final PatternContainer defaults, final Function<Page, String> patternName,
                                         final Function<String, String> argEvaluator) {
        if(pages == null) {
            return List.of();
        }
        return pages.stream()
                .map(p -> p.defaultedBy(defaults).getValue(patternName.apply(p), argEvaluator))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected List<String> getPageValues(PatternContainer defaults, final String patternName,
                                         Function<String, String> argEvaluator) {
        List<String> values = getPageValues(defaults, p -> patternName, argEvaluator);
        if(!values.isEmpty()) {
            return values;
        }

        // No page explicitly defined. Check if we have a value for this pattern to provide a default page.
        String value = defaults.getValue(patternName, argEvaluator);
        if(value != null) {
            return List.of(value);
        }

        return List.of();
    }

    protected List<URL> getPageUrls(PatternContainer defaults, Function<String, String> argEvaluator) {
        List<String> values = getPageValues(defaults, p -> p.pagePattern, argEvaluator);
        if(values.isEmpty()) {
            values = getPageValues(defaults, defaults.pagePattern, argEvaluator);
        }

        return values.stream()
                .map(s -> {
                    try {
                        // Try to fix the URL almost as a browser would do, so that users can input URLs like visible
                        // in their browser, even if not properly encoded.
                        // Taken from https://stackoverflow.com/a/30640843
                        URL rawUrl = new URL(s);
                        return new URL(
                                new URI(rawUrl.getProtocol(), rawUrl.getUserInfo(), rawUrl.getHost(), rawUrl.getPort(),
                                        rawUrl.getPath(), rawUrl.getQuery(), rawUrl.getRef()
                                ).toString().replace("%25", "%")
                        );
                    } catch (URISyntaxException | MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
