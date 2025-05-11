package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.TextParser;
import org.jsoup.nodes.TextNode;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Optional;

public class TextNodeExternalParser extends TextNodeAndContextStackAware {
    public TextParser textParser;

    public Optional<Parser<?>> getParserIfApplicable(TextNode n, Deque<Context> currentContextStack,
                                                     ContextualData contextualData) {
        if(isContextStackValid(currentContextStack)) {
            String text = extractText(n);
            if(text != null) {
                return Optional.of(textParser.getLocalParser(
                        new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
                        currentContextStack,
                        contextualData
                ));
            }
        }

        return Optional.empty();
    }
}
