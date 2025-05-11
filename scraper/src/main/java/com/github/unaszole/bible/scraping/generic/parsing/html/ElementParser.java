package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import org.jsoup.nodes.Element;

import java.util.List;

public class ElementParser extends ElementAndContextStackAware {

    /**
     * Instructions to extract a sequence of contexts from the selected HTML element.
     */
    public List<ElementContextExtractor> contexts;

    /**
     * @param e The element to parse.
     * @param ancestorStack The stack of contexts at this point.
     * @param nextContextType The type of context we're asked to open.
     * @return True if this extractor can indeed open a context here, false otherwise.
     */
    private boolean canParse(Element e, List<Context> ancestorStack, ContextType nextContextType,
                             ContextualData contextualData) {
        // If the first context of the sequence matches the requested type, and the context stack is valid.
        return contexts != null && !contexts.isEmpty() && contexts.get(0).type == nextContextType
                && areElementAndContextStackValid(e, ancestorStack, contextualData);
    }

    public List<PositionBufferedParserCore.ContextReader> parse(final Element e, List<Context> ancestorStack,
                                                                ContextType nextContextType,
                                                                ContextualData contextualData) {
        if(canParse(e, ancestorStack, nextContextType, contextualData)) {
            return ElementContextExtractor.getReaders(contexts, e, contextualData);
        }
        return null;
    }
}
