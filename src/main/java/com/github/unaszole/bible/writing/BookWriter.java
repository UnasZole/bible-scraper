package com.github.unaszole.bible.writing;

import java.util.function.Consumer;

public interface BookWriter extends AutoCloseable {
    /**
     * Write the title of the book.
     * @param title The title.
     * */
    void title(String title);

    /**
     * Write a book introduction.
     * @param writes Logic to write the introduction.
     */
    void introduction(Consumer<StructuredTextWriter.BookIntroWriter> writes);

    /**
     * Write the book contents.
     * @param writes Logic to write the book contents.
     */
    void contents(Consumer<StructuredTextWriter.BookContentsWriter> writes);
}
