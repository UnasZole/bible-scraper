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

    private void closeCurrentParagraph() {
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
