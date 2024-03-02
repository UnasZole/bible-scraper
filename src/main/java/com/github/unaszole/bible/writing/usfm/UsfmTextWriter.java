package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.io.PrintWriter;

public class UsfmTextWriter implements TextWriter {

    protected final PrintWriter out;
    public UsfmTextWriter(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void text(String str) {
        out.print(str);
    }

    @Override
    public void note(String str) {
        out.print("\\f + \\ft " + str + "\\f*");
    }

    @Override
    public void translationAdd(String str) {
        out.print("\\add " + str + "\\add*");
    }

    @Override
    public void close() {
        out.flush();
    }
}
