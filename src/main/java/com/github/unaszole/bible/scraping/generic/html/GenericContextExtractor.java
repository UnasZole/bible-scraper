package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.ContextReaderListBuilder;
import com.github.unaszole.bible.scraping.ParsingUtils;

import java.util.List;

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

    protected ContextMetadata getContextMetadata(ContextMetadata parent, ContextMetadata previousOfType, String value) {
        switch (type) {
            case CHAPTER:
                return ParsingUtils.getChapterMetadata(parent, previousOfType, value);
            case VERSE:
                value = stripLeadingZeroes(value);
                return ParsingUtils.getVerseMetadata(parent, previousOfType, value);
            default:
                return new ContextMetadata(type, parent.book, parent.chapter, parent.verses);
        }
    }

    public void appendTo(ContextReaderListBuilder builder, Position position) {
        ContextReaderListBuilder descendantsBuilder = new ContextReaderListBuilder();
        if(getDescendants() != null) {
            for (GenericContextExtractor<Position> descendantExtractor : getDescendants()) {
                descendantExtractor.appendTo(descendantsBuilder, position);
            }
        }

        final String value = extractValue(position);

        builder.followedBy(
                (as, pot) -> getContextMetadata(as.peekFirst(), pot, value),
                value,
                descendantsBuilder
        );
    }
}
