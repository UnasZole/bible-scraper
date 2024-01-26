package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.StructuredTextWriter;

import java.io.PrintWriter;

public abstract class UsfmStructuredTextWriter implements StructuredTextWriter {

    protected final PrintWriter out;
    public UsfmStructuredTextWriter(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void paragraph() {
        out.println();
        out.print("\\p ");
    }

    @Override
    public void text(String str) {
        out.print(str);
    }

    @Override
    public void note(String str) {
        out.print("\\f + \\ft " + str + " \\f*");
    }

    @Override
    public void close() {

    }
}
