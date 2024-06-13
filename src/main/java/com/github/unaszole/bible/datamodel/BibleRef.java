package com.github.unaszole.bible.datamodel;

import org.crosswire.jsword.versification.BibleBook;

public class BibleRef {
    public final BibleBook book;

    /**
     * The chapter number in standardised OSIS-compatible form, ie. the position of the chapter within the book,
     * starting at 1.
     * May be 0 when referring to a full book.
     */
    public final int chapter;

    /**
     * The verse numbers in standardised OSIS-compatible form, ie. the position of the verses within the book,
     * starting at 1.
     * May be 0 when referring to a full chapter or book.
     */
    public final int verse;

    public BibleRef(BibleBook book, int chapter, int verse) {
        this.book = book;
        this.chapter = chapter;
        this.verse = verse;
    }
}
