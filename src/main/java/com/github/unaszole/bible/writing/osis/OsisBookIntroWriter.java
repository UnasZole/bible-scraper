package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.BookWriter;
import com.github.unaszole.bible.writing.StructuredTextWriter;

import javax.xml.stream.XMLStreamWriter;

public class OsisBookIntroWriter extends OsisStructuredTextWriter<BookWriter, StructuredTextWriter.BookIntroWriter>
        implements StructuredTextWriter.BookIntroWriter {
    public OsisBookIntroWriter(BookWriter parent, XMLStreamWriter xmlWriter) {
        super(parent, xmlWriter);
    }

    @Override
    protected OsisBookIntroWriter getThis() {
        return this;
    }

    @Override
    public BookIntroWriter title(String title) {
        // <title>
        writeStartElement("title");
        writeCharacters(title);
        writeEndElement();
        // </title>
        return getThis();
    }
}
