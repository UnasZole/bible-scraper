package com.github.unaszole.bible.writing.osis.stax;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class WrappingXmlStreamWriter implements XMLStreamWriter {
    private final XMLStreamWriter wrapped;

    public WrappingXmlStreamWriter(XMLStreamWriter wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        wrapped.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        wrapped.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        wrapped.writeStartElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        wrapped.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        wrapped.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        wrapped.writeEmptyElement(localName);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        wrapped.writeEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        wrapped.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
        wrapped.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        wrapped.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        wrapped.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        wrapped.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        wrapped.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        wrapped.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        wrapped.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        wrapped.writeComment(data);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        wrapped.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        wrapped.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        wrapped.writeCData(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        wrapped.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        wrapped.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        wrapped.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        wrapped.writeStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        wrapped.writeStartDocument(encoding, version);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        wrapped.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        wrapped.writeCharacters(text, start, len);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return wrapped.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        wrapped.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        wrapped.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        wrapped.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return wrapped.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return wrapped.getProperty(name);
    }
}
