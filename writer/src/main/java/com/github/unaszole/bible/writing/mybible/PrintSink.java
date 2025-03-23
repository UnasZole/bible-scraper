package com.github.unaszole.bible.writing.mybible;

import java.io.PrintWriter;

public class PrintSink implements VerseSink {

    private final PrintWriter outWriter;

    public PrintSink(PrintWriter outWriter) {
        this.outWriter = outWriter;
    }

    @Override
    public void append(int bookNb, int chapterNb, int verseNb, String verseText) {
        outWriter.println(bookNb + "." + chapterNb + ":" + verseNb + " | " + verseText);
    }

    @Override
    public void close() throws Exception {
        outWriter.flush();
    }
}
