package com.github.unaszole.bible.writing;

import java.util.function.Consumer;

public interface BookWriter extends AutoCloseable {
    /**
     * Write the title of the book.
     * @param title The title.
     * @return This writer to keep writing book contents.
     */
    void title(String title);

    /**
     * Write a book introduction.
     * @return A writer to write the introduction.
     */
    void introduction(Consumer<StructuredTextWriter.BookIntroWriter> writes);

    /**
     * Write the book contents.
     * @return A writer to write the book's contents.
     */
    void contents(Consumer<StructuredTextWriter.BookContentsWriter> writes);
}
