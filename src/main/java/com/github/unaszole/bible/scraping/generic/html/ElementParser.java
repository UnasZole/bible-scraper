package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.ContextReaderListBuilder;
import com.github.unaszole.bible.scraping.PositionBufferedParserCore;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;

import java.util.Deque;
import java.util.List;

public class ElementParser extends ContextStackAware {

    /**
     * A selector used to test if an element can be parsed by this parser.
     */
    public Evaluator selector;

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
    private boolean canParse(Element e, Deque<ContextMetadata> ancestorStack, ContextType nextContextType) {
        // If the first context of the sequence matches the requested type, and the context stack is valid.
        return contexts != null && !contexts.isEmpty() && contexts.get(0).type == nextContextType
                && isContextStackValid(ancestorStack) && e.is(selector);
    }

    public List<PositionBufferedParserCore.ContextReader> parse(final Element e, Deque<ContextMetadata> ancestorStack,
                                                                ContextType nextContextType) {
        if(canParse(e, ancestorStack, nextContextType)) {
            if(!contexts.isEmpty()) {
                final ContextReaderListBuilder builder = new ContextReaderListBuilder();
                contexts.forEach(ex -> ex.appendTo(builder, e));
                return builder.build();
            }

            return List.of();
        }
        return null;
    }
}
