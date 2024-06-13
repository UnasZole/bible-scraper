package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.datamodel.BibleRef;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import javax.xml.stream.XMLStreamWriter;
import java.util.function.Consumer;

public class OsisTextWriter extends BaseXmlWriter implements TextWriter {

    public OsisTextWriter(XMLStreamWriter xmlWriter) {
        super(xmlWriter);
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
    public void note(Consumer<TextWriter> writes) {
        // <note>
        writeStartElement("note");
        writes.accept(this);
        writeEndElement();
        // </note>
    }

    @Override
    public void close() throws Exception {

    }
}
