package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UsfmBookContentsWriter extends UsfmStructuredTextWriter implements StructuredTextWriter.BookContentsWriter {
    private boolean inPsalmTitle = false;

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
        out.println();
        String verseNbsStr = Arrays.stream(verseNbs)
                .mapToObj(Integer::toString).collect(Collectors.joining("-"));
        out.print("\\v " + verseNbsStr + " ");
        if(!verseNbsStr.equals(sourceNb[0])) {
            out.print("\\vp " + sourceNb[0] + "\\vp* ");
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

        // Then write the text.
        writeText(writes);
        out.println();
    }

    @Override
    protected void closeParagraph() {
        if(inPsalmTitle) {
            // If we are in a psalm title, just mark it closed.
            this.inPsalmTitle = false;
        }
        else {
            // Else, we close a regular paragraph.
            super.closeParagraph();
        }
    }
}
