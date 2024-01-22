package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.BookWriter;
import com.github.unaszole.bible.writing.StructuredTextWriter;
import org.crosswire.jsword.versification.BibleBook;

import javax.xml.stream.XMLStreamWriter;

public class OsisBookContentsWriter extends OsisStructuredTextWriter<BookWriter, StructuredTextWriter.BookContentsWriter>
        implements StructuredTextWriter.BookContentsWriter {

    private final BibleBook book;
    private int currentChapter = -1;
    private boolean inActiveChapter = false;
    private String pendingChapterTitle = null;
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

    private void writeChapterTitle(String title) {
        // <title type="chapter">
        writeStartElement("title");
        writeAttribute("type", "chapter");
        writeCharacters(title);
        writeEndElement();
        // </title>
    }

    private void openChapter(int chapterNb) {
        // <chapter sID>
        writeEmptyElement("chapter");
        writeAttribute("sID", getCurrentChapterOsisId());
        writeAttribute("osisID", getCurrentChapterOsisId());
        // </chapter>

        this.inActiveChapter = true;

        if(pendingChapterTitle != null) {
            writeChapterTitle(pendingChapterTitle);
            pendingChapterTitle = null;
        }
    }
    protected final void ensureInActiveChapter() {
        if(!inActiveChapter) {
            // If we're not in a paragraph, or an inactive one, we need to open a new paragraph.
            openChapter(currentChapter);
        }
    }

    private void openVerse(int verseNb) {
        // Close the current verse if any.
        closeCurrentVerse();

        this.currentVerse = verseNb;

        // Make sure we're in an active paragraph and chapter for the verse start.
        ensureInActiveParagraph();
        ensureInActiveChapter();

        // <verse sID>
        writeEmptyElement("verse");
        writeAttribute("sID", getCurrentVerseOsisId());
        writeAttribute("osisID", getCurrentVerseOsisId());
        // </verse>
    }

    private void closeCurrentVerse() {
        if(currentVerse >= 0) {
            // <verse eID>
            writeEmptyElement("verse");
            writeAttribute("eID", getCurrentVerseOsisId());
            // </verse>

            this.currentVerse = -1;
        }
    }

    private void closeCurrentChapter() {
        // Always close the verse before closing a chapter.
        closeCurrentVerse();

        if(currentChapter >= 0) {
            // <chapter eID>
            writeEmptyElement("chapter");
            writeAttribute("eID", getCurrentChapterOsisId());
            // </chapter>

            this.currentChapter = -1;
        }
    }

    @Override
    public BookContentsWriter chapter(int chapterNb) {
        // Close the current chapter if any.
        closeCurrentChapter();

        // Set the new chapter number, but inactive yet.
        // (To be printed on first verse).
        this.currentChapter = chapterNb;
        this.inActiveChapter = false;

        return getThis();
    }

    @Override
    public BookContentsWriter chapterTitle(String title) {
        if(inActiveChapter) {
            writeChapterTitle(title);
        }
        else {
            this.pendingChapterTitle = title;
        }
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
