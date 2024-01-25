package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;

import java.util.Objects;
import java.util.stream.Stream;

public interface Scraper {

	/**
	 * Scrape and stream bible contents from a remote source.
	 * @param wantedContext The wanted context. If requesting just a subset (a book, a chapter...), the scraper may
	 *                      be able to avoid fetching the full bible contents.
	 * @return The stream of context events, starting with the opening of the wanted context and ending with its closure.
	 * Empty stream if the wanted context was not found.
	 */
	Stream<ContextEvent> stream(ContextMetadata wantedContext);

	/**
	 * Fetch bible contents from a remote source.
	 * @return The context with requested metadata, or null if it couldn't be found.
	 */
	default Context fetch(ContextMetadata wantedContext) {
		return stream(wantedContext)
				.filter(e -> e.type == ContextEvent.Type.CLOSE && Objects.equals(e.context.metadata, wantedContext))
				.findAny()
				.map(e -> e.context)
				.orElse(null);
	}
}