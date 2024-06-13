package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.scraping.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;

import java.util.Deque;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ExternalParserConfig extends ContextStackAware {

    /**
     * Selector to check if the current element triggers this external parser.
     */
    public Evaluator selector;

    /**
     * If the selector field selected an element with an href attribute pointing to another anchor in the page,
     * a linkTargetSelector will allows running the external parser on a descendant of the target of the link.
     */
    public Evaluator linkTargetSelector;

    private boolean canTriggerAtPosition(Element e, Deque<Context> currentContextStack) {
        return e.is(selector) && isContextStackValid(
                currentContextStack.stream().map(c -> c.metadata).collect(Collectors.toList())
        );
    }

    protected abstract Parser<?> getParser(Element e, Deque<Context> currentContextStack,
                                           ContextualData contextualData);

    public Optional<Parser<?>> getParserIfApplicable(Element e, Deque<Context> currentContextStack,
                                                     ContextualData contextualData) {
        if(!canTriggerAtPosition(e, currentContextStack)) {
            return Optional.empty();
        }

        Element target = e;
        if (linkTargetSelector != null && e.hasAttr("href")) {
            String[] link = e.attr("href").split("#");
            if (!link[0].isEmpty()) {
                throw new IllegalArgumentException("Cannot follow link " + link + " as it's not local to the page");
            }
            target = e.ownerDocument().getElementById(link[1]).selectFirst(linkTargetSelector);
        }
        return Optional.of(getParser(target, currentContextStack, contextualData));
    }
}
