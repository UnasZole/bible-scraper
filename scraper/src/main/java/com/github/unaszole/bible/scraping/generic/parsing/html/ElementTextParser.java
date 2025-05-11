package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.TextParser;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Deque;

public class ElementTextParser extends TextParser {
    private static final Logger LOG = LoggerFactory.getLogger(ElementTextParser.class);

    public ElementStringExtractor stringExtractor;

    public Parser<?> getParser(Element element, Deque<Context> currentContextStack, ContextualData contextualData) {
        String str = stringExtractor.extractString(element);

        LOG.debug("Getting external parser for text : '{}'", str);

        return super.getLocalParser(
                new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)),
                currentContextStack,
                contextualData
        );
    }
}
