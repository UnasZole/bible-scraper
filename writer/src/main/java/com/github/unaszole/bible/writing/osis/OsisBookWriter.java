package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.OutputContainer;
import com.github.unaszole.bible.writing.interfaces.BookWriter;
import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.BibleNames;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamWriter;
import java.util.function.Consumer;

public class OsisBookWriter extends BaseXmlWriter implements BookWriter {
	
	private static final Logger LOG = LoggerFactory.getLogger(OsisBookWriter.class);

	private final BibleNames bibleNames = BibleNames.instance();

    private final OutputContainer container;
	private final BibleBook book;
	private boolean introOpened = false;
	
	public OsisBookWriter(XMLStreamWriter xmlWriter, OutputContainer container, BibleBook book) {
		super(xmlWriter);
        this.container = container;
		this.book = book;
		
		// <div>
		writeStartElement("div");
		writeAttribute("type", "book");
		writeAttribute("osisID", book.getOSIS());
	}

	@Override
	public void title(String title) {
		// <title>
		writeStartElement("title");
		writeAttribute("type", "main");
		writeAttribute("short", bibleNames.getShortName(book));
		writeCharacters(title == null ? bibleNames.getLongName(book) : title);
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
	public void contents(Consumer<StructuredTextWriter.BookContentsWriter> writes) {
		closeIntroduction();

		try(StructuredTextWriter.BookContentsWriter contentsWriter = new OsisBookContentsWriter(xmlWriter, container, book)) {
			writes.accept(contentsWriter);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		// In the unlikely case of zero chapter, ensure now that the intro is closed.
		closeIntroduction();
		
		writeEndElement();
		// </div>
	}
}