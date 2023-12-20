package com.github.unaszole.bible.osisbuilder.writer;

import com.github.unaszole.bible.osisbuilder.BibleDataSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructuredTextWriter<ParentWriter extends Writer> implements Writer {
	
	private static final Logger LOG = LoggerFactory.getLogger(StructuredTextWriter.class);
	
	private final ParentWriter parent;
	private boolean inSection = false;
	private boolean inParagraph = false;
	private boolean inActiveParagraph = false;
	
	@Override
	public final XMLStreamWriter getXmlWriter() {
		return parent.getXmlWriter();
	}
	
	public StructuredTextWriter(ParentWriter parent) throws XMLStreamException {
		this.parent = parent;
	}
	
	private void openParagraph() throws XMLStreamException {
		// Close the current paragraph if any.
		closeCurrentParagraph();
		
		// <p>
		getXmlWriter().writeStartElement(OSIS_NS, "p");
		this.inParagraph = true;
		// Paragraph is active when opened.
		this.inActiveParagraph = true;
	}
	
	private void closeCurrentParagraph() throws XMLStreamException {
		if(inParagraph) {
			getXmlWriter().writeEndElement();
			// </p>
			this.inParagraph = false;
			this.inActiveParagraph = false;
		}
	}
	
	protected final void ensureInActiveParagraph() throws XMLStreamException {
		if(!inActiveParagraph) {
			// If we're not in a paragraph, or an inactive one, we need to open a new paragraph.
			openParagraph();
		}
	}
	
	private void openSection(String title) throws XMLStreamException {
		// Close the current section if any.
		closeCurrentSection();
		
		// <div>
		getXmlWriter().writeStartElement(OSIS_NS, "div");
		getXmlWriter().writeAttribute("type", "section");
		
			// <title>
			getXmlWriter().writeStartElement(OSIS_NS, "title");
			getXmlWriter().writeCharacters(title);
			getXmlWriter().writeEndElement();
			// </title>
		
		this.inSection = true;
	}
	
	private void closeCurrentSection() throws XMLStreamException {
		// Always close the paragraph when closing a section.
		closeCurrentParagraph();
		
		if(inSection) {
			getXmlWriter().writeEndElement();
			// </div>
			this.inSection = false;
		}
	}
	
	/**
		Mark the beginning of a new section.
		A section may contain several paragraphs, regrouped under a title.
		@param title The title of the section.
	*/
	public StructuredTextWriter<ParentWriter> section(String title) throws XMLStreamException {
		openSection(title);
		return this;
	}
	
	/**
		Mark the beginning of a new paragraph.
	*/
	public StructuredTextWriter<ParentWriter> paragraph() {
		// Mark the current paragraph as inactive to force opening a new one on next action.
		this.inActiveParagraph = false;
		
		return this;
	}
	
	/**
		Write raw text.
		@param str The text to write.
	*/
	public StructuredTextWriter<ParentWriter> text(String str) throws XMLStreamException {
		ensureInActiveParagraph();
		getXmlWriter().writeCharacters(str);
		
		return this;
	}
	
	/**
		Close the verse range. Must be called exactly once, and no other method called afterwards.
	*/
	public ParentWriter close() throws XMLStreamException {
		closeCurrentSection();
		
		// Give back the parent to caller.
		return parent;
	}
	
}