package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.downloading.SourceFile;

import java.util.Map;

public class PageData {

    public final SourceFile sourceFile;
    public final Map<String, String> args;

    public PageData(SourceFile sourceFile, Map<String, String> args) {
        this.sourceFile = sourceFile;
        this.args = args;
    }
}
