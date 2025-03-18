package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import org.jsoup.nodes.Element;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class ConfiguredHtmlParser extends PositionBufferedParserCore<Element> {

    private final List<ElementParser> elements;

    private final List<ElementExternalParser> externalParsers;

    private final ContextualData contextualData;

    public ConfiguredHtmlParser(List<ElementParser> elements,
                                List<ElementExternalParser> externalParsers,
                                ContextualData contextualData) {
        this.elements = elements;
        this.externalParsers = externalParsers;
        this.contextualData = contextualData;
    }

    @Override
    public Parser<?> parseExternally(Element e, Deque<Context> currentContextStack) {
        if(externalParsers == null) {
            return null;
        }

        for(ElementExternalParser elementExternalParser : externalParsers) {
            Optional<Parser<?>> parser = elementExternalParser.getParserIfApplicable(e, currentContextStack, contextualData);
            if(parser.isPresent()) {
                return parser.get();
            }
        }

        // The current element did not trigger any parser.
        return null;
    }

    @Override
    public List<ContextReader> readContexts(Deque<ContextMetadata> ancestorStack, ContextType type,
                                  ContextMetadata previousOfType, Element e) {
        if(elements == null) {
            return List.of();
        }

        for(ElementParser eltParser: elements) {
            List<ContextReader> result = eltParser.parse(e, ancestorStack, type, contextualData);
            if(result != null) {
                return result;
            }
        }

        return List.of();
    }
}
