package com.github.unaszole.bible.writing.osis.stax;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;

public class IndentingXmlStreamWriter extends WrappingXmlStreamWriter {

    public enum IndentMode { YES, NO, AUTO }

    private static class Element {
        public final IndentMode mode;
        public boolean hasChildren = false;
        public boolean endsWithNonWhitespaceData = false;

        public Element(IndentMode mode) {
            this.mode = mode;
        }
    }

    private final String indent;
    private final String lineBreak;
    private final BiFunction<String, String, IndentMode> indentModeResolver;
    private final Deque<Element> eltStack = new ArrayDeque<>(List.of(new Element(IndentMode.AUTO)));

    public IndentingXmlStreamWriter(XMLStreamWriter wrapped, String indent, String lineBreak,
                                    BiFunction<String, String, IndentMode> indentModeResolver) {
        super(wrapped);
        this.indent = indent;
        this.lineBreak = lineBreak;
        this.indentModeResolver = indentModeResolver;
    }

    public IndentingXmlStreamWriter(XMLStreamWriter wrapped) {
        this(wrapped, "  ", "\n", (ns, name) -> IndentMode.AUTO);
    }

    private void writeIndent(boolean closing) throws XMLStreamException {
        super.writeCharacters(lineBreak);
        for(int i = 0; i < eltStack.size() - (closing ? 2 : 1); i++) {
            super.writeCharacters(indent);
        }
    }

    private void writeIndentIfNeeded(boolean closing) throws XMLStreamException {
        assert !eltStack.isEmpty();

        // Check the mode of the current head.
        switch(eltStack.peek().mode) {
            case YES:
                // We need to indent before the next tag.
                writeIndent(closing);
                break;
            case AUTO:
                // We need to indent only if the current head does not currently end with non-space data.
                // Else, the indent would insert new significant spaces, to be avoided.
                if(!eltStack.peek().endsWithNonWhitespaceData) {
                    // Do not indent if closing an element with no children.
                    if(!closing || eltStack.peek().hasChildren) {
                        writeIndent(closing);
                    }
                }
                break;
        }
    }

    private void onStartElement(String namespaceUri, String eltName) throws XMLStreamException {
        assert !eltStack.isEmpty();
        // Note that the parent has children.
        eltStack.peek().hasChildren = true;

        // Write indent for the open tag if the parent requires it.
        writeIndentIfNeeded(false);

        // Starting element is the new head.
        eltStack.push(new Element(indentModeResolver.apply(namespaceUri, eltName)));
    }

    private void onEndElement() throws XMLStreamException {
        // Write indent for the close tag if the element requires it.
        writeIndentIfNeeded(true);

        // Remove the ending element from the stack.
        eltStack.pop();
    }

    private void onEmptyElement() throws XMLStreamException {
        assert !eltStack.isEmpty();
        // Note that the parent has children.
        eltStack.peek().hasChildren = true;

        // Write indent for the self-closing tag if the parent element requires it.
        writeIndentIfNeeded(false);
    }

    private boolean isXmlWhitespace(char c) {
        return c == '\r' || c == '\n' || c == '\t' || c == ' ';
    }

    private void onData(char[] data) {
        assert !eltStack.isEmpty();

        eltStack.peek().endsWithNonWhitespaceData = data.length > 0 && !isXmlWhitespace(data[data.length -1]);
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        onStartElement(null, localName);
        super.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        onStartElement(namespaceURI, localName);
        super.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        onStartElement(namespaceURI, localName);
        super.writeStartElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        onEndElement();
        super.writeEndElement();
    }

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        onEmptyElement();
        super.writeEmptyElement(namespaceURI, localName);
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        onEmptyElement();
        super.writeEmptyElement(prefix, localName, namespaceURI);
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        onEmptyElement();
        super.writeEmptyElement(localName);
    }

    public void writeCharacters(String text) throws XMLStreamException {
        onData(text.toCharArray());
        super.writeCharacters(text);
    }

    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        onData(text);
        super.writeCharacters(text, start, len);
    }

    public void writeCData(String data) throws XMLStreamException {
        onData(data.toCharArray());
        super.writeCData(data);
    }
}
