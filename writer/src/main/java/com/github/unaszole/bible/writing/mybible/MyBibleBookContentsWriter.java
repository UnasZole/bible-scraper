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
    private StringBuilder pendingPrefixBuilder = null;

    public MyBibleBookContentsWriter(VerseSink sink, int bookNumber) {
        this.sink = sink;
        this.bookNumber = bookNumber;
    }

    private void append(StringBuilder stringBuilder, String str) {
        if(stringBuilder != null) {
            stringBuilder.append(str);
        }
    }

    private void appendHere(String str) {
        append(currentVerseBuilder, str);
    }

    private void appendToPrefix(String str) {
        if(pendingPrefixBuilder == null) {
            this.pendingPrefixBuilder = new StringBuilder();
        }
        append(pendingPrefixBuilder, str);
    }

    private void appendPrefixHere() {
        if(pendingPrefixBuilder != null) {
            String prefix = pendingPrefixBuilder.toString();
            this.pendingPrefixBuilder = null;
            appendHere(prefix);
        }
    }

    private void appendTaggedText(StringBuilder stringBuilder,
                                  String openTag, Consumer<TextWriter> writes, String closeTag) {
        if(stringBuilder != null) {
            stringBuilder.append(openTag);
            writes.accept(new MyBibleTextWriter(stringBuilder));
            stringBuilder.append(closeTag);
        }
    }

    private void appendTaggedTextHere(String openTag, Consumer<TextWriter> writes, String closeTag) {
        appendTaggedText(currentVerseBuilder, openTag, writes, closeTag);
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
        appendTaggedTextHere("<TS2>", writes, "<Ts2>");
    }

    @Override
    public void majorSection(Consumer<TextWriter> writes) {
        appendTaggedTextHere("<TS1>", writes, "<Ts1>");
    }

    @Override
    public void section(Consumer<TextWriter> writes) {
        appendTaggedTextHere("<TS3>", writes, "<Ts3>");
    }

    @Override
    public void minorSection(Consumer<TextWriter> writes) {
        appendTaggedTextHere("<TS4>", writes, "<Ts4>");
    }

    @Override
    public void poetryLine(int indentLevel) {
        appendToPrefix("<PF" + indentLevel + ">");
    }

    @Override
    public void poetryRefrainLine() {
        appendToPrefix("<PF7>");
    }

    @Override
    public void poetryAcrosticLine() {
        appendToPrefix("<PF7>");
    }

    @Override
    public void poetrySelahLine() {
        appendToPrefix("<PF7>");
    }

    @Override
    public void poetryStanza() {
        paragraph();
    }

    @Override
    public void paragraph() {
        appendHere("<CM>");
    }

    @Override
    public void flatText(Consumer<TextWriter> writes) {
        if(currentVerseBuilder != null) {
            appendPrefixHere();
            writes.accept(new MyBibleTextWriter(currentVerseBuilder));
        }
    }

    @Override
    public void close() throws Exception {
        appendPrefixHere();
        closeCurrentVerse();
    }
}
