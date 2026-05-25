package com.github.unaszole.bible.scraping.generic.index;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.valuetypes.StdValueTypes;
import com.github.unaszole.bible.downloading.CachedDownloader;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.parsing.ParserCore;
import com.github.unaszole.bible.parsing.WrappingParserCore;
import com.github.unaszole.bible.scraping.generic.ValueMapper;
import com.github.unaszole.bible.scraping.generic.ValueTypeMapper;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.TextParser;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;

public class IndexedContentParser implements ParserCore<IndexNode> {

    private final Map<String, TextParser> namedParsers;
    private final CachedDownloader cachedDownloader;
    private final ValueMapper mapper;

    public IndexedContentParser(Map<String, TextParser> namedParsers, CachedDownloader cachedDownloader, ValueMapper mapper) {
        this.namedParsers = namedParsers;
        this.cachedDownloader = cachedDownloader;
        this.mapper = mapper;
    }

    @Override
    public Parser<?> parseExternally(IndexNode indexNode, Deque<Context> currentContextStack) {
        Parser<?> contentParser;
        if(indexNode.page() != null) {
            // If the index node is a page, fetch its base URI and update the mappers that depend on it accordingly.
            URI baseUri = indexNode.page().sourceFile.getBaseUri();
            ValueMapper pageMapper = mapper
                    .withMapper(StdValueTypes.URI, new ValueTypeMapper.UriMapper(baseUri))
                    .withMapper(StdValueTypes.ATTACHMENT, new ValueTypeMapper.AttachmentMapper(cachedDownloader, baseUri));

            // Instantiate the proper parser for the page, passing this new value mapper.
            try {
                contentParser = Optional.ofNullable(namedParsers.get(indexNode.page().parserName))
                        .orElseThrow(() -> new RuntimeException("Could not find parser definition for " + indexNode.page().parserName))
                        .getLocalParser(
                                Files.newInputStream(cachedDownloader.getFile(indexNode.page().sourceFile)),
                                currentContextStack,
                                new ContextualData(indexNode.page().args, namedParsers, pageMapper)
                        );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            assert indexNode.childNodes() != null : "Index node should either be a page or have children.";
            // Else, the node is an internal node with children : instantiate an indexed content parser on the children list.
            contentParser = new Parser<>(this, indexNode.childrenIterator(), currentContextStack);
        }

        if(indexNode.wrappingContext() != null) {
            // If there is a wrapping context, wrap the parser behind a reader that blocks until the wrapping context can be opened.
            // TODO improve that : this does not close the wrapping context, so other contents might leak into it.
            // It would be better to use a terminal parser that closes its own context after processing.
            return WrappingParserCore.wrap(
                    cs -> contentParser,
                    List.of(
                            (as, type, pot) -> Optional.of(indexNode.wrappingContext())
                                    .filter(c -> c.metadata.type == type)
                    ),
                    Collections.emptyList(),
                    currentContextStack
            );
        }
        else {
            return contentParser;
        }
    }

    @Override
    public PositionParseOutput readContext(List<Context> ancestorStack, ContextType type, ContextMetadata previousOfType, IndexNode indexNode) {
        // Nothing to parse directly from the index node.
        return new PositionParseOutput(null, true);
    }
}
