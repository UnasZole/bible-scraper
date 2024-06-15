package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.interfaces.NoteTextWriter;

import javax.xml.stream.XMLStreamWriter;

public class OsisNoteTextWriter extends OsisTextWriter implements NoteTextWriter {
    public OsisNoteTextWriter(XMLStreamWriter xmlWriter) {
        super(xmlWriter);
    }

    @Override
    public void catchphraseQuote(String str) {
        // <catchWord>
        writeStartElement("catchWord");
        writeCharacters(str);
        writeEndElement();
        // </catchWord>
    }

    @Override
    public void alternateTranslationQuote(String str) {
        // <rdg>
        writeStartElement("rdg");

        writeCharacters(str);
        writeEndElement();
        // </rdg>
    }
}
