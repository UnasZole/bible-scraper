package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import javax.xml.stream.XMLStreamWriter;
import java.util.function.Consumer;

public abstract class OsisStructuredTextWriter
        extends BaseXmlWriter
        implements StructuredTextWriter {

    private boolean inMajorSection = false;
    private boolean inSection = false;
    private boolean inMinorSection = false;
    private boolean inParagraph = false;
    private boolean inActiveParagraph = false;
    private boolean inStanza = false;
    private boolean inPoetryLine = false;

    private static final int POETRY_REFRAIN = -1;
    private static final int POETRY_ACROSTIC = -2;
    private static final int POETRY_SELAH = -3;
    /**
     * >= 1 if pending a normal poetry line, the number indicates the indent.
     * > 0 if pending a special line, the number indicates the type.
     * = 0 if no poetry line pending.
     */
    private int pendingPoetryLine = 0;

    public OsisStructuredTextWriter(XMLStreamWriter xmlWriter) {
        super(xmlWriter);
    }

    protected final void writeText(Consumer<TextWriter> writes) {
        try(TextWriter writer = new OsisTextWriter(xmlWriter)) {
            writes.accept(writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void openMajorSection(Consumer<TextWriter> writes) {
        // Close the current section if any.
        closeCurrentMajorSection();

        // <div>
        writeStartElement("div");
        writeAttribute("type", "majorSection");

        // <title>
        writeStartElement("title");
        writeText(writes);
        writeEndElement();
        // </title>

        this.inMajorSection = true;
    }

    private void openSection(Consumer<TextWriter> writes) {
        // Close the current section if any.
        closeCurrentSection();

        // <div>
        writeStartElement("div");
        writeAttribute("type", "section");

        // <title>
        writeStartElement("title");
        writeText(writes);
        writeEndElement();
        // </title>

        this.inSection = true;
    }

    private void openMinorSection(Consumer<TextWriter> writes) {
        // Close the current section if any.
        closeCurrentMinorSection();

        // <div>
        writeStartElement("div");
        writeAttribute("type", "subSection");

        // <title>
        writeStartElement("title");
        writeText(writes);
        writeEndElement();
        // </title>

        this.inMinorSection = true;
    }

    private void openParagraph() {
        // Close the current paragraph if any.
        closeCurrentParagraph();

        // <p>
        writeStartElement("p");
        this.inParagraph = true;
        // Paragraph is active when opened.
        this.inActiveParagraph = true;
    }

    private void openStanza() {
        // Close the current stanza if any.
        closeCurrentStanza();

        // <lg>
        writeStartElement("lg");
        this.inStanza = true;
    }

    private void ensureInStanza() {
        if(!inStanza) {
            openStanza();
        }
    }

    protected void ensureReadyForText() {
        if(!inActiveParagraph) {
            // If we're not in a paragraph, or an inactive one, we need to open a new paragraph before writing text.
            openParagraph();
        }

        if(pendingPoetryLine != 0) {
            // If we have a pending poetry line, process it before writing the text.
            boolean isRefrain = pendingPoetryLine == POETRY_REFRAIN;
            boolean isAcrostic = pendingPoetryLine == POETRY_ACROSTIC;
            boolean isSelah = pendingPoetryLine == POETRY_SELAH;
            int indentLevel = Math.max(pendingPoetryLine, 0);

            ensureInStanza();

            // <l>
            writeStartElement("l");
            if(isRefrain) {
                writeAttribute("type", "refrain");
            }
            if(isAcrostic) {
                writeAttribute("type", "acrostic");
            }
            if(isSelah) {
                writeAttribute("type", "selah");
            }
            if(indentLevel >=1 ) {
                writeAttribute("level", String.valueOf(indentLevel));
            }

            this.inPoetryLine = true;

            // Line is opened : no longer pending.
            this.pendingPoetryLine = 0;
        }
    }

    private void closeCurrentPoetryLine() {
        if(inPoetryLine) {
            // </l>
            writeEndElement();
            this.inPoetryLine = false;
        }
    }

    private void closeCurrentStanza() {
        // Always close the poetry line when closing a stanza.
        closeCurrentPoetryLine();

        if(inStanza) {
            writeEndElement();
            // </lg>
            this.inStanza = false;

            // If there was a pending poetry line within the open stanza, it's an empty line to be ignored.
            this.pendingPoetryLine = 0;
        }
    }

    protected void closeCurrentParagraph() {
        // Always close the stanza when closing a paragraph.
        closeCurrentStanza();

        if(inParagraph) {
            writeEndElement();
            // </p>
            this.inParagraph = false;
            this.inActiveParagraph = false;
        }
    }

    private void closeCurrentMinorSection() {
        // Always close the paragraph when closing a section.
        closeCurrentParagraph();

        if(inMinorSection) {
            writeEndElement();
            // </div>
            this.inMinorSection = false;
        }
    }

    private void closeCurrentSection() {
        // Always close the minor section when closing a section.
        closeCurrentMinorSection();

        if(inSection) {
            writeEndElement();
            // </div>
            this.inSection = false;
        }
    }

    private void closeCurrentMajorSection() {
        // Always close the minor section when closing a major section.
        closeCurrentSection();

        if(inMajorSection) {
            writeEndElement();
            // </div>
            this.inMajorSection = false;
        }
    }

    @Override
    public void majorSection(Consumer<TextWriter> writes) {
        openMajorSection(writes);
    }

    @Override
    public void section(Consumer<TextWriter> writes) {
        openSection(writes);
    }

    @Override
    public void minorSection(Consumer<TextWriter> writes) {
        openMinorSection(writes);
    }

    @Override
    public void poetryLine(int indentLevel) {
        // Close the current poetry line.
        closeCurrentPoetryLine();
        // Opening of the new line is deferred until there is actual content to write in the line.
        this.pendingPoetryLine = indentLevel;
    }

    @Override
    public void poetryRefrainLine() {
        closeCurrentPoetryLine();
        this.pendingPoetryLine = POETRY_REFRAIN;
    }

    @Override
    public void poetryAcrosticLine() {
        closeCurrentPoetryLine();
        this.pendingPoetryLine = POETRY_ACROSTIC;
    }

    @Override
    public void poetrySelahLine() {
        closeCurrentPoetryLine();
        this.pendingPoetryLine = POETRY_SELAH;
    }

    @Override
    public void poetryStanza() {
        closeCurrentStanza();
    }

    @Override
    public void paragraph() {
        // Close current stanza immediately, to ensure that any pending poetry line in the next paragraph is well written in a new stanza.
        closeCurrentStanza();

        // Mark the current paragraph as inactive to force opening a new one on next action.
        this.inActiveParagraph = false;
    }

    @Override
    public void flatText(Consumer<TextWriter> writes) {
        ensureReadyForText();

        writeText(writes);
    }

    @Override
    public void close() {
        closeCurrentMajorSection();
    }
}
