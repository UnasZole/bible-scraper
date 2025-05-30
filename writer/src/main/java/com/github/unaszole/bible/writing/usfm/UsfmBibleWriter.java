package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import com.github.unaszole.bible.writing.interfaces.BookGroupWriter;
import com.github.unaszole.bible.writing.interfaces.BookWriter;
import org.crosswire.jsword.versification.BibleBook;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class UsfmBibleWriter implements BibleWriter {

    private final Path outFolder;
    private final PrintWriter outWriter;
    private int bookNb = 1;

    private static void validateEmptyDir(Path path) throws IOException {
        if(!Files.isDirectory(path)) {
            throw new IllegalArgumentException(path + " must be an (empty) directory.");
        }

        try(Stream<Path> children = Files.list(path)) {
            if(children.findAny().isPresent()) {
                throw new IllegalArgumentException(path + " directory must be empty.");
            }
        }
    }

    public UsfmBibleWriter(Path outFolder) throws IOException {
        validateEmptyDir(outFolder);
        this.outFolder = outFolder;
        this.outWriter = null;
    }

    public UsfmBibleWriter(PrintWriter outWriter) {
        this.outWriter = outWriter;
        this.outFolder = null;
    }

    private BookWriter getBookWriter(BibleBook book) {
        if(outFolder != null) {
            try {
                return new UsfmBookWriter(book, bookNb++, outFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            return new UsfmBookWriter(book, outWriter);
        }
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
    public void close() {
        if(outWriter != null) {
            outWriter.flush();
            // Do not close the outWriter : it was provided externally, so it's the caller's job to manage it.
        }
    }
}
