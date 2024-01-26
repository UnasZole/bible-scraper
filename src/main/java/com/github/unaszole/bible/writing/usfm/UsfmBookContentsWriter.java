package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.StructuredTextWriter;

import java.io.PrintWriter;

public class UsfmBookContentsWriter extends UsfmStructuredTextWriter implements StructuredTextWriter.BookContentsWriter {
    public UsfmBookContentsWriter(PrintWriter out) {
        super(out);
    }

    @Override
    public void majorSection(String title) {
        out.println();
        out.println("\\ms1 " + title);
    }

    @Override
    public void section(String title) {
        out.println();
        out.println("\\s1 " + title);
    }

    @Override
    public void minorSection(String title) {
        out.println();
        out.println("\\s2 " + title);
    }

    @Override
    public void chapter(int chapterNb, String... sourceNb) {
        out.println();
        out.println("\\c " + chapterNb);
        out.println("\\cp " + sourceNb[0]);
    }

    @Override
    public void chapterTitle(String title) {
        out.println();
        out.println("\\cd " + title);
    }

    @Override
    public void verse(int verseNb, String... sourceNb) {
        out.println();
        out.print("\\v " + verseNb + " \\vp " + sourceNb[0] + "\\vp* ");
    }
}
