package com.github.unaszole.bible.writing.interfaces;

public interface TextWriter extends AutoCloseable {
    /**
     * Write text contents.
     * @param str The text.
     */
    void text(String str);

    /**
     * Write a note.
     * @param str The text of the note.
     */
    void note(String str);
}
