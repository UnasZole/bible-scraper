package com.github.unaszole.bible.writing.mybible;

import com.github.unaszole.bible.writing.interfaces.NoteTextWriter;

public class MyBibleNoteTextWriter extends MyBibleTextWriter implements NoteTextWriter {
    public MyBibleNoteTextWriter(StringBuilder outText) {
        super(outText);
    }

    @Override
    public void catchphraseQuote(String str) {
        // No specific markup.
        outText.append(str);
    }

    @Override
    public void alternateTranslationQuote(String str) {
        // No specific markup.
        outText.append(str);
    }
}
