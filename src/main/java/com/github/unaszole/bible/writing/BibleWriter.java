package com.github.unaszole.bible.writing;

import org.crosswire.jsword.versification.BibleBook;

public interface BibleWriter {
    /**
     * Write a book.
     * @param book The book identifier.
     * @return A writer to write the book.
     */
    BookWriter book(BibleBook book);

    /**
     * Close the book. Must be called exactly once, and no other method called afterwards.
     */
    void closeBible();
}
