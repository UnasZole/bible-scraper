package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.interfaces.NoteTextWriter;

import java.io.PrintWriter;

public class UsfmNoteTextWriter extends UsfmTextWriter implements NoteTextWriter {
    public UsfmNoteTextWriter(PrintWriter out) {
        super(out);
    }

    @Override
    public void text(String str) {
        printTag("ft", str, false);
    }

    @Override
    public void catchphraseQuote(String str) {
        printTag("fq", str, false);
    }

    @Override
    public void alternateTranslationQuote(String str) {
        printTag("fqa", str, false);
    }

}
