package com.github.unaszole.bible.scraping.generic.parsing.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.parsing.ContextReaderListBuilder;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextStackAware;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.jayway.jsonpath.JsonPath;

import java.util.Deque;
import java.util.List;

public class JsonNodeParser extends ContextStackAware {
    public JsonPath selector;
    public List<JsonNodeContextExtractor> contexts;

    private boolean nodeMatches(JsonParserProvider.JsonNodeWrapper node) {
        // Compute the list of nodes matching this json path from the root.
        // TODO : design a cache to avoid recomputing this list all the time.
        ArrayNode matchingNodes = selector.read(node.root, JsonConfig.JSON_PATH_CONFIG);

        // Check if the current node is among the results of the selector.
        for(JsonNode matchingNode: matchingNodes) {
            if(matchingNode == node.node) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param e The element to parse.
     * @param ancestorStack The stack of contexts at this point.
     * @param nextContextType The type of context we're asked to open.
     * @return True if this extractor can indeed open a context here, false otherwise.
     */
    private boolean canParse(JsonParserProvider.JsonNodeWrapper e, Deque<ContextMetadata> ancestorStack, ContextType nextContextType) {
        // If the first context of the sequence matches the requested type, and the context stack is valid.
        return contexts != null && !contexts.isEmpty() && contexts.get(0).type == nextContextType
                && isContextStackValid(ancestorStack) && nodeMatches(e);
    }

    public List<PositionBufferedParserCore.ContextReader> parse(final JsonParserProvider.JsonNodeWrapper e,
                                                                Deque<ContextMetadata> ancestorStack,
                                                                ContextType nextContextType,
                                                                ContextualData contextualData) {
        if(canParse(e, ancestorStack, nextContextType)) {
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
