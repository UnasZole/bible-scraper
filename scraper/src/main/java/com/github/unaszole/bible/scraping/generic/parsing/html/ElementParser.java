package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.parsing.ContextReaderListBuilder;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextStackAware;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import org.jsoup.nodes.Element;

import java.util.Deque;
import java.util.List;

public class ElementParser extends ContextStackAware {

    /**
     * A selector used to test if an element can be parsed by this parser.
     */
    public EvaluatorWrapper selector;

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
    private boolean canParse(Element e, Deque<ContextMetadata> ancestorStack, ContextType nextContextType,
                             ContextualData contextualData) {
        // If the first context of the sequence matches the requested type, and the context stack is valid.
        return contexts != null && !contexts.isEmpty() && contexts.get(0).type == nextContextType
                && isContextStackValid(ancestorStack) && e.is(selector.get(contextualData));
    }

    public List<PositionBufferedParserCore.ContextReader> parse(final Element e, Deque<ContextMetadata> ancestorStack,
                                                                ContextType nextContextType,
                                                                ContextualData contextualData) {
        if(canParse(e, ancestorStack, nextContextType, contextualData)) {
            if(!contexts.isEmpty()) {
                final ContextReaderListBuilder builder = new ContextReaderListBuilder();
                contexts.forEach(ex -> ex.appendTo(builder, e, contextualData));
                return builder.build();
            }

            return List.of();
        }
        return null;
    }
}
