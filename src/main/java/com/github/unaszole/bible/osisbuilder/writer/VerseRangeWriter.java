package com.github.unaszole.bible.osisbuilder.writer;

import com.github.unaszole.bible.osisbuilder.BibleDataSource;

import org.crosswire.jsword.passage.Verse;
import org.crosswire.jsword.passage.VerseRange;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
	This is a specialised version of the structured text writer, that is able to handle verses.
	
	Example of expected usage :
	
	.paragraph()
		.verse(1)
			.text("Verse 1 is fully contained in a paragraph,")
		.verse(2)
			.text("which also contains verse 2,")
		.verse(3)
			.text("And the beginning of verse 3.")
	.paragraph()
			.text("The end of verse 3 is in another paragraph.")
		.verse(4)
			.text("Verse 4 starts in the middle of the paragraph.")
	
*/
public class VerseRangeWriter<ParentWriter extends Writer> extends StructuredTextWriter<ParentWriter> {
	
	private static final Logger LOG = LoggerFactory.getLogger(VerseRangeWriter.class);
	
	private final VerseRange verseRange;
	private Verse currentVerse = null;
	
	public VerseRangeWriter(ParentWriter parent, VerseRange verseRange) throws XMLStreamException {
		super(parent);
		this.verseRange = verseRange;
	}
	
	private void openVerse(Verse verse) throws XMLStreamException {
		// Close the current verse if any.
		closeCurrentVerse();
		
		// Make sure we're in an active paragraph for the verse start.
		ensureInActiveParagraph();
		
		// <verse sID>
		getXmlWriter().writeStartElement(OSIS_NS, "verse");
		getXmlWriter().writeAttribute("sID", verse.getOsisID());
		getXmlWriter().writeAttribute("osisID", verse.getOsisID());
		getXmlWriter().writeEndElement();
		// </verse>
		
		this.currentVerse = verse;
	}
	
	private void closeCurrentVerse() throws XMLStreamException {
		if(currentVerse != null) {
			//assert inParagraph : "Closing verse " + currentVerse + " must be called while in a paragraph";
			
			// <verse eID>
			getXmlWriter().writeStartElement(OSIS_NS, "verse");
			getXmlWriter().writeAttribute("eID", currentVerse.getOsisID());
			getXmlWriter().writeEndElement();
			// </verse>
			
			this.currentVerse = null;
		}
	}
	
	/**
		Mark the beginning of a specific verse.
		@param verse The verse to start. Must belong to the verse range.
	*/
	public VerseRangeWriter<ParentWriter> verse(Verse verse) throws XMLStreamException {
		assert verseRange.contains(verse) : "Verse " + verse + " must belong to " + verseRange;
		
		if(currentVerse == null && !verse.equals(currentVerse.getVersification().next(currentVerse))) {
			LOG.warn("Skipping from verse {} to {}", currentVerse, verse);
		}
		
		openVerse(verse);
		
		return this;
	}
	
	/**
		Mark the beginning of the next verse.
	*/
	public VerseRangeWriter<ParentWriter> verse() throws XMLStreamException {
		if(currentVerse == null) {
			// No current verse yet : set to start of the range.
			return verse(verseRange.getStart());
		}
		else {
			// Move to the next verse.
			Verse nextVerse = currentVerse.getVersification().next(currentVerse);
			return verse(nextVerse);
		}
	}
	
	/**
		Mark the beginning of a specific verse by number.
		Requires that the verse range being worked on is fully contained within a chapter, to avoid any ambiguity.
		@param verseNb The verse number within the chapter.
	*/
	public VerseRangeWriter<ParentWriter> verse(int verseNb) throws XMLStreamException {
		assert verseRange.getVersification().isSameChapter(verseRange.getStart(), verseRange.getEnd())
			: "Using verse method with verse number requires that the range " + verseRange + " is included in one chapter";
		
		return verse(new Verse(
			verseRange.getVersification(),
			verseRange.getStart().getBook(),
			verseRange.getStart().getChapter(),
			verseNb
		));
	}
	
	@Override
	public ParentWriter close() throws XMLStreamException {
		if(!verseRange.getEnd().equals(currentVerse)) {
			LOG.warn("Range {} finished on verse {} instead of expected {}", verseRange, currentVerse, verseRange.getEnd());
		}
		
		// Ensure we close the current verse before closing the containers.
		closeCurrentVerse();
		
		return super.close();
	}
	
	// The following methods are overridden only for the sake of maintaining the fluent API.
	
	@Override
	public VerseRangeWriter<ParentWriter> section(String title) throws XMLStreamException {
		super.section(title);
		return this;
	}
	
	@Override
	public VerseRangeWriter<ParentWriter> paragraph() {
		super.paragraph();
		return this;
	}
	
	@Override
	public VerseRangeWriter<ParentWriter> text(String str) throws XMLStreamException {
		super.text(str);
		return this;
	}
	
	
	
}