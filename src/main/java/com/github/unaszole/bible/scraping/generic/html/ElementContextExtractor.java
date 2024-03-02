package com.github.unaszole.bible.scraping.generic.html;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.Parser;
import com.github.unaszole.bible.scraping.ParsingUtils;
import com.github.unaszole.bible.scraping.implementations.GenericHtml;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;

import java.util.List;

/**
 * Configuration to extract a context from an HTML element.
 */
public class ElementContextExtractor extends ContextExtractor {

    /**
     * A selector that selects only HTML elements that this extractor can build a context from.
     * For an extractor at the root of the parser, MUST always be provided : checks if the incoming element can open
     * this context.
     * For a descendant extractor, may be omitted, in which case the descendant context will be extracted
     * from the same element.
     */
    public Evaluator selector;
    /**
     * If provided, and if the "selector" points to an element with an "href" attribute (typically an "a"), then
     * this link target selector is evaluated from the target of the link to select another element.
     * Typically used to fetch note contents elsewhere in a page from a note reference link.
     */
    public Evaluator linkTargetSelector;
    /**
     * Configuration to extract a value for this context. This extractor is relative to the element selected by the
     * {@link #selector}.
     * This MUST be set for CHAPTER, VERSE and TEXT contexts !
     * It should be left null for other contexts.
     */
    public ElementValueExtractor valueExtractor;
    /**
     * Configuration to extract additional descendant contexts under this one.
     * All extractors are relative to the element selected by the {@link #selector}.
     */
    public List<ElementContextExtractor> descendantExtractors;



    private Context extractInternal(Element targetElt, ContextMetadata parent, ContextMetadata previousOfType) {
        Element actualTargetElt = targetElt;

        if (linkTargetSelector != null && actualTargetElt.hasAttr("href")) {
            String[] link = actualTargetElt.attr("href").split("#");
            if (!link[0].isEmpty()) {
                throw new IllegalArgumentException("Cannot follow link " + link + " as it's not local to the page");
            }
            actualTargetElt = targetElt.ownerDocument().getElementById(link[1]).selectFirst(linkTargetSelector);
        }

        String value = valueExtractor != null ? valueExtractor.extract(actualTargetElt) : null;

        ContextMetadata meta = getContextMetadata(parent, previousOfType, value);

        Context[] descendants = new Context[0];
        if (descendantExtractors != null) {
            descendants = new Context[descendantExtractors.size()];

            for (int i = 0; i < descendantExtractors.size(); i++) {
                ElementContextExtractor descendant = descendantExtractors.get(i);
                descendants[i] = descendant.extractDescendantContext(actualTargetElt, meta, null);
            }
        }

        return Parser.buildContext(meta, value, descendants);
    }

    /**
     * @param parentElt      The element that defined the root context.
     * @param parent         The root context.
     * @param previousOfType The previous child of same type of the root context.
     * @return The descendant context opened by this extractor.
     */
    public Context extractDescendantContext(Element parentElt, ContextMetadata parent, ContextMetadata previousOfType) {
        Element targetElt = selector != null ? parentElt.select(selector).first() : parentElt;
        return extractInternal(targetElt, parent, previousOfType);
    }

    /**
     * @param e              The element to test.
     * @param parent         The parent context when this element was reached.
     * @param previousOfType The previous child of same type of the parent context.
     * @return The context opened by this extractor, or null if the selector does not match.
     */
    public Context extractRootContext(Element e, ContextMetadata parent, ContextMetadata previousOfType) {
        if (e.is(selector)) {
            return extractInternal(e, parent, previousOfType);
        }
        return null;
    }
}
