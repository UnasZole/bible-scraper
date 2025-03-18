package com.github.unaszole.bible.writing.interfaces;

public interface NoteTextWriter extends TextWriter {
    /**
     * Write a catchphrase, ie a quoted phrase for which the current note is being provided.
     * @param str The text.
     */
    void catchphraseQuote(String str);

    /**
     * Write an alternate translation, ie a different way to translate the text referenced by the note.
     * @param str The text.
     */
    void alternateTranslationQuote(String str);
}
