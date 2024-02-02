package com.github.unaszole.bible.writing.osis;

import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import com.github.unaszole.bible.writing.interfaces.BookWriter;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.Versification;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsisBibleWriter extends BaseXmlWriter implements BibleWriter {
	
	private static final Logger LOG = LoggerFactory.getLogger(OsisBibleWriter.class);

	private static XMLStreamWriter getXmlStreamWriter(OutputStream outputStream) throws XMLStreamException {
		return new IndentingXMLStreamWriter(XMLOutputFactory.newInstance()
				.createXMLStreamWriter(outputStream, "UTF-8"));
	}

	public OsisBibleWriter(OutputStream outputStream, Versification versification,
						   String osisIDWork, String title, String ietfLanguage) throws XMLStreamException {
		super(getXmlStreamWriter(outputStream));

		// <osis>
		writeStartOsis();
		
			// <osisText>
			writeStartElement("osisText");
			writeAttribute("osisRefWork", "Bible");
			writeAttribute("osisIDWork", osisIDWork);
			writeAttribute("xml:lang", ietfLanguage);
			
				// <header>
				writeStartElement("header");
				
					// <revisionDesc>
					writeStartElement("revisionDesc");
					
						// <date>
						writeStartElement("date");
						writeCharacters(DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(Instant.now()));
						writeEndElement();
						// </date>
				
						// <p>
						writeStartElement("p");
						writeCharacters("Blabla");
						writeEndElement();
						// </p>
					
					writeEndElement();
					// </revisionDesc>
					
					// <work>
					writeStartElement("work");
					
						// <title>
						writeStartElement("title");
						writeCharacters(title);
						writeEndElement();
						// </title>
				
						// <type>
						writeStartElement("type");
						writeAttribute("type", "OSIS");
						writeCharacters("Bible");
						writeEndElement();
						// </type>
						
						// <identifier>
						writeStartElement("identifier");
						writeAttribute("type", "OSIS");
						writeCharacters(osisIDWork);
						writeEndElement();
						// </identifier>
						
						// <language>
						writeStartElement("language");
						writeAttribute("type", "IETF");
						writeCharacters(ietfLanguage);
						writeEndElement();
						// </language>
						
						// <refSystem>
						writeStartElement("refSystem");
						String versificationName = versification.getName();
						writeCharacters("Bible" + (versificationName.isEmpty() ? "" : "." + versificationName));
						writeEndElement();
						// </refSystem>
					
					writeEndElement();
					// </work>
				
				writeEndElement();
				// </header>
	}
	
	/**
		Mark the beginning of a specific book.
		@param book The book.
		@return The writer to write the book.
	*/
	@Override
	public void book(BibleBook book, Consumer<BookWriter> writes) {
		try(BookWriter bookWriter = new OsisBookWriter(xmlWriter, book)) {
			writes.accept(bookWriter);
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	/**
		Close the book. Must be called exactly once, and no other method called afterwards.
	*/
	@Override
	public void close() throws Exception {
		
			writeEndElement();
			// </osisText>
		
		writeEndOsis();
		// </osis>

		xmlWriter.close();
	}
}