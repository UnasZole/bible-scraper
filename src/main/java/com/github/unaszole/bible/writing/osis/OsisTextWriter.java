package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.interfaces.TextWriter;

import javax.xml.stream.XMLStreamWriter;

public class OsisTextWriter extends BaseXmlWriter implements TextWriter {

    public OsisTextWriter(XMLStreamWriter xmlWriter) {
        super(xmlWriter);
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
        writeCharacters(str);
    }

    @Override
    public void close() throws Exception {

    }
}
