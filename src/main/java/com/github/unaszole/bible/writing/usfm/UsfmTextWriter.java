package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.datamodel.BibleRef;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.io.PrintWriter;
import java.util.function.Consumer;

public class UsfmTextWriter implements TextWriter {

    protected final PrintWriter out;
    public UsfmTextWriter(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void text(String str) {
        out.print(str);
    }

    @Override
    public void translationAdd(String str) {
        out.print("\\add " + str + "\\add*");
    }

    @Override
    public void quote(String str) {
        out.print("\\qt " + str + "\\qt*");
    }

    private static String getUsfmRef(BibleRef ref) {
        return UsfmBookWriter.OSIS_TO_USFM.get(ref.book)
                + (ref.chapter == 0 ? "" : " " + ref.chapter
                + (ref.verse == 0 ? "" : ":" + ref.verse));
    }

    @Override
    public void reference(BibleRef rangeStart, BibleRef rangeEnd, String text) {
        out.print("\\xt " + text + "|" + getUsfmRef(rangeStart)
                + (rangeEnd != null && rangeEnd.verse > 0 ? "-" + rangeEnd.verse : "")
                + "\\xt*");
    }

    @Override
    public void note(Consumer<TextWriter> writes) {
        out.print("\\f + ");
        writes.accept(new UsfmNoteTextWriter(out));
        out.print("\\f*");
    }

    @Override
    public void close() {
        out.flush();
    }
}
