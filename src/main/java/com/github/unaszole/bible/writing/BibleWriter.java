package com.github.unaszole.bible.writing;

import org.crosswire.jsword.versification.BibleBook;

import java.util.function.Consumer;

public interface BibleWriter extends AutoCloseable {
    /**
     * Write a book.
     * @param book The book identifier.
     * @return A writer to write the book.
     */
    void book(BibleBook book, Consumer<BookWriter> writes);
}
