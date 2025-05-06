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
        closeSelah();

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

    private static final int POETRY_REFRAIN = -1;
    private static final int POETRY_ACROSTIC = -2;
    private static final int POETRY_SELAH = -3;
    /**
     * >= 1 if pending a normal poetry line, the number indicates the indent.
     * > 0 if pending a special line, the number indicates the type.
     * = 0 if no poetry line pending.
     */
    private int pendingPoetryLine = 0;
    private boolean inSelah = false;

    private void closeSelah() {
        if(inSelah) {
            out.print("\\qs* ");
            this.inSelah = false;
        }
    }

    protected void openPendingPoetryLineIfAny(boolean paragraphLevelOnly) {
        if(pendingPoetryLine != 0) {
            closeSelah();

            boolean isRefrain = pendingPoetryLine == POETRY_REFRAIN;
            boolean isAcrostic = pendingPoetryLine == POETRY_ACROSTIC;
            boolean isSelah = pendingPoetryLine == POETRY_SELAH;
            int indentLevel = Math.max(pendingPoetryLine, 0);

            ensureInStanza();

            if(isRefrain) {
                out.println();
                out.print("\\qr ");
                this.pendingPoetryLine = 0;
            }
            if(isAcrostic) {
                out.println();
                out.print("\\qa ");
                this.pendingPoetryLine = 0;
            }
            if(isSelah && !paragraphLevelOnly) {
                out.print("\\qs ");
                this.pendingPoetryLine = 0;
                this.inSelah = true;
            }
            if(indentLevel >=1 ) {
                out.println();
                out.print("\\q" + indentLevel + " ");
                this.pendingPoetryLine = 0;
            }
        }
    }

    private boolean inParagraph = false;
    protected void ensureInParagraph() {
        if(!inParagraph) {
            out.println();
            out.print(paragraphMarker + " ");
            inParagraph = true;
        }
    }
    protected void closeParagraph() {
        closeStanza();
        inParagraph = false;
    }

    protected void ensureReadyForText() {
        ensureInParagraph();

        // If we have a pending poetry line, process it before writing the text.
        openPendingPoetryLineIfAny(false);
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
        pendingPoetryLine = indentLevel;
    }

    /**
     * Mark the start of a refrain line of poetry.
     */
    @Override
    public void poetryRefrainLine() {
        pendingPoetryLine = POETRY_REFRAIN;
    }

    @Override
    public void poetryAcrosticLine() {
        pendingPoetryLine = POETRY_ACROSTIC;
    }

    @Override
    public void poetrySelahLine() {
        pendingPoetryLine = POETRY_SELAH;
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
        ensureReadyForText();

        writeText(writes);
    }

    @Override
    public void close() {
        out.flush();
    }
}
