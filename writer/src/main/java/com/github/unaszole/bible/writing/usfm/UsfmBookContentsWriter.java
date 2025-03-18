package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UsfmBookContentsWriter extends UsfmStructuredTextWriter implements StructuredTextWriter.BookContentsWriter {
    private boolean inPsalmTitle = false;
    private String pendingVerseTags = null;

    public UsfmBookContentsWriter(PrintWriter out) {
        super(out, "\\p");
    }

    @Override
    public void majorSection(Consumer<TextWriter> writes) {
        closeParagraph();
        out.println();
        out.print("\\ms1 ");
        writeText(writes);
        out.println();
    }

    @Override
    public void section(Consumer<TextWriter> writes) {
        closeParagraph();
        out.println();
        out.print("\\s1 ");
        writeText(writes);
        out.println();
    }

    @Override
    public void minorSection(Consumer<TextWriter> writes) {
        closeParagraph();
        out.println();
        out.print("\\s2 ");
        writeText(writes);
        out.println();
    }

    @Override
    public void chapter(int chapterNb, String... sourceNb) {
        closeParagraph();
        out.println();
        out.println("\\c " + chapterNb);
        out.println("\\cp " + sourceNb[0]);
    }

    @Override
    public void chapterTitle(Consumer<TextWriter> writes) {
        closeParagraph();
        out.println();
        out.print("\\cl ");
        writeText(writes);
        out.println();
    }

    @Override
    public void chapterIntro(Consumer<TextWriter> writes) {
        closeParagraph();
        out.println();
        out.print("\\cd ");
        writeText(writes);
        out.println();
    }

    @Override
    public void verse(int[] verseNbs, String... sourceNb) {
        String verseNbsStr = Arrays.stream(verseNbs)
                .mapToObj(Integer::toString).collect(Collectors.joining("-"));

        // Do not open the verse immediately, we should print it only after the paragraph marker if any.
        pendingVerseTags = "\\v " + verseNbsStr + " ";
        if(!verseNbsStr.equals(sourceNb[0])) {
            pendingVerseTags += "\\vp " + sourceNb[0] + "\\vp* ";
        }
    }

    private void openPendingVerse() {
        if(pendingVerseTags != null) {
            out.println();
            out.print(pendingVerseTags);
            pendingVerseTags = null;
        }
    }

    @Override
    public void psalmTitle(Consumer<TextWriter> writes) {
        // If not yet in a psalm title, open it.
        if(!inPsalmTitle) {
            // A psalm title closes the previous paragraph.
            closeParagraph();

            out.println();
            out.print("\\d ");

            this.inPsalmTitle = true;
        }

        // Open pending verse if any.
        openPendingVerse();

        // Then write the text.
        writeText(writes);
        out.println();
    }

    @Override
    protected void ensureInParagraph() {
        // Note that we are no longer in a psalm title.
        this.inPsalmTitle = false;

        // Actually open the paragraph.
        super.ensureInParagraph();

        // Open pending verse if any.
        openPendingVerse();
    }
}
