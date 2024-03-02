package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.scraping.Parser;
import org.jsoup.nodes.TextNode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration to extract a context from an HTML text node.
 */
public class TextNodeContextExtractor extends ContextExtractor {

    /**
     * Regexp to validate the text node contents, and capture a context value from it.
     * If the regexp does not match the text node contents, then no context can be extracted
     * If the regexp matches, and contains a capturing group, then the value of the first capturing group will be used
     * as context value.
     * If the regexp matches but has no capturing group, then the context will have a null value.
     */
    public Pattern regexp;

    /**
     * Configuration to extract additional descendant contexts under this one.
     * All extractors are relative to the same text node, but may use different regexps to capture different portions.
     *
     * NOTE : all of these extractors MUST match : the {@link #regexp} should be written in a way that guarantees that
     * all the descendant extractor regexps also match.
     */
    public List<TextNodeContextExtractor> descendantExtractors;

    public Context extract(TextNode textNode, ContextMetadata parent, ContextMetadata previousOfType) {
        Matcher matcher = regexp.matcher(textNode.text());

        if(matcher.matches()) {
            // The regexp actually matches, we can open a context.
            String value = matcher.groupCount() >= 1 ? matcher.group(1) : null;
            ContextMetadata meta = getContextMetadata(parent, previousOfType, value);

            Context[] descendants = new Context[0];
            if (descendantExtractors != null) {
                descendants = new Context[descendantExtractors.size()];

                for (int i = 0; i < descendantExtractors.size(); i++) {
                    TextNodeContextExtractor descendant = descendantExtractors.get(i);
                    descendants[i] = descendant.extract(textNode, meta, null);
                }
            }

            return Parser.buildContext(meta, value, descendants);
        }

        return null;
    }
}
