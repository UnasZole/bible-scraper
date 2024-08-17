package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.scraping.generic.parsing.GenericContextExtractor;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration to extract a context from an HTML element.
 */
public class ElementContextExtractor extends GenericContextExtractor<Element> {

    private static final Logger LOG = LoggerFactory.getLogger(ElementContextExtractor.class);

    /**
     * If specified, this selector is relative to the parsed element : the first descendant matching this selector will
     * be used by this extractor.
     */
    public Evaluator selector;
    /**
     * If provided, and if the "selector" points to an element with an "href" attribute (typically an "a"), then
     * this link target selector is evaluated from the target of the link to select another element.
     * Typically used to fetch note contents elsewhere in a page from a note reference link.
     */
    public Evaluator linkTargetSelector;

    /**
     * An operator to extract a string from the selected element. The following are supported :
     * <li>text : extract the full text of this element and all its descendants.</li>
     * <li>ownText : extract the text of this element, excluding its descendants.</li>
     * <li>attribute=&lt;attribute name&gt; : extract an attribute of this element by providing its name.</li>
     * <li>literal=&lt;value&gt; : Provide a hardcoded value.</li>
     */
    public String op;

    /**
     * A regexp to capture a part of the text content extracted by the operator.
     * This regexp will be implicitly anchored, and must contain one single capturing group.
     * If unset, the full result of the operator will be used.
     */
    public Pattern regexp;

    /**
     * Configuration to extract additional contexts as descendants of this one.
     * All these extractors are relative to the root element selected by the element parser, NOT to the target element
     * of the present extractor.
     */
    public List<ElementContextExtractor> descendants;

    @Override
    protected List<? extends GenericContextExtractor<Element>> getDescendants() {
        return descendants;
    }

    private String op(Element e) {
        String[] opSplit = op.split("=");

        switch (opSplit[0]) {
            case "attribute":
                return e.attr(opSplit[1]);
            case "literal":
                return opSplit[1];
            case "ownText":
                return e.ownText();
            case "text":
            default:
                return e.text();
        }
    }

    private String extractValueFromTargetElement(Element e) {
        String opResult = op(e);

        if (regexp != null && opResult != null) {
            Matcher matcher = regexp.matcher(opResult);
            if (matcher.matches()) {
                opResult = matcher.group(1);
            } else {
                LOG.warn("Failed to match " + opResult + " against " + regexp);
                opResult = null;
            }
        }

        return opResult;
    }

    /**
     * @param parsedElt      The element selected by the {@link ElementParser}.
     * @return The value for the new context.
     */
    @Override
    protected String extractValue(Element parsedElt) {
        if(op == null) {
            return null;
        }

        Element targetElt = selector != null ? parsedElt.select(selector).first() : parsedElt;

        Element actualTargetElt = targetElt;

        if (linkTargetSelector != null && actualTargetElt.hasAttr("href")) {
            String[] link = actualTargetElt.attr("href").split("#");
            if (!link[0].isEmpty()) {
                throw new IllegalArgumentException("Cannot follow link " + link + " as it's not local to the page");
            }
            actualTargetElt = targetElt.ownerDocument().getElementById(link[1]).selectFirst(linkTargetSelector);
        }

        return extractValueFromTargetElement(actualTargetElt);
    }
}
