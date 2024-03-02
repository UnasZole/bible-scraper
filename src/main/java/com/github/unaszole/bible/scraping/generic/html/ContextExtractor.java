package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.ParsingUtils;

import java.util.Deque;

public abstract class ContextExtractor {
    /**
     * The type of contexts this extractor produces.
     */
    public ContextType type;
    /**
     * A required ancestor type for this extractor to trigger. Only relevant for a root extractor.
     */
    public ContextType withAncestor;
    /**
     * An excluded ancestor type for this extractor to trigger. Only relevant for a root extractor.
     */
    public ContextType withoutAncestor;

    /**
     *
     * @param ancestorStack The stack of contexts at this point.
     * @param type The type of context we're asked to open.
     * @return True if this extractor can indeed open a context here, false otherwise.
     */
    public boolean canOpenContextAt(Deque<ContextMetadata> ancestorStack, ContextType type) {
        return this.type == type &&
                (this.withAncestor == null || ancestorStack.stream().anyMatch(a -> a.type == this.withAncestor)) &&
                (this.withoutAncestor == null || ancestorStack.stream().noneMatch(a -> a.type == this.withoutAncestor));
    }

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
}
