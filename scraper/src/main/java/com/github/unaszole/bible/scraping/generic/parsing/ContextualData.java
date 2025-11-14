package com.github.unaszole.bible.scraping.generic.parsing;

import com.github.unaszole.bible.downloading.CachedDownloader;
import org.crosswire.jsword.versification.BibleBook;

import java.net.URI;
import java.util.Map;

public class ContextualData {
    public final Map<String, String> args;
    public final Map<String, BibleBook> bookRefs;
    public final URI baseUri;
    public final CachedDownloader downloader;

    public ContextualData(Map<String, String> args, Map<String, BibleBook> bookRefs, URI baseUri, CachedDownloader downloader) {
        this.args = args;
        this.bookRefs = bookRefs;
        this.baseUri = baseUri;
        this.downloader = downloader;
    }
}
