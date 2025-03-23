package com.github.unaszole.bible.writing.mybible;

public interface VerseSink extends AutoCloseable {
    void append(int bookNb, int chapterNb, int verseNb, String verseText);
}
