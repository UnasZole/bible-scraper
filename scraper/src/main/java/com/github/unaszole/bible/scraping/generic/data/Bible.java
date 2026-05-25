package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.scraping.generic.ValueMapper;
import com.github.unaszole.bible.scraping.generic.index.IndexNode;
import com.github.unaszole.bible.scraping.generic.index.providers.Container;
import com.github.unaszole.bible.scraping.generic.index.providers.ContextProvider;
import com.github.unaszole.bible.scraping.generic.PatternConfig;
import com.github.unaszole.bible.writing.datamodel.DocumentMetadata;
import org.crosswire.jsword.versification.BibleBook;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bible extends PagesContainer {
    private static final Map<String, ContextType> CONTEXT_TYPES = BibleContainers.BIBLE.descendantTypesByName();

    public BibleMetadata metadata;
    public List<Book> books;

    /**
     * Configuration for a stream editor.
     */
    public List<StreamEditorConfig> edit;

    public DocumentMetadata getDocMeta(PatternConfig globalDefaults) {
        final PatternConfig bibleDefaults = this.asPatternConfig().defaultedBy(globalDefaults);
        BibleMetadata meta = metadata != null ? metadata : new BibleMetadata();
        return meta.getDocMeta(bibleDefaults.arguments());
    }

    public Map<String, BibleBook> getBookReferences() {
        return books.stream()
                .filter(b -> b.names != null)
                .flatMap(b -> b.names.stream().map(
                        n -> Map.entry(n, b.osis)
                ))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public Container asContainer() {
        PatternConfig biblePatternConfig = new PatternConfig(
                Map.of(),
                Map.of(),
                "bible"
        );

        return new Container(
                new ContextProvider(
                        BibleContainers.BIBLE.name(),
                        Map.of(),
                        null
                ),
                asPatternConfig().defaultedBy(biblePatternConfig),
                Stream.concat(
                        getPatternPages(),
                        Optional.ofNullable(books).stream()
                                .flatMap(Collection::stream)
                                .map(Book::asContainer)
                ).toList()
        );
    }

    public IndexNode getRootNode(PatternConfig globalDefaults, ValueMapper mapper) {
        return asContainer().getNodes(
                globalDefaults,
                CONTEXT_TYPES,
                Map.of(),
                mapper
        ).get(0);
    }
}
