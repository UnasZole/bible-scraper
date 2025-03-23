package com.github.unaszole.bible.writing.mybible;

import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.util.function.Consumer;

public class MyBibleBookContentsWriter implements StructuredTextWriter.BookContentsWriter {

    private final VerseSink sink;
    private final int bookNumber;
    private int currentChapter = 0;
    private int currentVerse = 0;
    private StringBuilder currentVerseBuilder = null;

    public MyBibleBookContentsWriter(VerseSink sink, int bookNumber) {
        this.sink = sink;
        this.bookNumber = bookNumber;
    }

    private void closeCurrentVerse() {
        if(currentVerseBuilder != null) {
            assert currentChapter > 0 && currentVerse > 0;
            sink.append(bookNumber, currentChapter, currentVerse, currentVerseBuilder.toString());
        }

        this.currentVerse = 0;
        this.currentVerseBuilder = null;
    }

    @Override
    public void chapter(int chapterNb, String... sourceNb) {
        closeCurrentVerse();
        this.currentChapter = chapterNb;
    }

    @Override
    public void chapterTitle(Consumer<TextWriter> writes) {
        // MyBible does not support chapter metadata.
    }

    @Override
    public void chapterIntro(Consumer<TextWriter> writes) {
        // MyBible does not support chapter metadata.
    }

    @Override
    public void verse(int[] verseNbs, String... sourceNb) {
        closeCurrentVerse();

        this.currentVerse = verseNbs[0];
        this.currentVerseBuilder = new StringBuilder();
    }

    @Override
    public void psalmTitle(Consumer<TextWriter> writes) {
        if(currentVerseBuilder != null) {
            currentVerseBuilder.append("<TS2>");
            writes.accept(new MyBibleTextWriter(currentVerseBuilder));
            currentVerseBuilder.append("<Ts2>");
        }
    }

    @Override
    public void majorSection(Consumer<TextWriter> writes) {
        if(currentVerseBuilder != null) {
            currentVerseBuilder.append("<TS1>");
            writes.accept(new MyBibleTextWriter(currentVerseBuilder));
            currentVerseBuilder.append("<Ts1>");
        }
    }

    @Override
    public void section(Consumer<TextWriter> writes) {
        if(currentVerseBuilder != null) {
            currentVerseBuilder.append("<TS3>");
            writes.accept(new MyBibleTextWriter(currentVerseBuilder));
            currentVerseBuilder.append("<Ts3>");
        }
    }

    @Override
    public void minorSection(Consumer<TextWriter> writes) {
        if(currentVerseBuilder != null) {
            currentVerseBuilder.append("<TS4>");
            writes.accept(new MyBibleTextWriter(currentVerseBuilder));
            currentVerseBuilder.append("<Ts4>");
        }
    }

    @Override
    public void poetryLine(int indentLevel) {
        if(currentVerseBuilder != null) {
            currentVerseBuilder.append("<PF")
                    .append(indentLevel)
                    .append(">");
        }
    }

    @Override
    public void poetryRefrainLine() {
        if(currentVerseBuilder != null) {
            currentVerseBuilder.append("<PF7>");
        }
    }

    @Override
    public void poetryAcrosticLine() {
        if(currentVerseBuilder != null) {
            currentVerseBuilder.append("<PF7>");
        }
    }

    @Override
    public void poetrySelahLine() {
        if(currentVerseBuilder != null) {
            currentVerseBuilder.append("<PF7>");
        }
    }

    @Override
    public void poetryStanza() {
        if(currentVerseBuilder != null) {
            currentVerseBuilder.append("<CM>");
        }
    }

    @Override
    public void paragraph() {
        if(currentVerseBuilder != null) {
            currentVerseBuilder.append("<CM>");
        }
    }

    @Override
    public void flatText(Consumer<TextWriter> writes) {
        if(currentVerseBuilder != null) {
            writes.accept(new MyBibleTextWriter(currentVerseBuilder));
        }
    }

    @Override
    public void close() throws Exception {
        closeCurrentVerse();
    }
}
