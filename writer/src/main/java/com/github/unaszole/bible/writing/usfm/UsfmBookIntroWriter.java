package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.OutputContainer;
import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.io.PrintWriter;
import java.util.function.Consumer;

public class UsfmBookIntroWriter extends UsfmStructuredTextWriter implements StructuredTextWriter.BookIntroWriter {
    public UsfmBookIntroWriter(PrintWriter out, OutputContainer container) {
        super(out, container, "\\ip");
    }

    @Override
    public void title(Consumer<TextWriter> writes) {
        closeParagraph();
        out.println();
        out.print("\\imt1 ");
        writeText(writes);
        out.println();
    }

    @Override
    public void majorSection(Consumer<TextWriter> writes) {
        closeParagraph();
        out.println();
        out.print("\\is1 ");
        writeText(writes);
        out.println();
    }

    @Override
    public void section(Consumer<TextWriter> writes) {
        closeParagraph();
        out.println();
        out.print("\\is2 ");
        writeText(writes);
        out.println();
    }

    @Override
    public void minorSection(Consumer<TextWriter> writes) {
        closeParagraph();
        out.println();
        out.print("\\is3 ");
        writeText(writes);
        out.println();
    }
}
