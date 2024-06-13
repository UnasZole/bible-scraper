package com.github.unaszole.bible.writing.usfm;

import java.io.PrintWriter;

public class UsfmNoteTextWriter extends UsfmTextWriter {
    public UsfmNoteTextWriter(PrintWriter out) {
        super(out);
    }

    @Override
    public void text(String str) {
        out.print("\\ft " + str);
    }

    @Override
    public void quote(String str) {
        out.print("\\fq " + str);
    }
}
