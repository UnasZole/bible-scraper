package com.github.unaszole.bible.scraping.generic.parsing;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.ContextReaderListBuilder;
import com.github.unaszole.bible.scraping.ScrapingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class GenericContextExtractor<Position> {
    private static final Logger LOG = LoggerFactory.getLogger(GenericContextExtractor.class);

    /**
     * The type of contexts this extractor produces.
     */
    public ContextType type;

    protected abstract List<? extends GenericContextExtractor<Position>> getDescendants();

    protected abstract String extractValue(Position position, ContextualData contextualData);

    private static String stripLeadingZeroes(String parsedNb) {
        return parsedNb.length() == 1 ? parsedNb : parsedNb.replaceAll("^0*(.)", "$1");
    }

    protected ContextMetadata getContextMetadata(List<Context> ancestorStack, ContextMetadata previousOfType, Object value) {
        if(type == BibleContainers.CHAPTER) {
            return ScrapingUtils.getChapterMetadata(ancestorStack, previousOfType, (String) value);
        }
        if(type == BibleContainers.VERSE) {
            return ScrapingUtils.getVerseMetadata(ancestorStack, previousOfType, stripLeadingZeroes((String)value));
        }
        return new ContextMetadata(type);
    }

    public void appendTo(ContextReaderListBuilder builder, Position position, ContextualData contextualData) {
        ContextReaderListBuilder descendantsBuilder = new ContextReaderListBuilder();
        if(getDescendants() != null) {
            for (GenericContextExtractor<Position> descendantExtractor : getDescendants()) {
                descendantExtractor.appendTo(descendantsBuilder, position, contextualData);
            }
        }

        String value = extractValue(position, contextualData);
        final Object finalValue = contextualData.mapper.mapToValue(type.valueType(), value);

        builder.followedBy(
                (as, pot) -> getContextMetadata(as, pot, finalValue),
                finalValue,
                descendantsBuilder
        );
    }
}
