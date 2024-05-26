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
    /**
     * >= 1 if pending a normal poetry line.
     * = -1 if pending a refrain line.
     * = 0 if no poetry line pending.
     */
    private int pendingPoetryLineIndent = 0;

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

    protected void openPendingPoetryLineIfAny() {
        if(pendingPoetryLineIndent != 0) {
            boolean isRefrain = pendingPoetryLineIndent == -1;
            int indentLevel = Math.max(pendingPoetryLineIndent, 0);

            ensureInStanza();

            // <l>
            writeStartElement("l");
            if(isRefrain) {
                writeAttribute("type", "refrain");
            }
            if(indentLevel >=1 ) {
                writeAttribute("level", String.valueOf(indentLevel));
            }

            this.inPoetryLine = true;

            // Line is opened : no longer pending.
            this.pendingPoetryLineIndent = 0;
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
            this.pendingPoetryLineIndent = 0;
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
        // Close the current poetry line.
        closeCurrentPoetryLine();
        // Opening of the new line is deferred until there is actual content to write in the line.
        this.pendingPoetryLineIndent = indentLevel;
    }

    @Override
    public void poetryRefrainLine() {
        closeCurrentPoetryLine();
        this.pendingPoetryLineIndent = -1;
    }

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
        openPendingPoetryLineIfAny();

        writeText(writes);
    }

    @Override
    public void close() {
        closeCurrentMajorSection();
    }
}
