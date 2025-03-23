package com.github.unaszole.bible.writing.mybible;

import com.github.unaszole.bible.writing.datamodel.BibleRef;
import com.github.unaszole.bible.writing.interfaces.NoteTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.net.URI;
import java.util.function.Consumer;

public class MyBibleTextWriter implements TextWriter {

    protected final StringBuilder outText;

    public MyBibleTextWriter(StringBuilder outText) {
        this.outText = outText;
    }

    @Override
    public void text(String str) {
        outText.append(str);
    }

    @Override
    public void translationAdd(String str) {
        outText.append("<FI>");
        outText.append(str);
        outText.append("<Fi>");
    }

    @Override
    public void quote(String str) {
        // No specific markup.
        outText.append(str);
    }

    @Override
    public void oldTestamentQuote(String str) {
        outText.append("<FO>");
        outText.append(str);
        outText.append("<Fo>");
    }

    @Override
    public void speaker(String str) {
        // No specific markup.
        outText.append(str);
    }

    @Override
    public void reference(BibleRef rangeStart, BibleRef rangeEnd, String text) {
        int bookNb = MyBibleBookWriter.OSIS_TO_MYBIBLE.get(rangeStart.book);
        outText.append(text)
                .append("<RX")
                .append(bookNb).append(".").append(rangeStart.chapter).append(".").append(rangeStart.verse)
                .append(rangeEnd != null ? "-" + rangeEnd.verse : "")
                .append(">");
    }

    @Override
    public void link(URI uri, String text) {
        outText.append("<a href=\"")
                .append(uri.toString())
                .append("\">")
                .append(text)
                .append("</a>");
    }

    @Override
    public void note(Consumer<NoteTextWriter> writes) {
        outText.append("<RF>");
        writes.accept(new MyBibleNoteTextWriter(outText));
        outText.append("<Rf>");
    }

    @Override
    public void close() throws Exception {

    }
}
