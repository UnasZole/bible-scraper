package com.github.unaszole.bible.writing.interfaces;

import org.crosswire.jsword.versification.BibleBook;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface BookGroupWriter extends AutoCloseable {

    /**
     * For output formats which do not suupport the notion of bookGroup, this implementation allows delegating easily
     * to the book implementation.
     */
    class Passthrough implements BookGroupWriter {

        private final BiConsumer<BibleBook, Consumer<BookWriter>> bookWriter;

        public Passthrough(BiConsumer<BibleBook, Consumer<BookWriter>> bookWriter) {
            this.bookWriter = bookWriter;
        }

        @Override
        public void title(String title) {
            // No support for book group title.
        }

        @Override
        public void introduction(Consumer<StructuredTextWriter.BookIntroWriter> writes) {
            // No support for book group intro.
        }

        @Override
        public void book(BibleBook book, Consumer<BookWriter> writes) {
            bookWriter.accept(book, writes);
        }

        @Override
        public void close() throws Exception {
            // Nothing specific to do on close.
        }
    }

    /**
     * Write the title of the book group.
     * @param title The title.
     * */
    void title(String title);

    /**
     * Write a book group introduction.
     * @param writes Logic to write the introduction.
     */
    void introduction(Consumer<StructuredTextWriter.BookIntroWriter> writes);

    /**
     * Write a book.
     * @param book The book identifier.
     * @param writes Logic to write the book.
     */
    void book(BibleBook book, Consumer<BookWriter> writes);
}
