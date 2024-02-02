package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;

import javax.xml.stream.XMLStreamWriter;

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

    private void openMajorSection(String title) {
        // Close the current section if any.
        closeCurrentMajorSection();

        // <div>
        writeStartElement("div");
        writeAttribute("type", "majorSection");

        // <title>
        writeStartElement("title");
        writeCharacters(title);
        writeEndElement();
        // </title>

        this.inMajorSection = true;
    }

    private void openSection(String title) {
        // Close the current section if any.
        closeCurrentSection();

        // <div>
        writeStartElement("div");
        writeAttribute("type", "section");

        // <title>
        writeStartElement("title");
        writeCharacters(title);
        writeEndElement();
        // </title>

        this.inSection = true;
    }

    private void openMinorSection(String title) {
        // Close the current section if any.
        closeCurrentMinorSection();

        // <div>
        writeStartElement("div");
        writeAttribute("type", "minorSection");

        // <title>
        writeStartElement("title");
        writeCharacters(title);
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
    public void majorSection(String title) {
        openMajorSection(title);
    }

    @Override
    public void section(String title) {
        openSection(title);
    }

    @Override
    public void minorSection(String title) {
        openMinorSection(title);
    }

    @Override
    public void paragraph() {
        // Mark the current paragraph as inactive to force opening a new one on next action.
        this.inActiveParagraph = false;
    }

    @Override
    public void note(String str) {
        // <note>
        writeStartElement("note");
        writeCharacters(str);
        writeEndElement();
        // </note>
    }

    @Override
    public void text(String str) {
        ensureInActiveParagraph();
        writeCharacters(str);
    }

    @Override
    public void close() {
        closeCurrentMajorSection();
    }
}
