package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.interfaces.TextWriter;
import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;

import java.io.PrintWriter;
import java.util.function.Consumer;

public abstract class UsfmStructuredTextWriter implements StructuredTextWriter {

    protected final PrintWriter out;
    public UsfmStructuredTextWriter(PrintWriter out) {
        this.out = out;
    }

    private boolean inParagraph = false;
    protected void closeParagraph() {
        inParagraph = false;
    }
    protected void ensureInParagraph() {
        if(!inParagraph) {
            out.println();
            out.print("\\p ");
            inParagraph = true;
        }
    }

    protected void writeText(Consumer<TextWriter> writes) {
        try(TextWriter writer = new UsfmTextWriter(out)) {
            writes.accept(writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void paragraph() {
        closeParagraph();
    }

    @Override
    public void flatText(Consumer<TextWriter> writes) {
        ensureInParagraph();

        writeText(writes);
    }

    @Override
    public void close() {
        out.flush();
    }
}
