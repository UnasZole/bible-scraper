package com.github.unaszole.bible.writing.osis;

import com.github.unaszole.bible.writing.BibleWriter;
import com.github.unaszole.bible.writing.BookWriter;
import com.github.unaszole.bible.writing.StructuredTextWriter;
import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.BibleNames;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamWriter;

public class OsisBookWriter extends BaseXmlWriter implements BookWriter {
	
	private static final Logger LOG = LoggerFactory.getLogger(OsisBookWriter.class);

	private final BibleNames bibleNames = BibleNames.instance();
	
	private final BibleWriter parent;
	private final BibleBook book;
	private boolean introOpened = false;
	
	public OsisBookWriter(BibleWriter parent, XMLStreamWriter xmlWriter, BibleBook book) {
		super(xmlWriter);
		this.parent = parent;
		this.book = book;
		
		// <div>
		writeStartElement("div");
		writeAttribute("type", "book");
		writeAttribute("osisID", book.getOSIS());
	}

	@Override
	public BookWriter title(String title) {
		// <title>
		writeStartElement("title");
		writeAttribute("type", "main");
		writeAttribute("short", bibleNames.getShortName(book));
		writeCharacters(title == null ? bibleNames.getLongName(book) : title);
		writeEndElement();
		// </title>

		return this;
	}

	@Override
	public StructuredTextWriter.BookIntroWriter introduction() {
		// <div>
		writeStartElement("div");
		writeAttribute("type", "section");
		
			// <title>
			writeStartElement("title");
			writeCharacters("Introduction");
			writeEndElement();
			// </title>
		
			this.introOpened = true;
		
		return new OsisBookIntroWriter(this, xmlWriter);
	}

	private void closeIntroduction() {
		if(introOpened) {
			writeEndElement();
			// </div>

			this.introOpened = false;
		}
	}

	@Override
	public StructuredTextWriter.BookContentsWriter contents() {
		closeIntroduction();
		return new OsisBookContentsWriter(this, xmlWriter, book);
	}

	@Override
	public BibleWriter closeBook() {
		// In the unlikely case of zero chapter, ensure now that the intro is closed.
		closeIntroduction();
		
		writeEndElement();
		// </div>
		
		// Give back the parent to caller.
		return parent;
	}
}