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
    private boolean inPsalmTitle = false;

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
            if(inPsalmTitle) {
                // osis2mod does not support verse tags within titles.
                // So when we close a verse, we must close the title beforehand.
                // Following psalm title content will be in a separate title tag.
                // TODO : just remove this if when osis2mod is fixed, to generate an OSIS file with a single title.
                closeCurrentParagraph();
            }

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
    public void psalmTitle(Consumer<TextWriter> writes) {
        // If not yet in a psalm title, open it.
        if(!inPsalmTitle) {
            // A psalm title closes the previous paragraph.
            closeCurrentParagraph();

            // <title type="psalm" canonical="true">
            writeStartElement("title");
            writeAttribute("type", "psalm");
            writeAttribute("canonical", "true");
            if(currentVerse == null) {
                // If we're before the start of the first verse, use SWORD extension to mark this as pre-verse title.
                writeAttribute("subType", "x-preverse");
            }
            this.inPsalmTitle = true;
        }
        // Then write the text.
        writeText(writes);

        // Leave the psalm title opened in case there is a continuation for the title after a verse start.
    }

    @Override
    protected void closeCurrentParagraph() {
        if(inPsalmTitle) {
            // If we are in a psalm title, close it.

            writeEndElement();
            // </title>
            this.inPsalmTitle = false;
        }
        else {
            // Else, we close a regular paragraph.
            super.closeCurrentParagraph();
        }
    }

    @Override
    public void close() {
        closeCurrentChapter();
        super.close();
    }
}
