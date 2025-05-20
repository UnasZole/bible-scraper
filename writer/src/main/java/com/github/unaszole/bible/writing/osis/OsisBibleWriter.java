package com.github.unaszole.bible.writing.osis;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import com.github.unaszole.bible.writing.datamodel.DocumentMetadata;
import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import com.github.unaszole.bible.writing.interfaces.BookGroupWriter;
import com.github.unaszole.bible.writing.interfaces.BookWriter;
import com.github.unaszole.bible.writing.osis.stax.IndentingXmlStreamWriter;
import org.crosswire.jsword.versification.BibleBook;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsisBibleWriter extends BaseXmlWriter implements BibleWriter {
	
	private static final Logger LOG = LoggerFactory.getLogger(OsisBibleWriter.class);

	private static XMLStreamWriter getXmlStreamWriter(OutputStream outputStream) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		return new IndentingXmlStreamWriter(
				factory.createXMLStreamWriter(outputStream, "UTF-8"),
				(ns, name) -> (name.equals("verse") || name.equals("chapter"))
						? IndentingXmlStreamWriter.IndentMode.SELF
						: IndentingXmlStreamWriter.IndentMode.AUTO
		);
	}

	public OsisBibleWriter(OutputStream outputStream, DocumentMetadata meta) throws XMLStreamException {
		super(getXmlStreamWriter(outputStream));

		// <osis>
		writeStartOsis();
		
			// <osisText>
			writeStartElement("osisText");
			writeAttribute("osisRefWork", meta.systemName);
			writeAttribute("osisIDWork", meta.systemName);
			writeAttribute("xml:lang", meta.locale.toLanguageTag());
			
				// <header>
				writeStartElement("header");
				
					// <revisionDesc>
					writeStartElement("revisionDesc");
					writeAttribute("resp", "bible-scraper");
					
						// <date>
						writeStartElement("date");
						writeCharacters(DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(Instant.now()));
						writeEndElement();
						// </date>
				
						// <p>
						writeStartElement("p");
						writeCharacters("Scraped the bible.");
						writeEndElement();
						// </p>
					
					writeEndElement();
					// </revisionDesc>
					
					// <work>
					writeStartElement("work");
					writeAttribute("osisWork", meta.systemName);
					
						// <title>
						writeStartElement("title");
						writeCharacters(meta.title);
						writeEndElement();
						// </title>
						
						// <identifier>
						writeStartElement("identifier");
						writeAttribute("type", "OSIS");
						writeCharacters(meta.systemName);
						writeEndElement();
						// </identifier>
						
						// <language>
						writeStartElement("language");
						writeAttribute("type", "IETF");
						writeCharacters(meta.locale.toLanguageTag());
						writeEndElement();
						// </language>
						
						// <refSystem>
						writeStartElement("refSystem");
						writeCharacters(meta.refSystem);
						writeEndElement();
						// </refSystem>
					
					writeEndElement();
					// </work>
				
				writeEndElement();
				// </header>
	}

	private static OutputStream getOutputStream(Path path) throws IOException {
		if(Files.isDirectory(path)) {
			throw new IllegalArgumentException(path + " must be a file.");
		}
		if(!path.getFileName().toString().endsWith(".xml")) {
			throw new IllegalArgumentException(path + " must be a file ending with .xml");
		}

		return Files.newOutputStream(path);
	}

	public OsisBibleWriter(Path outFile, DocumentMetadata meta) throws IOException, XMLStreamException {
		this(getOutputStream(outFile), meta);
	}

	@Override
	public void bookGroup(Consumer<BookGroupWriter> writes) {
		try(BookGroupWriter bookGroupWriter = new OsisBookGroupWriter(xmlWriter)) {
			writes.accept(bookGroupWriter);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void book(BibleBook book, Consumer<BookWriter> writes) {
		try(BookWriter bookWriter = new OsisBookWriter(xmlWriter, book)) {
			writes.accept(bookWriter);
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	@Override
	public void close() throws Exception {
		
			writeEndElement();
			// </osisText>
		
		writeEndOsis();
		// </osis>

		xmlWriter.close();
	}
}