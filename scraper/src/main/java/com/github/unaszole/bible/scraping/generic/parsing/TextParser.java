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

public class TextParser {

    public interface Provider<Position> {
        Iterator<Position> iterate(InputStream input) throws IOException;
        ParserCore<Position> getParser(ContextualData contextualData);
    }

    public HtmlParserProvider html;
    public JsonParserProvider json;
    public SlidingParserProvider sliding;

    private Provider<?> getProvider() {
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
        return getLocalParser(getProvider(), input, currentContextStack, contextualData);
    }
}
