package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.OutputContainer;
import com.github.unaszole.bible.writing.interfaces.BookGroupWriter;
import com.github.unaszole.bible.writing.interfaces.BookWriter;
import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import org.crosswire.jsword.versification.BibleBook;

import javax.xml.stream.XMLStreamWriter;
import java.util.function.Consumer;

public class OsisBookGroupWriter extends BaseXmlWriter implements BookGroupWriter {

    private final OutputContainer container;
    private boolean introOpened = false;

    public OsisBookGroupWriter(XMLStreamWriter xmlWriter, OutputContainer container) {
        super(xmlWriter);
        this.container = container;

        // <div>
        writeStartElement("div");
        writeAttribute("type", "bookGroup");
    }

    @Override
    public void title(String title) {
        // <title>
        writeStartElement("title");
        writeAttribute("type", "main");
        writeCharacters(title);
        writeEndElement();
        // </title>
    }

    @Override
    public void introduction(Consumer<StructuredTextWriter.BookIntroWriter> writes) {
        // <div>
        writeStartElement("div");
        writeAttribute("type", "introduction");

        this.introOpened = true;

        try(StructuredTextWriter.BookIntroWriter introWriter = new OsisBookIntroWriter(xmlWriter, container)) {
            writes.accept(introWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void closeIntroduction() {
        if(introOpened) {
            writeEndElement();
            // </div>

            this.introOpened = false;
        }
    }

    @Override
    public void book(BibleBook book, Consumer<BookWriter> writes) {
        closeIntroduction();

        try(BookWriter bookWriter = new OsisBookWriter(xmlWriter, container, book)) {
            writes.accept(bookWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        // In the unlikely case of zero book, ensure now that the intro is closed.
        closeIntroduction();

        writeEndElement();
        // </div>
    }
}
