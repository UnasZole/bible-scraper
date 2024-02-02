package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import org.crosswire.jsword.versification.BibleBook;

import javax.xml.stream.XMLStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OsisBookContentsWriter extends OsisStructuredTextWriter
        implements StructuredTextWriter.BookContentsWriter {

    private final BibleBook book;
    private int currentChapter = -1;
    private String[] currentChapterSourceNb = null;
    private boolean inActiveChapter = false;
    private String pendingChapterTitle = null;
    private int[] currentVerse = null;

    public OsisBookContentsWriter(XMLStreamWriter xmlWriter, BibleBook book) {
        super(xmlWriter);
        this.book = book;
    }

    private String getCurrentChapterOsisId() {
        return book.getOSIS() + "." + currentChapter;
    }
    private String getCurrentChapterMilestoneId() {
        return getCurrentChapterOsisId() + (currentChapterSourceNb.length > 0 ?
                "-aka-" + String.join("-", currentChapterSourceNb)
                : ""
        );
    }

    private List<String> getCurrentVerseOsisIds() {
        return Arrays.stream(currentVerse)
                .mapToObj(v -> getCurrentChapterOsisId() + "." + v)
                .collect(Collectors.toList());
    }
    private String getCurrentVerseMilestoneId() {
        return getCurrentVerseOsisIds().get(0);
    }

    private void writeChapterTitle(String title) {
        // <title type="chapter">
        writeStartElement("title");
        writeAttribute("type", "chapter");
        writeCharacters(title);
        writeEndElement();
        // </title>
    }

    private void openChapter() {
        // <chapter sID>
        writeEmptyElement("chapter");
        writeAttribute("sID", getCurrentChapterMilestoneId());
        writeAttribute("osisID", getCurrentChapterOsisId());
        if(currentChapterSourceNb.length > 0) {
            writeAttribute("n", currentChapterSourceNb[0]);
        }
        // </chapter>

        this.inActiveChapter = true;

        if(pendingChapterTitle != null) {
            writeChapterTitle(pendingChapterTitle);
            pendingChapterTitle = null;
        }
    }
    protected final void ensureInActiveChapter() {
        if(!inActiveChapter) {
            // If we're not in a chapter, or an inactive one, we need to open a new chapter.
            openChapter();
        }
    }

    private void openVerse(int[] verseNbs, String... sourceNb) {
        // Close the current verse if any.
        closeCurrentVerse();

        this.currentVerse = verseNbs;

        // Make sure we're in an active paragraph and chapter for the verse start.
        ensureInActiveParagraph();
        ensureInActiveChapter();

        // <verse sID>
        writeEmptyElement("verse");
        writeAttribute("sID", getCurrentVerseMilestoneId());
        writeAttribute("osisID", String.join(" ", getCurrentVerseOsisIds()));
        if(sourceNb.length > 0) {
            writeAttribute("n", sourceNb[0]);
        }
        // </verse>
    }

    private void closeCurrentVerse() {
        if(currentVerse != null) {
            // <verse eID>
            writeEmptyElement("verse");
            writeAttribute("eID", getCurrentVerseMilestoneId());
            // </verse>

            this.currentVerse = null;
        }
    }

    private void closeCurrentChapter() {
        // Always close the verse before closing a chapter.
        closeCurrentVerse();

        if(currentChapter >= 0) {
            // <chapter eID>
            writeEmptyElement("chapter");
            writeAttribute("eID", getCurrentChapterMilestoneId());
            // </chapter>

            this.currentChapter = -1;
            this.currentChapterSourceNb = null;
        }
    }

    @Override
    public void chapter(int chapterNb, String... sourceNb) {
        // Close the current chapter if any.
        closeCurrentChapter();

        // Set the new chapter number, but inactive yet.
        // (To be printed on first verse).
        this.currentChapter = chapterNb;
        this.currentChapterSourceNb = sourceNb;
        this.inActiveChapter = false;
    }

    @Override
    public void chapterTitle(String title) {
        if(inActiveChapter) {
            writeChapterTitle(title);
        }
        else {
            this.pendingChapterTitle = title;
        }
    }

    @Override
    public void verse(int[] verseNbs, String... sourceNb) {
        openVerse(verseNbs, sourceNb);
    }

    @Override
    public void close() {
        closeCurrentChapter();
        super.close();
    }
}
