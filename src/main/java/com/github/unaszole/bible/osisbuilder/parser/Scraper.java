package com.github.unaszole.bible.osisbuilder.parser;

import org.crosswire.jsword.versification.BibleBook;

public interface Scraper {
	/**
	 * Fetch bible contents from a remote source.
	 * @param wantedContext Metadata of the wanted contents. Use {@link ContextMetadata#forBible()} to fetch all bible
	 *                      contents available through this scraper, or a lower specified one (eg.
	 *                      {@link ContextMetadata#forBook(BibleBook)},
	 *                      {@link ContextMetadata#forChapter(BibleBook, int)} ) to retrieve this specific subset of
	 *                      bible data.
	 * @return The context with requested metadata, or null if it couldn't be found.
	 */
	Context fetch(ContextMetadata wantedContext);
}