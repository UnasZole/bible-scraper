package com.github.unaszole.bible.writing;

public interface BookWriter {
    /**
     * Write the title of the book.
     * @param title The title.
     * @return This writer to keep writing book contents.
     */
    BookWriter title(String title);

    /**
     * Write a book introduction.
     * @return A writer to write the introduction.
     */
    StructuredTextWriter.BookIntroWriter introduction();

    /**
     * Write the book contents.
     * @return A writer to write the book's contents.
     */
    StructuredTextWriter.BookContentsWriter contents();

    /**
     * Close this book.
     * @return The parent bible writer to keep writing, or null if no parent.
     */
    BibleWriter closeBook();
}
