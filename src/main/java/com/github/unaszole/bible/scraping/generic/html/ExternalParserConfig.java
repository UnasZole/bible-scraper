package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.scraping.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;

import java.util.Deque;
import java.util.stream.Collectors;

public abstract class ExternalParserConfig extends ContextStackAware {

    /**
     * Selector to check if the current element triggers this external parser.
     */
    public Evaluator selector;

    public boolean canTriggerAtPosition(Element e, Deque<Context> currentContextStack) {
        return e.is(selector) && isContextStackValid(
                currentContextStack.stream().map(c -> c.metadata).collect(Collectors.toList())
        );
    }

    public abstract Parser<?> getParser(Element e, Deque<Context> currentContextStack);
}
