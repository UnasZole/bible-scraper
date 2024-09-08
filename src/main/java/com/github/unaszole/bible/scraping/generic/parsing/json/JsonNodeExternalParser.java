package com.github.unaszole.bible.scraping.generic.parsing.json;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.scraping.Parser;
import com.github.unaszole.bible.scraping.generic.parsing.ContextStackAware;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;

import java.util.Deque;
import java.util.Optional;

public class JsonNodeExternalParser extends ContextStackAware {
    /**
     * If not null, invokes any text parser on the selected element's text contents.
     */
    public JsonNodeTextParser textParser;

    public Optional<Parser<?>> getParserIfApplicable(final JsonParserProvider.JsonNodeWrapper n,
                                                     final Deque<Context> currentContextStack,
                                                     final ContextualData contextualData) {

        if(textParser != null) {
            return Optional.of(textParser.getParser(n, currentContextStack, contextualData));
        }
        return Optional.empty();
    }
}
