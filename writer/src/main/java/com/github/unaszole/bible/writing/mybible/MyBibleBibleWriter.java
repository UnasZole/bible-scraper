package com.github.unaszole.bible.writing.mybible;

import com.github.unaszole.bible.writing.datamodel.DocumentMetadata;
import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import com.github.unaszole.bible.writing.interfaces.BookGroupWriter;
import com.github.unaszole.bible.writing.interfaces.BookWriter;
import org.crosswire.jsword.versification.BibleBook;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.function.Consumer;

public class MyBibleBibleWriter implements BibleWriter {

    private final VerseSink sink;

    public MyBibleBibleWriter(Path outSqlite, DocumentMetadata meta) throws SQLException, IOException {
        this.sink = new SQliteSink(outSqlite, meta);
    }

    public MyBibleBibleWriter(PrintWriter outWriter) {
        this.sink = new PrintSink(outWriter);
    }

    @Override
    public void bookGroup(Consumer<BookGroupWriter> writes) {
        // No support for book groups in MyBible : all contained books are passed-through.
        try(BookGroupWriter writer = new BookGroupWriter.Passthrough(this::book)) {
            writes.accept(writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void book(BibleBook book, Consumer<BookWriter> writes) {
        writes.accept(new MyBibleBookWriter(sink, book));
    }

    @Override
    public void close() throws Exception {
        this.sink.close();
    }
}
