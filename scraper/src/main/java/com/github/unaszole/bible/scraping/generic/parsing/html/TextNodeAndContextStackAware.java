package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.scraping.generic.parsing.ContextStackAware;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextNodeAndContextStackAware extends ContextStackAware {
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

    /**
     * Determines if and how whitespaces in the text node's contents should be processed before parsing.
     */
    public TextNodeParser.WhitespaceProcessing whitespaceProcessing = TextNodeParser.WhitespaceProcessing.CSS;

    /**
     * A regexp used to test if a text node can be parsed by this parser.
     * If this regexp contains a capturing group, only the part of the string captured by the first group will be
     * passed to the context extractors.
     * If unset, this parser will accept all nodes and process their full text.
     */
    public Pattern regexp;

    private boolean isLineBoundary(Node node) {
        if(node == null) {
            return true;
        }
        if(node instanceof Element && ((Element)node).tagName().equalsIgnoreCase("br")) {
            return true;
        }
        return false;
    }

    private String processCssText(TextNode textNode) {
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

    private String processText(TextNode textNode) {
        switch (whitespaceProcessing) {
            case PRESERVE:
                return textNode.text();
            default:
                return processCssText(textNode);
        }
    }

    protected String extractText(TextNode textNode) {
        String text = processText(textNode);

        if(regexp == null) {
            return text;
        }

        Matcher matcher = regexp.matcher(text);
        if(!matcher.matches()) {
            // Regexp failed to match, so we can't parse.
            return null;
        }

        if(matcher.groupCount() >= 1) {
            // There was one capturing group : return only the captured portion.
            return matcher.group(1);
        }

        // Else, return the full string.
        return text;
    }

}
