package com.github.unaszole.bible.writing.interfaces;

import com.github.unaszole.bible.writing.datamodel.BibleRef;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface TextWriter extends AutoCloseable {
    /**
     * Write text contents.
     * @param str The text.
     */
    void text(String str);

    /**
     * Write a translation addition.
     * @param str The text added by the translator.
     */
    void translationAdd(String str);

    /**
     * Write a quote.
     * @param str The quoted text.
     */
    void quote(String str);

    /**
     * Write a quote from the old testament.
     * @param str The quoted text.
     */
    void oldTestamentQuote(String str);

    /**
     * Write a speaker identification.
     * @param str The speaker's identification.
     */
    void speaker(String str);

    /**
     * Write a reference to another portion of the text.
     * @param rangeStart The first verse of the range being referenced.
     * @param rangeEnd The last verse of the range being referenced - leave null if no range.
     * @param text The text contents of this reference.
     */
    void reference(BibleRef rangeStart, BibleRef rangeEnd, String text);

    /**
     * Write a link to an external resource.
     * @param uri The URI referencing the resource.
     * @param text The text contents of this link.
     */
    void link(URI uri, String text);

    /**
     * Write a figure.
     * @param filename The file name of the attached image.
     * @param bytes Accessor for the bytes composing the attached image.
     * @param alt Alternative text to be rendered for users that can't view the image (or null).
     * @param caption Caption for the figure (or null).
     */
    void figure(String filename, Supplier<byte[]> bytes, String alt, String caption);

    /**
     * Write a note.
     * @param writes Logic to write the contents of the note.
     */
    void note(Consumer<NoteTextWriter> writes);
}
