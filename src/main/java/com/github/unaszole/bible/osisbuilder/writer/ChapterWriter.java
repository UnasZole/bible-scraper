package com.github.unaszole.bible.osisbuilder.writer;

import com.github.unaszole.bible.osisbuilder.BibleDataSource;

import org.crosswire.jsword.passage.Verse;
import org.crosswire.jsword.passage.VerseRange;
import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.Versification;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class ChapterWriter<ParentWriter extends Writer> implements Writer {
	
	private final ParentWriter parent;
	private final Versification versification;
	private final BibleBook book;
	private final int chapter;
	
	@Override
	public final XMLStreamWriter getXmlWriter() {
		return parent.getXmlWriter();
	}
	
	public ChapterWriter(ParentWriter parent, Versification versification, BibleBook book, int chapter) throws XMLStreamException {
		this.parent = parent;
		this.versification = versification;
		this.book = book;
		this.chapter = chapter;
		
		// <chapter>
		getXmlWriter().writeStartElement(OSIS_NS, "chapter");
		getXmlWriter().writeAttribute("osisID", book.getOSIS() + "." + chapter);
	}
	
	/**
		Write a title for the chapter. Must be called at most once.
		@param title The title of the chapter.
	*/
	public ChapterWriter<ParentWriter> title(String title) throws XMLStreamException {
		// <title>
		getXmlWriter().writeStartElement(OSIS_NS, "title");
		getXmlWriter().writeAttribute("type", "chapter");
		getXmlWriter().writeCharacters(title);
		getXmlWriter().writeEndElement();
		// </title>
		
		return this;
	}
	
	/**
		Write the chapter contents. Must be called exactly once.
		@return The writer to write the chapter contents.
	*/
	public VerseRangeWriter<ChapterWriter<ParentWriter>> contents() throws XMLStreamException {
		VerseRange chapterRange = new VerseRange(
			versification,
			new Verse(versification, book, chapter, 1),
			new Verse(versification, book, chapter, versification.getLastVerse(book, chapter))
		);
		
		return new VerseRangeWriter(this, chapterRange);
	}
	
	/**
		Close the chapter. Must be called exactly once, and no other method called afterwards.
	*/
	public ParentWriter close() throws XMLStreamException {
		getXmlWriter().writeEndElement();
		// </chapter>
		
		// Give back the parent to caller.
		return parent;
	}
}