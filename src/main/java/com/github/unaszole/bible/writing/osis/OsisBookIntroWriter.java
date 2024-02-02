package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;

import javax.xml.stream.XMLStreamWriter;

public class OsisBookIntroWriter extends OsisStructuredTextWriter
        implements StructuredTextWriter.BookIntroWriter {
    public OsisBookIntroWriter(XMLStreamWriter xmlWriter) {
        super(xmlWriter);
    }

    @Override
    public void title(String title) {
        // <title>
        writeStartElement("title");
        writeCharacters(title);
        writeEndElement();
        // </title>
    }
}
