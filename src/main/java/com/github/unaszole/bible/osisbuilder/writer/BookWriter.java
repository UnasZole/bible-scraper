package com.github.unaszole.bible.osisbuilder.writer;

import com.github.unaszole.bible.osisbuilder.BibleDataSource;

import org.crosswire.jsword.passage.VerseRange;
import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.BibleNames;
import org.crosswire.jsword.versification.Versification;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookWriter<ParentWriter extends Writer> implements Writer {
	
	private static final Logger LOG = LoggerFactory.getLogger(BookWriter.class);
	
	private final BibleNames bibleNames = BibleNames.instance();
	
	private final ParentWriter parent;
	private final Versification versification;
	private final BibleBook book;
	private int currentChapter = 0;
	private boolean introOpened = false;
	
	@Override
	public final XMLStreamWriter getXmlWriter() {
		return parent.getXmlWriter();
	}
	
	public BookWriter(ParentWriter parent, Versification versification, BibleBook book, String title) throws XMLStreamException {
		this.parent = parent;
		this.book = book;
		this.versification = versification;
		
		
		// <div>
		getXmlWriter().writeStartElement(OSIS_NS, "div");
		getXmlWriter().writeAttribute("type", "book");
		getXmlWriter().writeAttribute("osisID", book.getOSIS());
		
			// <title>
			getXmlWriter().writeStartElement(OSIS_NS, "title");
			getXmlWriter().writeAttribute("type", "main");
			getXmlWriter().writeAttribute("short", bibleNames.getShortName(book));
			getXmlWriter().writeCharacters(title == null ? bibleNames.getLongName(book) : title);
			getXmlWriter().writeEndElement();
			// </title>
	}
	
	public StructuredTextWriter<BookWriter<ParentWriter>> introduction() throws XMLStreamException {
		// <div>
		getXmlWriter().writeStartElement(OSIS_NS, "div");
		getXmlWriter().writeAttribute("type", "section");
		
			// <title>
			getXmlWriter().writeStartElement(OSIS_NS, "title");
			getXmlWriter().writeCharacters("Introduction");
			getXmlWriter().writeEndElement();
			// </title>
		
			this.introOpened = true;
		
		return new StructuredTextWriter(this);
	}
	
	private void closeIntroduction() throws XMLStreamException {
		if(introOpened) {
			getXmlWriter().writeEndElement();
			// </div>
			
			this.introOpened = false;
		} 
	}
	
	/**
		Mark the beginning of a specific chapter.
		@param chapterNb The chapter number.
		@return The writer to write the chapter.
	*/
	public ChapterWriter<BookWriter<ParentWriter>> chapter(int chapterNb) throws XMLStreamException {
		
		// When starting a chapter, ensure the intro is closed.
		closeIntroduction();
		
		if(chapterNb != currentChapter + 1) {
			LOG.warn("Skipping from chapter {} to {} of {}", currentChapter, chapterNb, book);
		}
		currentChapter = chapterNb;
		assert currentChapter <= versification.getLastChapter(book) : "Current chapter " + currentChapter + " must belong to book " + book;
		
		return new ChapterWriter(this, versification, book, currentChapter);
	}
	
	/**
		Mark the beginning of the next chapter.
		@return The writer to write the chapter.
	*/
	public ChapterWriter<BookWriter<ParentWriter>> chapter() throws XMLStreamException {
		return chapter(currentChapter + 1);
	}
	
	/**
		Close the book. Must be called exactly once, and no other method called afterwards.
	*/
	public ParentWriter close() throws XMLStreamException {
		// In the unlikely case of zero chapter, ensure now that the intro is closed.
		closeIntroduction();
		
		getXmlWriter().writeEndElement();
		// </div>
		
		if(currentChapter != versification.getLastChapter(book)) {
			LOG.warn("Book {} finished on chapter {} instead of expected {}", book, currentChapter, versification.getLastChapter(book));
		}
		
		// Give back the parent to caller.
		return parent;
	}
}