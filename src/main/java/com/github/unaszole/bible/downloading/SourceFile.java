package com.github.unaszole.bible.downloading;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

/**
 * A source file to be fetched by the scraper.
 */
public interface SourceFile {
    interface Builder {
        /**
         *
         * @param propertySource Function to resolve the value of a property, by name.
         * @return A source file, or empty optional if no file could be determined from the given properties.
         */
        Optional<SourceFile> buildFrom(Function<String, Optional<String>> propertySource);
    }

    /**
     *
     * @return A unique hash for this source file, that may be used for local caching.
     * When another file with the same hash is requested, cache may be reused.
     */
    String getHash();

    /**
     * Fetch the source file and open it for reading.
     * @return An input stream to read the file.
     * @throws IOException If anything prevented to read the source file.
     */
    InputStream openStream() throws IOException;
}
