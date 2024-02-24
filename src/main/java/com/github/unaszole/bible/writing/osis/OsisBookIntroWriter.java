package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import javax.xml.stream.XMLStreamWriter;
import java.util.function.Consumer;

public class OsisBookIntroWriter extends OsisStructuredTextWriter
        implements StructuredTextWriter.BookIntroWriter {
    public OsisBookIntroWriter(XMLStreamWriter xmlWriter) {
        super(xmlWriter);
    }

    @Override
    public void title(Consumer<TextWriter> writes) {
        // <title>
        writeStartElement("title");
        writeText(writes);
        writeEndElement();
        // </title>
    }
}
