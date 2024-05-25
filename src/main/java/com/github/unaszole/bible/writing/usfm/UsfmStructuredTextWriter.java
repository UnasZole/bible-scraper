package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.interfaces.TextWriter;
import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;

import java.io.PrintWriter;
import java.util.function.Consumer;

public abstract class UsfmStructuredTextWriter implements StructuredTextWriter {

    protected final PrintWriter out;
    private final String paragraphMarker;
    public UsfmStructuredTextWriter(PrintWriter out, String paragraphMarker) {
        this.out = out;
        this.paragraphMarker = paragraphMarker;
    }

    private boolean inStanza = false;
    protected void closeStanza() {
        inStanza = false;
    }
    protected void ensureInStanza() {
        if(!inStanza) {
            out.println("\\b");
            inStanza = true;
        }
    }

    private boolean inParagraph = false;
    protected void closeParagraph() {
        closeStanza();
        inParagraph = false;
    }
    protected void ensureInParagraph() {
        if(!inParagraph) {
            out.println();
            out.print(paragraphMarker + " ");
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
    public void poetryLine(int indentLevel) {
        out.println();
        out.print("\\q" + indentLevel + " ");
    }

    /**
     * Mark the start of a refrain line of poetry.
     */
    @Override
    public void poetryRefrainLine() {
        out.println();
        out.print("\\qr ");
    }

    /**
     * Mark the start of a new stanza of poetry.
     */
    @Override
    public void poetryStanza() {
        closeStanza();
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
