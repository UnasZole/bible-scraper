package com.github.unaszole.bible.scraping.generic.parsing.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.unaszole.bible.parsing.ParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.TextParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class JsonParserProvider implements TextParser.Provider<JsonParserProvider.JsonNodeWrapper> {

    public static class JsonNodeWrapper {
        public final JsonNode root;
        public final JsonNode node;

        public JsonNodeWrapper(JsonNode root, JsonNode node) {
            assert root != null && node != null;
            this.root = root;
            this.node = node;
        }
    }

    private static class NodeIterator implements Iterator<JsonNodeWrapper> {

        private final JsonNode rootNode;
        private final Deque<Iterator<JsonNode>> iteratorStack = new ArrayDeque<>();

        public NodeIterator(JsonNode rootNode) {
            this.rootNode = rootNode;
            iteratorStack.push(Collections.singleton(rootNode).iterator());
        }

        @Override
        public boolean hasNext() {
            return !iteratorStack.isEmpty();
        }

        @Override
        public JsonNodeWrapper next() {
            // Get the next node from the iterator on top of the stack.
            // Since we remove iterators from the stack as soon as they are exhausted, we're sure to have a next one.
            JsonNode nextNode = iteratorStack.peek().next();

            // Prepare to iterate over all this node's children afterwards.
            iteratorStack.push(nextNode.elements());

            // Remove the exhausted iterators from the stack.
            while(!iteratorStack.isEmpty() && !iteratorStack.peek().hasNext()) {
                iteratorStack.pop();
            }

            return new JsonNodeWrapper(rootNode, nextNode);
        }
    }

    public List<JsonNodeParser> nodes;
    public List<JsonNodeExternalParser> externalParsers;

    @Override
    public Iterator<JsonNodeWrapper> iterate(InputStream input) throws IOException {
        return new NodeIterator(JsonConfig.MAPPER.readTree(input));
    }

    @Override
    public ParserCore<JsonNodeWrapper> getParser(ContextualData contextualData) {
        return new ConfiguredJsonParser(nodes, externalParsers, contextualData);
    }
}
