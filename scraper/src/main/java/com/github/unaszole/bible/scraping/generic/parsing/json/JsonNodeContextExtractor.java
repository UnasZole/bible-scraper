package com.github.unaszole.bible.scraping.generic.parsing.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.GenericContextExtractor;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class JsonNodeContextExtractor extends GenericContextExtractor<JsonParserProvider.JsonNodeWrapper> {
    private static final Logger LOG = LoggerFactory.getLogger(JsonNodeContextExtractor.class);

    /**
     * Path to a descendant field of this object that the value should be extracted from.
     * Should point to a leaf node (is a scalar value, string, boolean or integer). If several, the first will be used.
     * If unset, no value will be selected.
     */
    public JsonPath selector;

    /**
     * See {@link JsonNodeStringExtractor#regexp}.
     */
    public Pattern regexp;

    /**
     * If set, ignore the other fields and return this value directly.
     */
    public String literal;

    /**
     * Configuration to extract additional contexts as descendants of this one.
     * All these extractors are relative to the same object.
     */
    public List<JsonNodeContextExtractor> descendants;

    @Override
    protected List<? extends GenericContextExtractor<JsonParserProvider.JsonNodeWrapper>> getDescendants() {
        return descendants;
    }

    @Override
    protected String extractValue(JsonParserProvider.JsonNodeWrapper jsonNode, ContextualData contextualData) {
        if(literal != null) {
            return literal;
        }

        if(selector == null) {
            return null;
        }

        ArrayNode results = selector.read(jsonNode.node, JsonConfig.JSON_PATH_CONFIG);
        if(results.isEmpty()) {
            LOG.warn("Failed to find {} within {}", selector.getPath(), jsonNode.node);
        }
        JsonNode selectedNode = results.get(0);

        JsonNodeStringExtractor stringExtractor = new JsonNodeStringExtractor();
        stringExtractor.regexp = this.regexp;
        return stringExtractor.extractString(new JsonParserProvider.JsonNodeWrapper(jsonNode.root, selectedNode));
    }
}
