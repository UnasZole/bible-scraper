package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.parsing.WrappingParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import org.jsoup.nodes.Element;

import java.util.*;
import java.util.function.Function;

public class ElementExternalParser extends ElementAndContextStackAware {

    /**
     * If the selector field selected an element with an href attribute pointing to another anchor in the page,
     * a linkTargetSelector allows running the external parser on a descendant of the target of the link.
     */
    public EvaluatorWrapper linkTargetSelector;

    /**
     * Instructions to extract a sequence of contexts to inject before the external parser.
     */
    public List<ElementContextExtractor> contextsBefore;

    /**
     * Instructions to extract a sequence of contexts to inject after the external parser.
     */
    public List<ElementContextExtractor> contextsAfter;

    /**
     * If not null, invokes an HTML node parser iterating on the child nodes of the selected element.
     */
    public NodeParserConfig nodeParser;

    /**
     * If not null, invokes any text parser on the selected element's text contents.
     */
    public ElementTextParser textParser;

    private Optional<Element> getTargetElement(Element e, List<Context> currentContextStack, ContextualData contextualData) {
        if(!areElementAndContextStackValid(e, currentContextStack, contextualData)) {
            return Optional.empty();
        }

        Element target = e;
        if (linkTargetSelector != null && e.hasAttr("href")) {
            String[] link = e.attr("href").split("#");
            if (!link[0].isEmpty()) {
                throw new IllegalArgumentException("Cannot follow link " + link[0] + " as it's not local to the page");
            }
            target = e.ownerDocument().getElementById(link[1]).selectFirst(linkTargetSelector.get(contextualData));
        }
        return Optional.ofNullable(target);
    }

    public Parser<?> wrap(Function<Deque<Context>, Parser<?>> parserBuilder, Element targetElement,
                          Deque<Context> currentContextStack, ContextualData contextualData) {
        return WrappingParserCore.wrap(
                parserBuilder,
                ElementContextExtractor.getReaders(contextsBefore, targetElement, contextualData),
                ElementContextExtractor.getReaders(contextsAfter, targetElement, contextualData),
                currentContextStack
        );
    }

    public Optional<Parser<?>> getParserIfApplicable(Element e, final Deque<Context> currentContextStack,
                                                     final ContextualData contextualData) {
        return getTargetElement(e, new ArrayList<>(currentContextStack), contextualData)
                .flatMap(te -> {
                    if(nodeParser != null) {
                        return Optional.of(wrap(
                                cs -> nodeParser.getParser(te, cs, contextualData),
                                te, currentContextStack, contextualData
                        ));
                    }
                    else if(textParser != null) {
                        return Optional.of(wrap(
                                cs -> textParser.getParser(te, cs, contextualData),
                                te, currentContextStack, contextualData
                        ));
                    }
                    return Optional.empty();
                });
    }
}
