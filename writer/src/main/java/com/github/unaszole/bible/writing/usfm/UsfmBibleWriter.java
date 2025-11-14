package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.OutputContainer;
import com.github.unaszole.bible.writing.OutputDirectory;
import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import com.github.unaszole.bible.writing.interfaces.BookGroupWriter;
import com.github.unaszole.bible.writing.interfaces.BookWriter;
import org.crosswire.jsword.versification.BibleBook;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.function.Consumer;

public class UsfmBibleWriter implements BibleWriter {

    private final OutputContainer output;
    private int bookNb = 1;

    public UsfmBibleWriter(OutputContainer output) {
        this.output = output;
    }

    private BookWriter getBookWriter(BibleBook book) {
        return new UsfmBookWriter(book, bookNb++, output);
    }

    @Override
    public void bookGroup(Consumer<BookGroupWriter> writes) {
        // No support for book groups in USFM : all contained books are passed-through.
        try(BookGroupWriter writer = new BookGroupWriter.Passthrough(this::book)) {
            writes.accept(writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void book(BibleBook book, Consumer<BookWriter> writes) {
        try(BookWriter bookWriter = getBookWriter(book)) {
            writes.accept(bookWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        output.close();
    }
}
