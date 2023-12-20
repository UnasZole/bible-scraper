package com.github.unaszole.bible.osisbuilder.writer;

import java.io.OutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.Versification;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BibleWriter implements Writer {
	
	private static final Logger LOG = LoggerFactory.getLogger(BibleWriter.class);
	
	private final XMLStreamWriter writer;
	private final Versification versification;
	private BibleBook currentBook;
	
	@Override
	public final XMLStreamWriter getXmlWriter() {
		return writer;
	}
	
	public BibleWriter(OutputStream outputStream, Versification versification,
		String osisIDWork, String title, String ietfLanguage) throws XMLStreamException {
		XMLOutputFactory output = XMLOutputFactory.newInstance();
		this.writer = output.createXMLStreamWriter(outputStream, "UTF-8");
		this.versification = versification;
		this.currentBook = null;
		
		writer.writeStartDocument("UTF-8", "1.0");
		writer.setDefaultNamespace(OSIS_NS);
		writer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		// <osis>
		writer.writeStartElement(OSIS_NS, "osis");
		writer.writeAttribute("http://www.w3.org/2001/XMLSchema-instance",
			"schemaLocation", "http://www.bibletechnologies.net/2003/OSIS/namespace http://www.bibletechnologies.net/osisCore.2.1.1.xsd");
		
			// <osisText>
			writer.writeStartElement(OSIS_NS, "osisText");
			writer.writeAttribute("osisRefWork", "Bible");
			writer.writeAttribute("osisIDWork", osisIDWork);
			writer.writeAttribute("xml:lang", ietfLanguage);
			
				// <header>
				writer.writeStartElement(OSIS_NS, "header");
				
					// <revisionDesc>
					writer.writeStartElement(OSIS_NS, "revisionDesc");
					
						// <date>
						writer.writeStartElement(OSIS_NS, "date");
						writer.writeCharacters(DateTimeFormatter.ISO_LOCAL_DATE.format(Instant.now()));
						writer.writeEndElement();
						// </date>
				
						// <p>
						writer.writeStartElement(OSIS_NS, "p");
						writer.writeCharacters("Blabla");
						writer.writeEndElement();
						// </p>
					
					writer.writeEndElement();
					// </revisionDesc>
					
					// <work>
					writer.writeStartElement(OSIS_NS, "work");
					
						// <title>
						writer.writeStartElement(OSIS_NS, "title");
						writer.writeCharacters(title);
						writer.writeEndElement();
						// </title>
				
						// <type>
						writer.writeStartElement(OSIS_NS, "type");
						writer.writeAttribute("type", "OSIS");
						writer.writeCharacters("Bible");
						writer.writeEndElement();
						// </type>
						
						// <identifier>
						writer.writeStartElement(OSIS_NS, "identifier");
						writer.writeAttribute("type", "OSIS");
						writer.writeCharacters(osisIDWork);
						writer.writeEndElement();
						// </identifier>
						
						// <language>
						writer.writeStartElement(OSIS_NS, "language");
						writer.writeAttribute("type", "IETF");
						writer.writeCharacters(ietfLanguage);
						writer.writeEndElement();
						// </language>
						
						// <refSystem>
						writer.writeStartElement(OSIS_NS, "refSystem");
						String versificationName = versification.getName();
						writer.writeCharacters("Bible" + (versificationName.isEmpty() ? "" : "." + versificationName));
						writer.writeEndElement();
						// </refSystem>
					
					writer.writeEndElement();
					// </work>
				
				writer.writeEndElement();
				// </header>
	}
	
	/**
		Mark the beginning of a specific book.
		@param book The book.
		@param title The full title of the book.
		@return The writer to write the book.
	*/
	public BookWriter<BibleWriter> book(BibleBook book, String title) throws XMLStreamException {
		if(!book.equals(versification.getNextBook(this.currentBook))) {
			LOG.warn("Skipping from book {} to {}", currentBook, book);
		}
		this.currentBook = book;
		
		return new BookWriter(this, versification, currentBook, title);
	}
	
	/**
		Mark the beginning of a specific book, using the default title.
		@param book The book.
		@return The writer to write the book.
	*/
	public BookWriter<BibleWriter> book(BibleBook book) throws XMLStreamException {
		return book(book, null);
	}
	
	/**
		Mark the beginning of the next book.
		@return The writer to write the book.
	*/
	public BookWriter<BibleWriter> book() throws XMLStreamException {
		if(currentBook == null) {
			// No current book : start with first book.
			return book(versification.getFirstBook());
		}
		else {
			return book(versification.getNextBook(this.currentBook));
		}
	}
	
	/**
		Close the book. Must be called exactly once, and no other method called afterwards.
	*/
	public void close() throws XMLStreamException {
		
			writer.writeEndElement();
			// </osisText>
		
		writer.writeEndElement();
		// </osis>
		
		writer.writeEndDocument();
		
		if(!versification.getLastBook().equals(currentBook)) {
			LOG.warn("Finished on book {} instead of expected {}", currentBook, versification.getLastBook());
		}
	}
}