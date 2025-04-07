package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.scraping.generic.parsing.ContextStackAware;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import org.jsoup.nodes.Element;

import java.util.Deque;
import java.util.Optional;
import java.util.stream.Collectors;

public class ElementExternalParser extends ContextStackAware {

    /**
     * Selector to check if the current element triggers this external parser.
     */
    public EvaluatorWrapper selector;

    /**
     * If the selector field selected an element with an href attribute pointing to another anchor in the page,
     * a linkTargetSelector allows running the external parser on a descendant of the target of the link.
     */
    public EvaluatorWrapper linkTargetSelector;

    /**
     * If not null, invokes an HTML node parser iterating on the child nodes of the selected element.
     */
    public NodeParserConfig nodeParser;

    /**
     * If not null, invokes any text parser on the selected element's text contents.
     */
    public ElementTextParser textParser;

    private boolean canTriggerAtPosition(Element e, Deque<Context> currentContextStack, ContextualData contextualData) {
        return e.is(selector.get(contextualData)) && isContextStackValid(
                currentContextStack.stream().map(c -> c.metadata).collect(Collectors.toList())
        );
    }

    private Optional<Element> getTargetElement(Element e, Deque<Context> currentContextStack, ContextualData contextualData) {
        if(!canTriggerAtPosition(e, currentContextStack, contextualData)) {
            return Optional.empty();
        }

        Element target = e;
        if (linkTargetSelector != null && e.hasAttr("href")) {
            String[] link = e.attr("href").split("#");
            if (!link[0].isEmpty()) {
                throw new IllegalArgumentException("Cannot follow link " + link + " as it's not local to the page");
            }
            target = e.ownerDocument().getElementById(link[1]).selectFirst(linkTargetSelector.get(contextualData));
        }
        return Optional.ofNullable(target);
    }

    public Optional<Parser<?>> getParserIfApplicable(final Element e, final Deque<Context> currentContextStack,
                                                     final ContextualData contextualData) {
        return getTargetElement(e, currentContextStack, contextualData)
                .flatMap(te -> {
                    if(nodeParser != null) {
                        return Optional.of(nodeParser.getParser(te, currentContextStack, contextualData));
                    }
                    else if(textParser != null) {
                        return Optional.of(textParser.getParser(te, currentContextStack, contextualData));
                    }
                    return Optional.empty();
                });
    }
}
