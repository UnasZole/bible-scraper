package com.github.unaszole.bible.writing.osis.stax;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;

public class IndentingXmlStreamWriter extends WrappingXmlStreamWriter {

    public enum Mode {YES, NO, AUTO}

    public static class IndentMode {
        public static final IndentMode AUTO = new IndentMode(Mode.AUTO, Mode.AUTO);
        public static final IndentMode CONTENTS = new IndentMode(Mode.AUTO, Mode.YES);
        public static final IndentMode SELF = new IndentMode(Mode.YES, Mode.AUTO);

        public final Mode self;
        public final Mode contents;

        public IndentMode(Mode self, Mode contents) {
            this.self = self;
            this.contents = contents;
        }
    }

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

    public IndentingXmlStreamWriter(XMLStreamWriter wrapped, BiFunction<String, String, IndentMode> indentModeResolver) {
        this(wrapped, "  ", "\n", indentModeResolver);
    }

    public IndentingXmlStreamWriter(XMLStreamWriter wrapped) {
        this(wrapped, (ns, name) -> IndentMode.AUTO);
    }

    private void writeIndent(boolean closing) throws XMLStreamException {
        super.writeCharacters(lineBreak);
        for(int i = 0; i < eltStack.size() - (closing ? 2 : 1); i++) {
            super.writeCharacters(indent);
        }
    }

    private void writeIndentForStartElement(IndentMode eltIndentMode) throws XMLStreamException {
        assert !eltStack.isEmpty();

        // Check the mode of the current head, i.e. the parent element.
        IndentMode parentIndentMode = eltStack.peek().mode;

        if(
                // Contents of the parent are explicitly indented.
                (parentIndentMode.contents == Mode.YES)
                // Contents of the parent are auto, and element itself is explicitly indented.
                || (parentIndentMode.contents == Mode.AUTO && eltIndentMode.self == Mode.YES)
                // Contents of the parent are auto, and parent currently ends with a whitespace.
                || (parentIndentMode.contents == Mode.AUTO && !eltStack.peek().endsWithNonWhitespaceData)
        ) {
            // Write indent for the opening tag.
            writeIndent(false);
        }
    }

    private void writeIndentForEndElement() throws XMLStreamException {
        assert !eltStack.isEmpty();

        // Check the mode of the current head, i.e. the closed element.
        IndentMode indentMode = eltStack.peek().mode;

        if(
                // Contents of the element are explicitly indented.
                indentMode.contents == Mode.YES
                // Contents of the element are auto, and element has children and currently ends with whitespace.
                || (indentMode.contents == Mode.AUTO && !eltStack.peek().endsWithNonWhitespaceData && eltStack.peek().hasChildren)
        ) {
            // Write indent for the closing tag.
            writeIndent(true);
        }
    }

    private void onStartElement(String namespaceUri, String eltName) throws XMLStreamException {
        assert !eltStack.isEmpty();
        // Note that the parent has children.
        eltStack.peek().hasChildren = true;

        IndentMode eltIndentMode = indentModeResolver.apply(namespaceUri, eltName);

        // Write indent for the open tag if needed.
        writeIndentForStartElement(eltIndentMode);

        // Starting element is the new head.
        eltStack.push(new Element(eltIndentMode));
    }

    private void onEndElement() throws XMLStreamException {
        // Write indent for the close tag if needed.
        writeIndentForEndElement();

        // Remove the ending element from the stack.
        eltStack.pop();
    }

    private void onEmptyElement(String namespaceUri, String eltName) throws XMLStreamException {
        assert !eltStack.isEmpty();
        // Note that the parent has children.
        eltStack.peek().hasChildren = true;

        IndentMode eltIndentMode = indentModeResolver.apply(namespaceUri, eltName);

        // Write indent for the self-closing tag if needed.
        writeIndentForStartElement(eltIndentMode);
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
        onEmptyElement(namespaceURI, localName);
        super.writeEmptyElement(namespaceURI, localName);
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        onEmptyElement(namespaceURI, localName);
        super.writeEmptyElement(prefix, localName, namespaceURI);
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        onEmptyElement(null, localName);
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
