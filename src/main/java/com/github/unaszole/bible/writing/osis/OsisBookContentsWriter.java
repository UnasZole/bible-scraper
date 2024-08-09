package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.BufferedTextWrites;
import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;
import org.crosswire.jsword.versification.BibleBook;

import javax.xml.stream.XMLStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OsisBookContentsWriter extends OsisStructuredTextWriter
        implements StructuredTextWriter.BookContentsWriter {

    private final BibleBook book;
    private int currentChapter = -1;
    private String[] currentChapterSourceNb = null;
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

    private void openChapter(int chapterNb, String... sourceNb) {
        // Close the current chapter if any.
        closeCurrentChapter();

        this.currentChapter = chapterNb;
        this.currentChapterSourceNb = sourceNb;

        // <chapter sID>
        writeEmptyElement("chapter");
        writeAttribute("sID", getCurrentChapterMilestoneId());
        writeAttribute("osisID", getCurrentChapterOsisId());
        if(currentChapterSourceNb.length > 0) {
            writeAttribute("n", currentChapterSourceNb[0]);
        }
        // </chapter>
    }

    private void openVerse(int[] verseNbs, String... sourceNb) {
        // Close the current verse if any.
        closeCurrentVerse();

        this.currentVerse = verseNbs;

        // Make sure we're in an active paragraph for the verse start.
        ensureInActiveParagraph();

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
        // Open the new chapter.
        openChapter(chapterNb, sourceNb);
    }

    @Override
    public void chapterTitle(Consumer<TextWriter> writes) {
        // If there is a chapter title, then this chapter will be a new paragraph.
        closeCurrentParagraph();

        // <title type="chapter">
        writeStartElement("title");
        writeAttribute("type", "chapter");
        writeText(writes);
        writeEndElement();
        // </title>
    }

    @Override
    public void chapterIntro(Consumer<TextWriter> writes) {
        // If there is a chapter intro, then this chapter will be a new paragraph.
        closeCurrentParagraph();

        // <div type="introduction">
        writeStartElement("div");
        writeAttribute("type", "introduction");
        writeText(writes);
        writeEndElement();
        // </div>
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
