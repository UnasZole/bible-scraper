package com.github.unaszole.bible.datamodel;

public enum ContextMetadataType {
    NO_META(false, false, false),
    BOOK_LEVEL(true, false, false),
    CHAPTER_LEVEL(true, true, false),
    VERSE_LEVEL(true, true, true);

    public final boolean hasBook;
    public final boolean hasChapter;
    public final boolean hasVerses;

    ContextMetadataType(boolean hasBook, boolean hasChapter, boolean hasVerses) {
        this.hasBook = hasBook;
        this.hasChapter = hasChapter;
        this.hasVerses = hasVerses;
    }
}
