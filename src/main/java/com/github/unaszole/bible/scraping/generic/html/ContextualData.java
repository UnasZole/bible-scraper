package com.github.unaszole.bible.scraping.generic.html;

import org.crosswire.jsword.versification.BibleBook;

import java.util.Map;

public class ContextualData {
    public final Map<String, BibleBook> bookRefs;

    public ContextualData(Map<String, BibleBook> bookRefs) {
        this.bookRefs = bookRefs;
    }
}
