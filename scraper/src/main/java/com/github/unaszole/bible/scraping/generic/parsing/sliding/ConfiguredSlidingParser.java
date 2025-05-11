package com.github.unaszole.bible.scraping.generic.parsing.sliding;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;

import java.util.List;
import java.util.Optional;

public class ConfiguredSlidingParser extends PositionBufferedParserCore<SlidingView> {

    private final List<SlidingParserRule> rules;

    private final ContextualData contextualData;

    public ConfiguredSlidingParser(List<SlidingParserRule> rules, ContextualData contextualData) {
        this.rules = rules;
        this.contextualData = contextualData;
    }

    @Override
    protected List<ContextReader> readContexts(List<Context> ancestorStack, ContextType type,
                                               ContextMetadata previousOfType,
                                               SlidingView view) {
        // Evaluations will refer to a slice starting at the current position in the full contents buffer.
        CharSequence remaining = view.getRemaining();

        for(SlidingParserRule rule: rules) {
            Optional<SlidingParserRule.MatchResult> result = rule.parse(remaining, ancestorStack, type, contextualData);
            if(result.isPresent()) {
                // We matched a result : advance the view to skip the matched string.
                view.consume(result.get().fullMatch.length());
                return result.get().contexts;
            }
        }

        // No result : let it auto-advance.
        return List.of();
    }
}
