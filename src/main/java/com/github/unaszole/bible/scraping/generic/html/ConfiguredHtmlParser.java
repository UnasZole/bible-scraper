package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.Parser;
import org.jsoup.nodes.Element;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class ConfiguredHtmlParser extends PositionBufferedParserCore<Element> {

    private final List<ElementParser> elements;

    private final List<NodeParserConfig> nodeParsers;

    private final ContextualData contextualData;

    public ConfiguredHtmlParser(List<ElementParser> elements,
                                List<NodeParserConfig> nodeParsers,
                                ContextualData contextualData) {
        this.elements = elements;
        this.nodeParsers = nodeParsers;
        this.contextualData = contextualData;
    }

    @Override
    public Parser<?> parseExternally(Element e, Deque<Context> currentContextStack) {
        if(nodeParsers == null) {
            return null;
        }

        for(NodeParserConfig nodeParserConfig: nodeParsers) {
            Optional<Parser<?>> parser = nodeParserConfig.getParserIfApplicable(e, currentContextStack, contextualData);
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
            return null;
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
