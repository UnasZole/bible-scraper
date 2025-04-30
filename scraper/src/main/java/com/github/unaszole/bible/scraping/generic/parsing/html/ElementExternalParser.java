package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import org.jsoup.nodes.Element;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.stream.Collectors;

public class ElementExternalParser extends ElementAndContextStackAware {

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

    private Optional<Element> getTargetElement(Element e, Deque<ContextMetadata> currentContextStack, ContextualData contextualData) {
        if(!areElementAndContextStackValid(e, currentContextStack, contextualData)) {
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

    private Deque<ContextMetadata> toMetadataStack(Deque<Context> currentContextStack) {
        return currentContextStack.stream()
                .map(c -> c.metadata)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    public Optional<Parser<?>> getParserIfApplicable(final Element e, final Deque<Context> currentContextStack,
                                                     final ContextualData contextualData) {
        return getTargetElement(e, toMetadataStack(currentContextStack), contextualData)
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
