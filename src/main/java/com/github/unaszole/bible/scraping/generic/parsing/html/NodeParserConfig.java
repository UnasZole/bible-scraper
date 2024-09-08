package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.PositionBufferedParserCore;
import com.github.unaszole.bible.scraping.Parser;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class NodeParserConfig {

    public List<TextNodeParser> nodes;
    public List<ElementParser> elements;
    public List<ElementExternalParser> externalParsers;

    private List<PositionBufferedParserCore.ContextReader> parseElement(Deque<ContextMetadata> ancestorStack,
                                                                        ContextType type, Element e,
                                                                        ContextualData contextualData) {
        for(ElementParser eltParser: elements) {
            List<PositionBufferedParserCore.ContextReader> result = eltParser.parse(e, ancestorStack, type, contextualData);
            if(result != null) {
                return result;
            }
        }
        return List.of();
    }

    private List<PositionBufferedParserCore.ContextReader> parseTextNode(Deque<ContextMetadata> ancestorStack,
                                                                         ContextType type, TextNode t,
                                                                         ContextualData contextualData) {

        for(TextNodeParser nodeParser: nodes) {
            List<PositionBufferedParserCore.ContextReader> result = nodeParser.parse(t, ancestorStack, type, contextualData);
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

    public Parser<?> getParser(Element e, Deque<Context> currentContextStack, final ContextualData contextualData) {
        return new Parser<Node>(new PositionBufferedParserCore<Node>() {

            @Override
            public Parser<?> parseExternally(Node n, Deque<Context> currentContextStack) {
                if(externalParsers == null || !(n instanceof Element)) {
                    return null;
                }
                Element e = (Element) n;

                for(ElementExternalParser elementExternalParser : externalParsers) {
                    Optional<Parser<?>> parser = elementExternalParser.getParserIfApplicable(e, currentContextStack,
                            contextualData);
                    if(parser.isPresent()) {
                        return parser.get();
                    }
                }

                // The current element did not trigger any parser.
                return null;
            }

            @Override
            protected List<ContextReader> readContexts(Deque<ContextMetadata> ancestorStack, ContextType type,
                                                       ContextMetadata previousOfType, Node n) {
                if(n instanceof Element) {
                    return parseElement(ancestorStack, type, (Element) n, contextualData);
                }
                else if(n instanceof TextNode) {
                    return parseTextNode(ancestorStack, type, (TextNode) n, contextualData);
                }
                throw new IllegalArgumentException("Received a node of unknown type : " + n);
            }
        }, getNodeIterator(e), currentContextStack);
    }
}
