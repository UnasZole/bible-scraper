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
    private void closeStanza() {
        if(inStanza) {
            out.println();
            out.println("\\b");
        }
        inStanza = false;
    }
    private void ensureInStanza() {
        if(!inStanza) {
            inStanza = true;
        }
    }

    /**
     * >= 1 if pending a normal poetry line.
     * = -1 if pending a refrain line.
     * = 0 if no poetry line pending.
     */
    private int pendingPoetryLineIndent = 0;

    private void openPendingPoetryLineIfAny() {
        if(pendingPoetryLineIndent != 0) {
            boolean isRefrain = pendingPoetryLineIndent == -1;
            int indentLevel = Math.max(pendingPoetryLineIndent, 0);

            ensureInStanza();

            out.println();
            if(isRefrain) {
                out.print("\\qr ");
            }
            if(indentLevel >=1 ) {
                out.print("\\q" + indentLevel + " ");
            }

            // Line is opened : no longer pending.
            this.pendingPoetryLineIndent = 0;
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
        pendingPoetryLineIndent = indentLevel;
    }

    /**
     * Mark the start of a refrain line of poetry.
     */
    @Override
    public void poetryRefrainLine() {
        pendingPoetryLineIndent = -1;
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
        openPendingPoetryLineIfAny();

        writeText(writes);
    }

    @Override
    public void close() {
        out.flush();
    }
}
