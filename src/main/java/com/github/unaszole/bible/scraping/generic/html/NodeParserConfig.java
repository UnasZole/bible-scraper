package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class NodeParserConfig extends ExternalParserConfig {

    public List<TextNodeContextExtractor> textNodeExtractors;
    public List<ElementContextExtractor> elementExtractors;

    private Context readElementContext(Deque<ContextMetadata> ancestorStack, ContextType type,
                                       ContextMetadata previousOfType, Element e) {
        if(elementExtractors == null) {
            return null;
        }

        List<ElementContextExtractor> extractors = elementExtractors.stream()
                .filter(ex -> ex.canOpenContextAt(ancestorStack, type))
                .collect(Collectors.toList());
        for(ElementContextExtractor extractor: extractors) {
            Context out = extractor.extractRootContext(e, ancestorStack.peekFirst(), previousOfType);
            if(out != null) {
                return out;
            }
        }
        return null;
    }

    private Context readTextNodeContext(Deque<ContextMetadata> ancestorStack, ContextType type,
                                        ContextMetadata previousOfType, TextNode t) {
        if(textNodeExtractors == null) {
            return null;
        }

        List<TextNodeContextExtractor> extractors = textNodeExtractors.stream()
                .filter(ex -> ex.canOpenContextAt(ancestorStack, type))
                .collect(Collectors.toList());
        for(TextNodeContextExtractor extractor: extractors) {
            Context out = extractor.extract(t, ancestorStack.peekFirst(), previousOfType);
            if(out != null) {
                return out;
            }
        }
        return null;
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
        return new Parser<Node>(getNodeIterator(e), currentContextStack) {
            @Override
            protected Context readContext(Deque<ContextMetadata> ancestorStack, ContextType type,
                                          ContextMetadata previousOfType, Node n) {
                if(n instanceof Element) {
                    return readElementContext(ancestorStack, type, previousOfType, (Element) n);
                }
                else if(n instanceof TextNode) {
                    return readTextNodeContext(ancestorStack, type, previousOfType, (TextNode) n);
                }
                return null;
            }
        };
    }
}
