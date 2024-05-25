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

    public OsisStructuredTextWriter(XMLStreamWriter xmlWriter) {
        super(xmlWriter);
    }

    protected void writeText(Consumer<TextWriter> writes) {
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
        writeAttribute("type", "minorSection");

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

    protected final void ensureInActiveParagraph() {
        if(!inActiveParagraph) {
            // If we're not in a paragraph, or an inactive one, we need to open a new paragraph.
            openParagraph();
        }
    }

    private void openStanza() {
        // Close the current stanza if any.
        closeCurrentStanza();

        // <lg>
        writeStartElement("lg");
        this.inStanza = true;
    }

    protected final void ensureInStanza() {
        ensureInActiveParagraph();
        if(!inStanza) {
            openStanza();
        }
    }

    private void openPoetryLine(boolean isRefrain, int indentLevel) {
        // Close the current line if any.
        closeCurrentPoetryLine();

        ensureInStanza();

        // <l>
        writeStartElement("l");
        if(isRefrain) {
            writeAttribute("type", "refrain");
        }
        if(indentLevel >=1 ) {
            writeAttribute("level", "1");
        }

        this.inPoetryLine = true;
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
        }
    }

    private void closeCurrentParagraph() {
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
        openPoetryLine(false, indentLevel);
    }

    /**
     * Mark the start of a refrain line of poetry.
     */
    @Override
    public void poetryRefrainLine() {
        openPoetryLine(true, 0);
    }

    /**
     * Mark the start of a new stanza of poetry.
     */
    @Override
    public void poetryStanza() {
        closeCurrentStanza();
    }

    @Override
    public void paragraph() {
        // Mark the current paragraph as inactive to force opening a new one on next action.
        this.inActiveParagraph = false;
    }

    @Override
    public void flatText(Consumer<TextWriter> writes) {
        ensureInActiveParagraph();

        writeText(writes);
    }

    @Override
    public void close() {
        closeCurrentMajorSection();
    }
}
