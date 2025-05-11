package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.ContextReaderListBuilder;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.StringContextExtractor;
import org.jsoup.nodes.TextNode;

import java.util.List;

public class TextNodeParser extends TextNodeAndContextStackAware {

    /**
     * Instructions to extract a sequence of contexts from the parsed text node.
     */
    public List<StringContextExtractor> contexts;

    private boolean canParseInContext(List<Context> ancestorStack, ContextType nextContextType) {
        // If the first context of the sequence matches the requested type, and the context stack is valid.
        return contexts != null && !contexts.isEmpty() && contexts.get(0).type == nextContextType
                && isContextStackValid(ancestorStack);
    }

    public List<PositionBufferedParserCore.ContextReader> parse(TextNode n, List<Context> ancestorStack,
                                                                ContextType nextContextType,
                                                                ContextualData contextualData) {
        final String text = extractText(n);
        
        if(text != null && !text.isEmpty() && canParseInContext(ancestorStack, nextContextType)) {
            if(!contexts.isEmpty()) {
                final ContextReaderListBuilder builder = new ContextReaderListBuilder();
                contexts.forEach(ex -> ex.appendTo(builder, text, contextualData));
                return builder.build();
            }

            return List.of();
        }
        return null;
    }
}
