package com.github.unaszole.bible.scraping.generic.html;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.unaszole.bible.scraping.implementations.GenericHtml;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration for extracting a String from an HTML element.
 */
public class ElementValueExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(ElementValueExtractor.class);

    /**
     * A selector to select a descendant of the current element.
     * If omitted, the operator applies to the current element itself.
     */
    public Evaluator selector;
    /**
     * An operator to extract a string from the selected element. The following are supported :
     * <li>text : extract the full text of this element and all its descendants.</li>
     * <li>ownText : extract the text of this element, excluding its descendants.</li>
     * <li>attribute=&lt;attribute name&gt; : extract an attribute of this element by providing its name.</li>
     */
    public String op;
    /**
     * A regexp to capture a part of the text content extracted by the operator.
     * This regexp will be implicitly anchored, and must contain one single capturing group.
     */
    public Pattern regexp;

    public String extract(Element e) {
        Element target = selector != null ? e.select(selector).first() : e;

        String[] opSplit = op.split("=");
        String opResult;
        switch (opSplit[0]) {
            case "text":
                opResult = target.text();
                break;
            case "ownText":
                opResult = target.ownText();
                break;
            case "attribute":
                opResult = target.attr(opSplit[1]);
                break;
            case "literal":
                opResult = opSplit[1];
                break;
            case "null":
            default:
                opResult = null;
                break;
        }

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
