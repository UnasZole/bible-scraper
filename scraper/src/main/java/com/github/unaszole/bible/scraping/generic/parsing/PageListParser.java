package com.github.unaszole.bible.scraping.generic.parsing;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.downloading.CachedDownloader;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.parsing.ParserCore;
import com.github.unaszole.bible.scraping.generic.data.PageData;
import org.crosswire.jsword.versification.BibleBook;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PageListParser implements ParserCore<PageData> {

    private final Map<String, TextParser> namedParsers;
    private final CachedDownloader cachedDownloader;
    private final Map<String, BibleBook> bookReferences;

    public PageListParser(Map<String, TextParser> namedParsers, CachedDownloader cachedDownloader,
                          Map<String, BibleBook> bookReferences) {
        this.namedParsers = namedParsers;
        this.cachedDownloader = cachedDownloader;
        this.bookReferences = bookReferences;
    }

    @Override
    public Parser<?> parseExternally(PageData pageData, Deque<Context> currentContextStack) {
        // Each page data is parsed externally by a dedicated local parser.
        try {
            return Optional.ofNullable(namedParsers.get(pageData.parserName))
                    .orElseThrow(() -> new RuntimeException("Could not find parser definition for " + pageData.parserName))
                    .getLocalParser(
                            Files.newInputStream(cachedDownloader.getFile(pageData.sourceFile)),
                            currentContextStack,
                            new ContextualData(pageData.args, bookReferences, namedParsers, pageData.sourceFile.getBaseUri(), cachedDownloader)
                    );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PositionParseOutput readContext(List<Context> ancestorStack, ContextType type, ContextMetadata previousOfType, PageData pageData) {
        // Nothing to parse directly from the page data.
        return new PositionParseOutput(null, true);
    }
}
