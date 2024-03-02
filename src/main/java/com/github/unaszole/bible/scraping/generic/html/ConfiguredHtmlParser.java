package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.Parser;
import org.jsoup.nodes.Element;

import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfiguredHtmlParser extends Parser.TerminalParser<Element> {

    private final List<ElementContextExtractor> parserConfig;

    private List<NodeParserConfig> nodeParserConfigs;

    public ConfiguredHtmlParser(List<ElementContextExtractor> parserConfig,
                                List<NodeParserConfig> nodeParserConfigs,
                                Stream<Element> docStream, Context rootContext) {
        super(docStream.iterator(), rootContext);
        this.parserConfig = parserConfig;
        this.nodeParserConfigs = nodeParserConfigs;
    }

    @Override
    protected Parser<?> parseExternally(Element e, Deque<Context> currentContextStack) {
        if(nodeParserConfigs == null) {
            return null;
        }

        for(NodeParserConfig nodeParserConfig: nodeParserConfigs) {
            if(nodeParserConfig.canTriggerAtPosition(e, currentContextStack)) {
                // The current element triggers an external node parser.
                return nodeParserConfig.getParser(e, currentContextStack);
            }
        }

        // The current element did not trigger any parser.
        return null;
    }

    @Override
    protected Context readContext(Deque<ContextMetadata> ancestorStack, ContextType type,
                                  ContextMetadata previousOfType, Element e) {
        if(parserConfig == null) {
            return null;
        }

        List<ElementContextExtractor> extractors = parserConfig.stream()
                .filter(ex -> ex.canOpenContextAt(ancestorStack, type))
                .collect(Collectors.toList());
        for(ElementContextExtractor extractor: extractors) {
            Context out = extractor.extractRootContext(e, ancestorStack.peekFirst(), previousOfType);
            if(out != null) {
                return out;
            }
        }
        return null;
    }
}
