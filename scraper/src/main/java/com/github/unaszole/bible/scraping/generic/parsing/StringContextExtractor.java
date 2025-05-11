package com.github.unaszole.bible.scraping.generic.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration to extract a context from a string.
 */
public class StringContextExtractor extends GenericContextExtractor<String> {
    private static final Logger LOG = LoggerFactory.getLogger(StringContextExtractor.class);

    /**
     * Regexp to extract a context value from the selected text.
     * If specified, should have one single capturing group and always match.
     * Otherwise, no value will be given to the context.
     */
    public Pattern regexp;

    /**
     * Literal value for the context. Mutually exclusive with regexp.
     */
    public String literal;

    /**
     * Configuration to extract additional contexts as descendants of this one from the same text node.
     */
    public List<StringContextExtractor> descendants;

    @Override
    protected List<? extends GenericContextExtractor<String>> getDescendants() {
        return descendants;
    }

    @Override
    protected String extractValue(String text, ContextualData contextualData) {
        if(regexp == null) {
            return literal;
        }

        Matcher matcher = regexp.matcher(text);
        if(!matcher.matches()) {
            LOG.warn("Regexp {} failed to match {}", regexp, text);
            return "";
        }
        if(matcher.groupCount() == 0) {
            LOG.warn("If specified, regexp {} should have one single capturing group.", regexp);
            return text;
        }
        return matcher.group(1);
    }
}
