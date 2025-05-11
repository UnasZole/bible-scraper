package com.github.unaszole.bible.scraping.generic.parsing.json;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class ConfiguredJsonParser extends PositionBufferedParserCore<JsonParserProvider.JsonNodeWrapper> {

    private final List<JsonNodeParser> nodes;
    private final List<JsonNodeExternalParser> externalParsers;

    private final ContextualData contextualData;

    public ConfiguredJsonParser(List<JsonNodeParser> nodes, List<JsonNodeExternalParser> externalParsers,
                                ContextualData contextualData) {
        this.nodes = nodes;
        this.externalParsers = externalParsers;
        this.contextualData = contextualData;
    }

    @Override
    public Parser<?> parseExternally(JsonParserProvider.JsonNodeWrapper e, Deque<Context> currentContextStack) {
        if(externalParsers == null) {
            return null;
        }

        for(JsonNodeExternalParser elementExternalParser : externalParsers) {
            Optional<Parser<?>> parser = elementExternalParser.getParserIfApplicable(e, currentContextStack, contextualData);
            if(parser.isPresent()) {
                return parser.get();
            }
        }

        // The current element did not trigger any parser.
        return null;
    }

    @Override
    public List<ContextReader> readContexts(List<Context> ancestorStack, ContextType type,
                                            ContextMetadata previousOfType, JsonParserProvider.JsonNodeWrapper e) {
        if(nodes == null) {
            return List.of();
        }

        for(JsonNodeParser eltParser: nodes) {
            List<ContextReader> result = eltParser.parse(e, ancestorStack, type, contextualData);
            if(result != null) {
                return result;
            }
        }

        return List.of();
    }
}