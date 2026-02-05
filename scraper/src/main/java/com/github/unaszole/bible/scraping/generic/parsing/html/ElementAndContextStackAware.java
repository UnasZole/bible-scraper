package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.scraping.generic.parsing.ContextStackAware;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.List;
import java.util.regex.Pattern;

/**
 * An item of the parsing configuration that can trigger only at some points in the context stack and only for some
 * specific HTML elements.
 */
public class ElementAndContextStackAware extends ContextStackAware {

    public static class ElementMatcher {
        public Boolean exists;
        public EvaluatorWrapper matchesSelector;

        public boolean matches(Element elt, ContextualData contextualData) {
            if(exists != null && (exists != (elt != null))) {
                return false;
            }

            if(matchesSelector != null &&
                    (elt == null
                            || !elt.is(matchesSelector.get(contextualData)))
            ){
                return false;
            }

            return true;
        }
    }

    public static class NodeMatcher {
        public Boolean exists;
        public EvaluatorWrapper isElementMatching;
        public Pattern isTextNodeMatching;

        public NodeMatcher previous;
        public NodeMatcher next;

        public List<NodeMatcher> anyOf;
        public List<NodeMatcher> noneOf;

        private static boolean anyMatch(List<NodeMatcher> matchers, Node node, ContextualData contextualData) {
            for(NodeMatcher matcher: matchers) {
                if(matcher.matches(node, contextualData)) {
                    return true;
                }
            }
            return false;
        }

        public boolean matches(Node node, ContextualData contextualData) {
            if(node == null) {
                if(exists == Boolean.TRUE) {
                    // Fail if node is expected to exist.
                    return false;
                }

                if(previous != null || next != null || isElementMatching != null || isTextNodeMatching != null) {
                    // Fail all checks which imply existence.
                    return false;
                }
            }
            else {
                if(exists == Boolean.FALSE) {
                    // Fail if node is expected to not exist.
                    return false;
                }

                if(previous != null && !previous.matches(node.previousSibling(), contextualData)) {
                    return false;
                }

                if(next != null && !next.matches(node.nextSibling(), contextualData)) {
                    return false;
                }

                if(isElementMatching != null &&
                        (!(node instanceof Element) || !((Element) node).is(isElementMatching.get(contextualData)))
                ){
                    return false;
                }

                if(isTextNodeMatching != null &&
                        (!(node instanceof TextNode) || !isTextNodeMatching.matcher(((TextNode) node).text()).matches())) {
                    return false;
                }
            }

            if(anyOf != null && !anyMatch(anyOf, node, contextualData) ) {
                return false;
            }

            if(noneOf != null && anyMatch(noneOf, node, contextualData)) {
                return false;
            }

            return true;
        }
    }


    /**
     * Selector against the current element.
     */
    public EvaluatorWrapper selector;

    public ElementMatcher previousElement;
    public NodeMatcher previousNode;

    public ElementMatcher nextElement;
    public NodeMatcher nextNode;

    public boolean areElementAndContextStackValid(Element e, List<Context> currentContextStack, ContextualData contextualData) {
        if(!isContextStackValid(currentContextStack)) {
            return false;
        }

        if(!e.is(selector.get(contextualData))) {
            return false;
        }

        if(previousElement != null && !previousElement.matches(e.previousElementSibling(), contextualData)) {
            return false;
        }
        if(previousNode != null && !previousNode.matches(e.previousSibling(), contextualData)) {
            return false;
        }
        if(nextElement != null && !nextElement.matches(e.nextElementSibling(), contextualData)) {
            return false;
        }
        if(nextNode != null && !nextNode.matches(e.nextSibling(), contextualData)) {
            return false;
        }

        return true;
    }
}
