package com.github.unaszole.bible.scraping.generic.parsing;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.parsing.ParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.html.HtmlParserProvider;
import com.github.unaszole.bible.scraping.generic.parsing.json.JsonParserProvider;
import com.github.unaszole.bible.scraping.generic.parsing.sliding.SlidingParserProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class TextParser {

    public interface Provider<Position> {
        Iterator<Position> iterate(InputStream input) throws IOException;
        ParserCore<Position> getParser(ContextualData contextualData);
    }

    public String ref;
    public HtmlParserProvider html;
    public JsonParserProvider json;
    public SlidingParserProvider sliding;

    private Provider<?> getProvider(Map<String, TextParser> namedParsers) {
        if(ref != null) {
            return Optional.ofNullable(namedParsers.get(ref))
                    .map(tp -> tp.getProvider(namedParsers))
                    .orElseThrow(() -> new RuntimeException("Could not find parser with name " + ref));
        }
        if(html != null) {
            return html;
        }
        if(json != null) {
            return json;
        }
        if(sliding != null) {
            return sliding;
        }
        throw new IllegalArgumentException("No parser provided !");
    }

    private <P> Parser<P> getLocalParser(Provider<P> provider, InputStream input,
                                        Deque<Context> currentContextStack, ContextualData contextualData) {
        try {
            return new Parser<>(provider.getParser(contextualData), provider.iterate(input), currentContextStack);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Parser<?> getLocalParser(InputStream input,
                                    Deque<Context> currentContextStack, ContextualData contextualData) {
        return getLocalParser(getProvider(contextualData.namedParsers), input, currentContextStack, contextualData);
    }
}
