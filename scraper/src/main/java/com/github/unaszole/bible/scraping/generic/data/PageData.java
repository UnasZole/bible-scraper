package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.downloading.SourceFile;

import java.util.Map;

public class PageData {

    public final SourceFile sourceFile;
    public final Map<String, String> args;
    public final String parserName;

    public PageData(SourceFile sourceFile, Map<String, String> args, String parserName) {
        this.sourceFile = sourceFile;
        this.args = args;
        this.parserName = parserName;
    }
}
