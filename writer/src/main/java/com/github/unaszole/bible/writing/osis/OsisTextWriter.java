package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.OutputContainer;
import com.github.unaszole.bible.writing.datamodel.BibleRef;
import com.github.unaszole.bible.writing.interfaces.NoteTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import javax.xml.stream.XMLStreamWriter;
import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OsisTextWriter extends BaseXmlWriter implements TextWriter {

    private final OutputContainer container;

    public OsisTextWriter(XMLStreamWriter xmlWriter, OutputContainer container) {
        super(xmlWriter);
        this.container = container;
    }

    @Override
    public void text(String str) {
        writeCharacters(str);
    }

    @Override
    public void translationAdd(String str) {
        // <transChange type="added">
        writeStartElement("transChange");
        writeAttribute("type", "added");
        writeCharacters(str);
        writeEndElement();
        // </transChange>
    }

    @Override
    public void quote(String str) {
        // <q>
        writeStartElement("q");
        writeCharacters(str);
        writeEndElement();
        // </q>
    }

    @Override
    public void oldTestamentQuote(String str) {
        // <q>
        writeStartElement("seg");
        writeAttribute("type", "otPassage");
        writeCharacters(str);
        writeEndElement();
        // </q>
    }

    @Override
    public void speaker(String str) {
        // <speaker>
        writeStartElement("speaker");
        writeCharacters(str);
        writeEndElement();
        // </speaker>
    }

    private static String getOsisRef(BibleRef ref) {
        return ref.book.getOSIS()
                + (ref.chapter == 0 ? "" : "." + ref.chapter
                + (ref.verse == 0 ? "" : "." + ref.verse));
    }

    @Override
    public void reference(BibleRef rangeStart, BibleRef rangeEnd, String text) {
        // <reference>
        writeStartElement("reference");
        writeAttribute("osisRef", getOsisRef(rangeStart)
                + (rangeEnd == null ? "" : "-" + getOsisRef(rangeEnd))
        );
        writeCharacters(text);
        writeEndElement();
        // </reference>
    }

    @Override
    public void link(URI uri, String text) {
        // <a>
        writeStartElement("a");
        writeAttribute("href", uri.toString());
        writeCharacters(text);
        writeEndElement();
        // </a>
    }

    @Override
    public void figure(String attachmentName, Supplier<byte[]> bytes, String alt, String caption) {
        String fileName = container.attach(attachmentName, bytes.get());

        // <figure>
        writeStartElement("figure");
        writeAttribute("src", fileName);
        if(alt != null) {
            writeAttribute("alt", alt);
        }
        if(caption != null) {
            // <caption>
            writeStartElement("caption");
            writeCharacters(caption);
            writeEndElement();
            // </caption>
        }
        writeEndElement();
        // </figure>
    }

    @Override
    public void note(Consumer<NoteTextWriter> writes) {
        // <note>
        writeStartElement("note");
        writes.accept(new OsisNoteTextWriter(xmlWriter, container));
        writeEndElement();
        // </note>
    }

    @Override
    public void close() throws Exception {

    }
}
