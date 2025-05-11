package com.github.unaszole.bible.scraping.generic.parsing.sliding;

import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.ContextReaderListBuilder;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextStackAware;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.StringContextExtractor;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlidingParserRule extends ContextStackAware {

    public static class MatchResult {
        public final String fullMatch;
        public final List<PositionBufferedParserCore.ContextReader> contexts;

        private MatchResult(String fullMatch, List<PositionBufferedParserCore.ContextReader> contexts) {
            this.fullMatch = fullMatch;
            this.contexts = contexts;
        }
    }

    public Pattern regexp;

    public List<StringContextExtractor> contexts;

    /**
     * @param str The string at the current position.
     * @param ancestorStack The stack of contexts at this point.
     * @param nextContextType The type of context we're asked to open.
     * @return The matched string if match occurred, empty optional otherwise.
     */
    private Optional<String> match(CharSequence str, List<Context> ancestorStack, ContextType nextContextType) {
        if(contexts == null || contexts.isEmpty() || contexts.get(0).type != nextContextType) {
            // If rule can't build the requested context, no match.
            return Optional.empty();
        }

        if(!isContextStackValid(ancestorStack)) {
            // If context type is invalid, no match.
            return Optional.empty();
        }

        if(regexp == null) {
            // Catch-all rule : return the full string.
            return Optional.of(str.toString());
        }

        Matcher matcher = regexp.matcher(str);
        if(matcher.lookingAt()) {
            // If the beginning of the string matches the pattern, return the portion of the string that matched.
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }

    public Optional<MatchResult> parse(final CharSequence remaining, List<Context> ancestorStack,
                                                                ContextType nextContextType,
                                                                ContextualData contextualData) {
        return match(remaining, ancestorStack, nextContextType)
                .map(fullMatch -> {
                    final ContextReaderListBuilder builder = new ContextReaderListBuilder();
                    contexts.forEach(ex -> ex.appendTo(builder, fullMatch, contextualData));
                    return new MatchResult(fullMatch, builder.build());
                });
    }
}
