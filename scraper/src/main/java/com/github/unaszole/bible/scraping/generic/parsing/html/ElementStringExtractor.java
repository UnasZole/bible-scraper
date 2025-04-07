package com.github.unaszole.bible.scraping.generic.parsing.html;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElementStringExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(ElementStringExtractor.class);

    /**
     * An operator to extract a string from the selected element. The following are supported :
     * <li>text : extract the full text of this element and all its descendants.</li>
     * <li>ownText : extract the text of this element, excluding its descendants.</li>
     * <li>attribute=&lt;attribute name&gt; : extract an attribute of this element by providing its name.</li>
     * <li>literal=&lt;value&gt; : Provide a hardcoded value.</li>
     */
    public String op;

    /**
     * A regexp to capture a part of the text content extracted by the operator.
     * This regexp will be implicitly anchored, and must contain one single capturing group.
     * If unset, the full result of the operator will be used.
     */
    public Pattern regexp;

    private String op(Element e) {
        String[] opSplit = op.split("=");

        switch (opSplit[0]) {
            case "attribute":
                return e.attr(opSplit[1]);
            case "literal":
                return opSplit[1];
            case "ownText":
                return e.ownText();
            case "wholeOwnText":
                return e.wholeOwnText();
            case "text":
                return e.text();
            case "wholeText":
                return e.wholeText();
            default:
                throw new IllegalArgumentException("Unknown operator : " + opSplit[0]);
        }
    }

    public String extractString(Element e) {
        String opResult = op(e);

        if (regexp != null && opResult != null) {
            Matcher matcher = regexp.matcher(opResult);
            if (matcher.matches()) {
                opResult = matcher.group(1);
            } else {
                LOG.warn("Failed to match " + opResult + " against " + regexp);
                opResult = null;
            }
        }

        return opResult;
    }
}
