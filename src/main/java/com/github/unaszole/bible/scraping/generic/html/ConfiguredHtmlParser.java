package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.Parser;
import org.jsoup.nodes.Element;

import java.util.Deque;
import java.util.List;

public class ConfiguredHtmlParser extends PositionBufferedParserCore<Element> {

    private final List<ElementParser> elements;

    private final List<NodeParserConfig> nodeParsers;

    public ConfiguredHtmlParser(List<ElementParser> elements,
                                List<NodeParserConfig> nodeParsers) {
        this.elements = elements;
        this.nodeParsers = nodeParsers;
    }

    @Override
    public Parser<?> parseExternally(Element e, Deque<Context> currentContextStack) {
        if(nodeParsers == null) {
            return null;
        }

        for(NodeParserConfig nodeParserConfig: nodeParsers) {
            if(nodeParserConfig.canTriggerAtPosition(e, currentContextStack)) {
                // The current element triggers an external node parser.
                return nodeParserConfig.getParser(e, currentContextStack);
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
            List<ContextReader> result = eltParser.parse(e, ancestorStack, type);
            if(result != null) {
                return result;
            }
        }

        return List.of();
    }
}
