package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class NodeParserConfig extends ExternalParserConfig {

    public List<TextNodeParser> nodes;
    public List<ElementParser> elements;

    private List<PositionBufferedParserCore.ContextReader> parseElement(Deque<ContextMetadata> ancestorStack,
                                                                        ContextType type, Element e) {
        for(ElementParser eltParser: elements) {
            List<PositionBufferedParserCore.ContextReader> result = eltParser.parse(e, ancestorStack, type);
            if(result != null) {
                return result;
            }
        }
        return List.of();
    }

    private List<PositionBufferedParserCore.ContextReader> parseTextNode(Deque<ContextMetadata> ancestorStack,
                                                                         ContextType type, TextNode t) {

        for(TextNodeParser nodeParser: nodes) {
            List<PositionBufferedParserCore.ContextReader> result = nodeParser.parse(t, ancestorStack, type);
            if(result != null) {
                return result;
            }
        }
        return List.of();
    }

    private Iterator<Node> getNodeIterator(Element e) {
        return new Iterator<Node>() {
            private Element root = e;
            private Iterator<Node> childNodesIterator = e.childNodes().iterator();
            private Iterator<Element> elementDescendantsIterator = null;

            @Override
            public boolean hasNext() {
                return root != null || (elementDescendantsIterator != null && elementDescendantsIterator.hasNext())
                        || childNodesIterator.hasNext();
            }

            @Override
            public Node next() {
                if(root != null) {
                    // If the root element is still present, consume it first.
                    Element ret = root;
                    root = null;
                    return ret;
                }

                if(elementDescendantsIterator != null) {
                    // If we're exploring an child Element's descendants.
                    if(elementDescendantsIterator.hasNext()) {
                        // And there are descendants left, continue.
                        return elementDescendantsIterator.next();
                    }
                    else {
                        // And no descendant left, proceed to next child.
                        elementDescendantsIterator = null;
                    }
                }

                Node nextNode = childNodesIterator.next();
                if(nextNode instanceof Element) {
                    // If next node is an element, prepare to read its descendants.
                    elementDescendantsIterator = ((Element) nextNode).stream().iterator();
                    // Eliminate the first item of the stream as it's the current element that we're already returning.
                    elementDescendantsIterator.next();
                }
                // Return the next node for now.
                return nextNode;
            }
        };
    }

    @Override
    public Parser<?> getParser(Element e, Deque<Context> currentContextStack) {
        return new Parser<Node>(new PositionBufferedParserCore<Node>() {
            @Override
            protected List<ContextReader> readContexts(Deque<ContextMetadata> ancestorStack, ContextType type, ContextMetadata previousOfType, Node n) {
                if(n instanceof Element) {
                    return parseElement(ancestorStack, type, (Element) n);
                }
                else if(n instanceof TextNode) {
                    return parseTextNode(ancestorStack, type, (TextNode) n);
                }
                return null;
            }
        }, getNodeIterator(e), currentContextStack);
    }
}
