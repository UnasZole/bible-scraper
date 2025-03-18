package com.github.unaszole.bible.writing.osis;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class BaseXmlWriter {
    private static final String OSIS_NS = "http://www.bibletechnologies.net/2003/OSIS/namespace";

    protected final XMLStreamWriter xmlWriter;

    public BaseXmlWriter(XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    protected final void writeStartOsis() {
        try {
            xmlWriter.writeStartDocument("UTF-8", "1.0");
            xmlWriter.setDefaultNamespace(OSIS_NS);
            xmlWriter.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");

            // <osis>
            writeStartElement("osis");
            xmlWriter.writeAttribute("http://www.w3.org/2001/XMLSchema-instance",
                    "schemaLocation", "http://www.bibletechnologies.net/2003/OSIS/namespace http://www.bibletechnologies.net/osisCore.2.1.1.xsd");
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void writeEndOsis() {
        try {
            writeEndElement();
            // </osis>

            xmlWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void writeStartElement(String tagName) {
        try {
            xmlWriter.writeStartElement(OSIS_NS, tagName);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void writeEndElement() {
        try {
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void writeEmptyElement(String tagName) {
        try {
            xmlWriter.writeEmptyElement(OSIS_NS, tagName);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void writeAttribute(String name, String value) {
        try {
            xmlWriter.writeAttribute(name, value);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void writeCharacters(String chars) {
        try {
            xmlWriter.writeCharacters(chars);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
