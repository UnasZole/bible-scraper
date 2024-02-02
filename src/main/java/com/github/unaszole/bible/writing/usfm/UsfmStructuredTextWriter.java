package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;

import java.io.PrintWriter;

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

    @Override
    public void paragraph() {
        closeParagraph();
    }

    @Override
    public void text(String str) {
        ensureInParagraph();
        out.print(str);
    }

    @Override
    public void note(String str) {
        out.print("\\f + \\ft " + str + " \\f*");
    }

    @Override
    public void close() {
        out.flush();
    }
}
