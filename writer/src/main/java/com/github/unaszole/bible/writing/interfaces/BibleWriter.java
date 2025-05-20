package com.github.unaszole.bible.writing.interfaces;

import org.crosswire.jsword.versification.BibleBook;

import java.util.function.Consumer;

public interface BibleWriter extends AutoCloseable {

    /**
     * Write a book group in this bible.
     * @param writes Logic to write the book groups.
     */
    void bookGroup(Consumer<BookGroupWriter> writes);

    /**
     * Write a book outside of any group.
     * @param book The book identifier.
     * @param writes Logic to write the book.
     */
    void book(BibleBook book, Consumer<BookWriter> writes);
}
