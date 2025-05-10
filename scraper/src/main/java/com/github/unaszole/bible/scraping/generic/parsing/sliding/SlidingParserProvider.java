package com.github.unaszole.bible.scraping.generic.parsing.sliding;

import com.github.unaszole.bible.parsing.ParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.TextParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

/**
 * This uses anchored rules to match the beginning of the remaining string, progressively consuming the string and thus
 * "sliding" towards the end.
 */
public class SlidingParserProvider implements TextParser.Provider<SlidingView> {

    public List<SlidingParserRule> rules;

    @Override
    public Iterator<SlidingView> iterate(InputStream input) throws IOException {
        return new SlidingView(new String(input.readAllBytes(), StandardCharsets.UTF_8));
    }

    @Override
    public ParserCore<SlidingView> getParser(ContextualData contextualData) {
        return new ConfiguredSlidingParser(rules, contextualData);
    }
}
