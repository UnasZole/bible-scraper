package com.github.unaszole.bible.scraping.generic.parsing;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.downloading.CachedDownloader;
import com.github.unaszole.bible.downloading.SourceFile;
import com.github.unaszole.bible.scraping.Parser;
import com.github.unaszole.bible.scraping.ParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.html.HtmlParserProvider;
import com.github.unaszole.bible.stream.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TextParser {

    public interface Provider<Position> {
        Iterator<Position> iterate(InputStream input) throws IOException;
        ParserCore<Position> getParser(ContextualData contextualData);
    }

    public HtmlParserProvider html;

    private Provider<?> getProvider() {
        if(html != null) {
            return html;
        }
        throw new IllegalArgumentException("No parser provided !");
    }

    private <P> Parser<P> getLocalParser(Provider<P> provider, InputStream input,
                                        Deque<Context> currentContextStack, ContextualData contextualData) {
        try {
            return new Parser<>(provider.getParser(contextualData), provider.iterate(input), currentContextStack);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Parser<?> getLocalParser(InputStream input,
                                    Deque<Context> currentContextStack, ContextualData contextualData) {
        return getLocalParser(getProvider(), input, currentContextStack, contextualData);
    }

    private <P> Parser.TerminalParser<P> getFileParser(Provider<P> provider,
                                                       CachedDownloader cachedDownloader, List<SourceFile> sourceFiles,
                                                       Context rootContext, ContextualData contextualData) {
        return new Parser.TerminalParser<>(provider.getParser(contextualData), StreamUtils.concatIterators(
                sourceFiles.stream()
                        .map(sf -> StreamUtils.deferredIterator(
                                () -> {
                                    try {
                                        return provider.iterate(Files.newInputStream(cachedDownloader.getFile(sf)));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        ))
                        .collect(Collectors.toList())
        ), rootContext);
    }

    public Parser.TerminalParser<?> getFileParser(CachedDownloader cachedDownloader, List<SourceFile> sourceFiles,
                                                  Context rootContext, ContextualData contextualData) {
        return getFileParser(getProvider(), cachedDownloader, sourceFiles, rootContext, contextualData);
    }
}
