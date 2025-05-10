package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.parsing.ContextReaderListBuilder;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.GenericContextExtractor;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
    public EvaluatorWrapper selector;
    /**
     * If provided, and if the "selector" points to an element with an "href" attribute (typically an "a"), then
     * this link target selector is evaluated from the target of the link to select another element.
     * Typically used to fetch note contents elsewhere in a page from a note reference link.
     */
    public EvaluatorWrapper linkTargetSelector;

    /**
     * See {@link ElementStringExtractor#op}.
     */
    public String op;

    /**
     * See {@link ElementStringExtractor#regexp}.
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

    /**
     * @param parsedElt      The element selected by the {@link ElementParser}.
     * @return The value for the new context.
     */
    @Override
    protected String extractValue(Element parsedElt, ContextualData contextualData) {
        if(op == null) {
            return null;
        }

        Element targetElt = selector != null ? parsedElt.select(selector.get(contextualData)).first() : parsedElt;

        Element actualTargetElt = targetElt;

        if (linkTargetSelector != null && actualTargetElt.hasAttr("href")) {
            String[] link = actualTargetElt.attr("href").split("#");
            if (!link[0].isEmpty()) {
                throw new IllegalArgumentException("Cannot follow link " + link + " as it's not local to the page");
            }
            actualTargetElt = targetElt.ownerDocument().getElementById(link[1]).selectFirst(linkTargetSelector.get(contextualData));
        }

        ElementStringExtractor stringExtractor = new ElementStringExtractor();
        stringExtractor.op = op;
        stringExtractor.regexp = regexp;

        return stringExtractor.extractString(actualTargetElt);
    }

    public static List<PositionBufferedParserCore.ContextReader> getReaders(List<ElementContextExtractor> contexts,
                                                                            final Element e,
                                                                            final ContextualData contextualData) {
        if(contexts != null && !contexts.isEmpty()) {
            final ContextReaderListBuilder builder = new ContextReaderListBuilder();
            contexts.forEach(ex -> ex.appendTo(builder, e, contextualData));
            return builder.build();
        }
        return List.of();
    }
}
