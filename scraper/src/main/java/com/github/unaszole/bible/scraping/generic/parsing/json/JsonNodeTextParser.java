package com.github.unaszole.bible.scraping.generic.parsing.json;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.TextParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Deque;

public class JsonNodeTextParser extends TextParser {
    private static final Logger LOG = LoggerFactory.getLogger(JsonNodeTextParser.class);

    public JsonNodeStringExtractor stringExtractor;

    public Parser<?> getParser(JsonParserProvider.JsonNodeWrapper node,
                               Deque<Context> currentContextStack, ContextualData contextualData) {
        String str = stringExtractor.extractString(node);

        LOG.debug("Getting external parser for text : '{}'", str);

        return super.getLocalParser(
                new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)),
                currentContextStack,
                contextualData
        );
    }
}
