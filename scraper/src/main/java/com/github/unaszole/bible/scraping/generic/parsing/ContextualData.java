package com.github.unaszole.bible.scraping.generic.parsing;

import com.github.unaszole.bible.scraping.generic.ValueMapper;

import java.util.Map;

public class ContextualData {
    public final Map<String, String> args;
    public final Map<String, TextParser> namedParsers;
    public final ValueMapper mapper;

    public ContextualData(Map<String, String> args, Map<String, TextParser> namedParsers, ValueMapper mapper) {
        this.args = args;
        this.namedParsers = namedParsers;
        this.mapper = mapper;
    }
}
