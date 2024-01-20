package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.StructuredTextWriter;

import javax.xml.stream.XMLStreamWriter;

public abstract class OsisStructuredTextWriter<ParentWriter, ThisWriter>
        extends BaseXmlWriter
        implements StructuredTextWriter<ParentWriter, ThisWriter> {

    private final ParentWriter parent;
    private boolean inMajorSection = false;
    private boolean inSection = false;
    private boolean inParagraph = false;
    private boolean inActiveParagraph = false;

    public OsisStructuredTextWriter(ParentWriter parent, XMLStreamWriter xmlWriter) {
        super(xmlWriter);
        this.parent = parent;
    }

    protected abstract ThisWriter getThis();

    private void openParagraph() {
        // Close the current paragraph if any.
        closeCurrentParagraph();

        // <p>
        writeStartElement("p");
        this.inParagraph = true;
        // Paragraph is active when opened.
        this.inActiveParagraph = true;
    }

    private void closeCurrentParagraph() {
        if(inParagraph) {
            writeEndElement();
            // </p>
            this.inParagraph = false;
            this.inActiveParagraph = false;
        }
    }

    protected final void ensureInActiveParagraph() {
        if(!inActiveParagraph) {
            // If we're not in a paragraph, or an inactive one, we need to open a new paragraph.
            openParagraph();
        }
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

    private void closeCurrentSection() {
        // Always close the paragraph when closing a section.
        closeCurrentParagraph();

        if(inSection) {
            writeEndElement();
            // </div>
            this.inSection = false;
        }
    }

    private void openMajorSection(String title) {
        // Close the current section if any.
        closeCurrentSection();

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
    public ThisWriter majorSection(String title) {
        openMajorSection(title);
        return getThis();
    }

    @Override
    public ThisWriter section(String title) {
        openSection(title);
        return getThis();
    }

    @Override
    public ThisWriter paragraph() {
        // Mark the current paragraph as inactive to force opening a new one on next action.
        this.inActiveParagraph = false;
        return getThis();
    }

    @Override
    public ThisWriter text(String str) {
        ensureInActiveParagraph();
        writeCharacters(str);
        return getThis();
    }

    @Override
    public ParentWriter closeText() {
        closeCurrentMajorSection();
        return parent;
    }
}
