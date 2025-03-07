package com.github.unaszole.bible.scraping.generic.parsing;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.ValueType;
import com.github.unaszole.bible.scraping.ContextReaderListBuilder;
import com.github.unaszole.bible.scraping.ParsingUtils;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public abstract class GenericContextExtractor<Position> {
    /**
     * The type of contexts this extractor produces.
     */
    public ContextType type;

    protected abstract List<? extends GenericContextExtractor<Position>> getDescendants();

    protected abstract String extractValue(Position position);

    private static String stripLeadingZeroes(String parsedNb) {
        return parsedNb.length() == 1 ? parsedNb : parsedNb.replaceAll("^0*(.)", "$1");
    }

    protected ContextMetadata getContextMetadata(ContextMetadata parent, ContextMetadata previousOfType, Object value) {
        switch (type) {
            case CHAPTER:
                return ParsingUtils.getChapterMetadata(parent, previousOfType, (String) value);
            case VERSE:
                return ParsingUtils.getVerseMetadata(parent, previousOfType, stripLeadingZeroes((String)value));
            default:
                return new ContextMetadata(type, parent.book, parent.chapter, parent.verses);
        }
    }

    public void appendTo(ContextReaderListBuilder builder, Position position, ContextualData contextualData) {
        ContextReaderListBuilder descendantsBuilder = new ContextReaderListBuilder();
        if(getDescendants() != null) {
            for (GenericContextExtractor<Position> descendantExtractor : getDescendants()) {
                descendantExtractor.appendTo(descendantsBuilder, position, contextualData);
            }
        }

        String value = extractValue(position);
        final Object finalValue;
        switch(type.valueType) {
            case BOOK_ID:
                finalValue = Optional.<Object>ofNullable(contextualData.bookRefs.get(value)).orElse(value);
                break;
            case URI:
                finalValue = Optional.ofNullable(contextualData.baseUri)
                        .map(baseUri -> baseUri.resolve(URI.create(value)))
                        .orElse(URI.create(value));
                break;
            default:
                finalValue = value;
        }

        builder.followedBy(
                (as, pot) -> getContextMetadata(as.peekFirst(), pot, finalValue),
                finalValue,
                descendantsBuilder
        );
    }
}
