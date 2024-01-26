package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.StructuredTextWriter;

import java.io.PrintWriter;

public class UsfmBookIntroWriter extends UsfmStructuredTextWriter implements StructuredTextWriter.BookIntroWriter {
    public UsfmBookIntroWriter(PrintWriter out) {
        super(out);
    }

    @Override
    public void title(String title) {
        out.println();
        out.println("\\imt1 " + title);
    }

    @Override
    public void majorSection(String title) {
        out.println();
        out.println("\\is1 " + title);
    }

    @Override
    public void section(String title) {
        out.println();
        out.println("\\is2 " + title);
    }

    @Override
    public void minorSection(String title) {
        out.println();
        out.println("\\is3 " + title);
    }
}
