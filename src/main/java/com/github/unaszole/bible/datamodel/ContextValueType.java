package com.github.unaszole.bible.datamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ContextValueType {
    NO_VALUE(null, null),
    INTEGER(Pattern.compile("^\\s*(\\d+)\\s*$"), "1"),
    STRING(Pattern.compile("^(.*)$"), "");

    private static final Logger LOG = LoggerFactory.getLogger(ContextValueType.class);

    private final Pattern extractor;
    public final String implicitValue;

    ContextValueType(Pattern extractor, String implicitValue) {
        this.extractor = extractor;
        this.implicitValue = implicitValue;
    }

    /**
     *
     * @param value The context value as read from the parser.
     * @return The normalised context value for this value type.
     * Print error logs for unexpected values, but return in best-effort.
     */
    public String normalise(String value) {
        if((value == null) != (extractor == null)) {
            LOG.error("Context value {} provided when expecting {}", value, extractor);
        }

        if(extractor == null || value == null) {
            return null;
        }

        Matcher matcher = extractor.matcher(value);
        if(!matcher.matches()) {
            LOG.error("Context value {} provided when expecting {}", value, extractor);
            return value;
        }
        if(matcher.groupCount() >= 1) {
            return matcher.group(1);
        }
        return matcher.group();
    }
}
