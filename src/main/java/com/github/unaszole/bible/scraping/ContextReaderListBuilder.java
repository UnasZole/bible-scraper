package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class ContextReaderListBuilder {

    public static ContextReaderListBuilder context(final BiFunction<Deque<ContextMetadata>, ContextMetadata,
            ContextMetadata> buildMeta, final String value, ContextReaderListBuilder descendants) {
        return new ContextReaderListBuilder().followedBy(buildMeta, value, descendants);
    }

    public static ContextReaderListBuilder context(final ContextMetadata meta, final String value,
                                                   ContextReaderListBuilder descendants) {
        return context((as, pot) -> meta, value, descendants);
    }

    public static ContextReaderListBuilder context(final ContextMetadata meta, final String value) {
        return context(meta, value, null);
    }

    public static ContextReaderListBuilder context(final ContextMetadata meta, final ContextReaderListBuilder descendants) {
        return context(meta, null, descendants);
    }

    public static ContextReaderListBuilder context(final ContextMetadata meta) {
        return context(meta, null, null);
    }

    private final List<PositionBufferedParserCore.ContextReader> readers = new ArrayList<>();

    private final ContextMetadata[] lastObjectMeta = new ContextMetadata[] { null };

    public ContextReaderListBuilder followedBy(final BiFunction<Deque<ContextMetadata>, ContextMetadata,
            ContextMetadata> buildMeta, final String value, ContextReaderListBuilder descendants) {
        // Add a reader for this context.
        readers.add((as, type, pot) -> {
            ContextMetadata meta = buildMeta.apply(as, pot);
            if(type == meta.type && !as.contains(lastObjectMeta[0])) {
                // If the requested type matches the one provided by the builder
                // AND we are outside the previous sibling, then we return a context.
                lastObjectMeta[0] = meta;
                return Optional.of(new Context(meta, value));
            }
            return Optional.empty();
        });

        if(descendants != null) {
            // Add all readers for its descendants if any.
            readers.addAll(descendants.build());
        }

        return this;
    }

    public ContextReaderListBuilder followedBy(final ContextMetadata meta, final String value,
                                               ContextReaderListBuilder descendants) {
        return followedBy((as, pot) -> meta, value, descendants);
    }

    public ContextReaderListBuilder followedBy(final ContextMetadata meta, final String value) {
        return followedBy(meta, value, null);
    }

    public ContextReaderListBuilder followedBy(final ContextMetadata meta, final ContextReaderListBuilder descendants) {
        return followedBy(meta, null, descendants);
    }

    public ContextReaderListBuilder followedBy(final ContextMetadata meta) {
        return followedBy(meta, null, null);
    }

    public List<PositionBufferedParserCore.ContextReader> build() {
        return readers;
    }
}
