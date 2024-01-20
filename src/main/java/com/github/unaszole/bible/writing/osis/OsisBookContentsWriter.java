package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.BookWriter;
import com.github.unaszole.bible.writing.StructuredTextWriter;
import org.crosswire.jsword.versification.BibleBook;

import javax.xml.stream.XMLStreamWriter;

public class OsisBookContentsWriter extends OsisStructuredTextWriter<BookWriter, StructuredTextWriter.BookContentsWriter>
        implements StructuredTextWriter.BookContentsWriter {

    private final BibleBook book;
    private int currentChapter = -1;
    private int currentVerse = -1;

    public OsisBookContentsWriter(BookWriter parent, XMLStreamWriter xmlWriter, BibleBook book) {
        super(parent, xmlWriter);
        this.book = book;
    }

    private String getCurrentChapterOsisId() {
        return book.getOSIS() + "." + currentChapter;
    }

    private String getCurrentVerseOsisId() {
        return getCurrentChapterOsisId() + "." + currentVerse;
    }

    private void openVerse(int verseNb) {
        // Close the current verse if any.
        closeCurrentVerse();

        // Make sure we're in an active paragraph for the verse start.
        ensureInActiveParagraph();

        // <verse sID>
        writeStartElement("verse");
        writeAttribute("sID", getCurrentVerseOsisId());
        writeAttribute("osisID", getCurrentVerseOsisId());
        writeEndElement();
        // </verse>

        this.currentVerse = verseNb;
    }

    private void closeCurrentVerse() {
        if(currentVerse >= 0) {
            // <verse eID>
            writeStartElement("verse");
            writeAttribute("eID", getCurrentVerseOsisId());
            writeEndElement();
            // </verse>

            this.currentVerse = -1;
        }
    }

    private void openChapter(int chapterNb) {
        // Close the current chapter if any.
        closeCurrentChapter();

        this.currentChapter = chapterNb;

        // <chapter sID>
        writeStartElement("chapter");
        writeAttribute("sID", getCurrentChapterOsisId());
        writeAttribute("osisID", getCurrentChapterOsisId());
        writeEndElement();
        // </chapter>
    }

    private void closeCurrentChapter() {
        // Always close the verse before closing a chapter.
        closeCurrentVerse();

        if(currentChapter >= 0) {
            // <chapter eID>
            writeStartElement("chapter");
            writeAttribute("eID", getCurrentChapterOsisId());
            writeEndElement();
            // </chapter>

            this.currentChapter = -1;
        }
    }

    @Override
    public BookContentsWriter chapter(int chapterNb) {
        openChapter(chapterNb);
        return getThis();
    }

    @Override
    public BookContentsWriter chapter(int chapterNb, String chapterTitle) {
        openChapter(chapterNb);

        // <title type="chapter">
        writeStartElement("title");
        writeAttribute("type", "chapter");
        writeCharacters(chapterTitle);
        writeEndElement();
        // </title>

        return getThis();
    }

    @Override
    public BookContentsWriter verse(int verseNb) {
        openVerse(verseNb);
        return getThis();
    }

    @Override
    public BookWriter closeText() {
        closeCurrentChapter();
        return super.closeText();
    }

    @Override
    protected BookContentsWriter getThis() {
        return this;
    }
}
