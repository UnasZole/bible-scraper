package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import com.github.unaszole.bible.writing.interfaces.BookWriter;
import org.crosswire.jsword.versification.BibleBook;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.function.Consumer;

public class UsfmBibleWriter implements BibleWriter {

    private final Path outFolder;
    private final PrintWriter outWriter;
    private int bookNb = 1;

    public UsfmBibleWriter(Path outFolder) {
        this.outFolder = outFolder;
        this.outWriter = null;
    }

    public UsfmBibleWriter(PrintWriter outWriter) {
        this.outWriter = outWriter;
        this.outFolder = null;
    }

    private BookWriter getBookWriter(BibleBook book) throws IOException {
        if(outFolder != null) {
            return new UsfmBookWriter(book, bookNb++, outFolder);
        }
        else {
            return new UsfmBookWriter(book, outWriter);
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
    public void close() {
        if(outWriter != null) {
            outWriter.flush();
            // Do not close the outWriter : it was provided externally, so it's the caller's job to manage it.
        }
    }
}
