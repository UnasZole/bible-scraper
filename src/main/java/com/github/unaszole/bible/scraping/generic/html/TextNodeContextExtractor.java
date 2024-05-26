package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.scraping.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration to extract a context from an HTML text node.
 */
public class TextNodeContextExtractor extends ContextExtractor {
    public enum WhitespaceProcessing {
        /**
         * Preserve whitespaces as in the source document.
         */
        PRESERVE,
        /**
         * Process whitespaces according to CSS rules to remove meaningless whitespaces.
         * cf. https://www.w3.org/TR/css-text-3/#white-space-processing
         * This is a best-effort implementation, as this parser does not have the same notion of "inline context" and
         * overall rendering rules as a web browser.
         */
        CSS
    }

    public WhitespaceProcessing whitespaceProcessing = WhitespaceProcessing.CSS;

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

    private boolean isLineBoundary(Node node) {
        if(node == null) {
            return true;
        }
        if(node instanceof Element && ((Element)node).tagName().equalsIgnoreCase("br")) {
            return true;
        }
        return false;
    }

    private String extractCssText(TextNode textNode) {
        String str = textNode.text();

        // "Carriage returns (U+000D) are treated identically to spaces (U+0020) in all respects."
        str = str.replace("\r", " ");

        // "Any sequence of collapsible spaces and tabs immediately preceding or following a segment break is removed."
        str = str.replaceAll("[ \\t]*\\n[ \\t]*", "\n");
        // "any collapsible segment break immediately following another collapsible segment break is removed."
        str = str.replaceAll("\\n+", "\n");
        // "any remaining segment break is either transformed into a space"
        str = str.replace("\n", " ");
        // "Every collapsible tab is converted to a collapsible space"
        str = str.replace("\t", " ");

        // "Any collapsible space immediately following another collapsible space [...] is collapsed"
        str = str.replaceAll(" +", " ");

        // "A sequence of collapsible spaces at the beginning of a line is removed."
        if(isLineBoundary(textNode.previousSibling())) {
            str = str.replaceAll("^ +", "");
        }
        // "A sequence of collapsible spaces at the end of a line is removed"
        if(isLineBoundary(textNode.nextSibling())) {
            str = str.replaceAll(" +$", "");
        }

        return str;
    }

    private String extractText(TextNode textNode) {
        switch (whitespaceProcessing) {
            case PRESERVE:
                return textNode.text();
            default:
                return extractCssText(textNode);
        }
    }

    public Context extract(TextNode textNode, ContextMetadata parent, ContextMetadata previousOfType) {
        Matcher matcher = regexp.matcher(extractText(textNode));

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
