package com.github.unaszole.bible.scraping.generic.parsing.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonNodeStringExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(JsonNodeStringExtractor.class);

    /**
     * A regexp to capture a part of the text of the selected node.
     * This regexp will be implicitly anchored, and must contain one single capturing group.
     * If unset, the full value of the selected node.
     */
    public Pattern regexp;

    public String extractString(JsonParserProvider.JsonNodeWrapper n) {
        String str = n.node.asText();

        if (regexp != null && str != null) {
            Matcher matcher = regexp.matcher(str);
            if (matcher.matches()) {
                str = matcher.group(1);
            } else {
                LOG.warn("Failed to match " + str + " against " + regexp);
                str = null;
            }
        }

        return str;
    }
}
