package com.github.unaszole.bible.scraping.generic.html;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;

import java.util.Deque;

public abstract class ExternalParserConfig {
    /**
     * A required ancestor type for this external parser to trigger. Only relevant for a root extractor.
     */
    public ContextType withAncestor;
    /**
     * An excluded ancestor type for this external parser to trigger. Only relevant for a root extractor.
     */
    public ContextType withoutAncestor;
    /**
     * Selector to check if the current element triggers this external parser.
     */
    public Evaluator selector;

    public boolean canTriggerAtPosition(Element e, Deque<Context> currentContextStack) {
        return e.is(selector) &&
                (this.withAncestor == null || currentContextStack.stream().anyMatch(a -> a.metadata.type == this.withAncestor)) &&
                (this.withoutAncestor == null || currentContextStack.stream().noneMatch(a -> a.metadata.type == this.withoutAncestor));
    }

    public abstract Parser<?> getParser(Element e, Deque<Context> currentContextStack);
}
