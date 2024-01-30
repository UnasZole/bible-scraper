package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.stream.ContextStream;
import com.github.unaszole.bible.datamodel.ContextType;
import org.crosswire.jsword.versification.BibleBook;

import java.util.ArrayList;
import java.util.List;

public abstract class Scraper {

	/**
	 *
	 * @param rootContextMeta Metadata for a requested root context.
	 * @return A context stream for the contents of the requested context, or null.
	 * If null, the scraper will try to fetch a parent context and extract the wanted portion.
	 */
	protected abstract ContextStream.Single getContextStreamFor(ContextMetadata rootContextMeta);

	/**
	 * Utility method for scrapers to automatically generate a book from its chapters.
	 * This should be used only if we don't need to fetch a book introduction.
	 * @param book The book to fetch.
	 * @param nbChapters The number of chapters in this book.
	 * @return The context stream for the book containing all these chapters.
	 */
	protected ContextStream.Single autoGetBookStream(BibleBook book, int nbChapters) {
		Context bookCtx = new Context(ContextMetadata.forBook(book));
		List<ContextStream.Single> contextStreams = new ArrayList<>();
		for(int i = 1; i <= nbChapters; i++) {
			contextStreams.add(getContextStreamFor(ContextMetadata.forChapter(book, i)));
		}
		return ContextStream.fromContents(bookCtx, contextStreams);
	}

	/**
	 * Utility method for scrapers to automatically generate a bible from a sequence of books.
	 * @param books The list of books to include, in order.
	 * @return The context stream for a bible containing all these books.
	 */
	protected ContextStream.Single autoGetBibleStream(List<BibleBook> books) {
		Context bibleCtx = new Context(ContextMetadata.forBible());
		List<ContextStream.Single> contextStreams = new ArrayList<>();
		for(BibleBook book: books) {
			ContextStream.Single bookStream = getContextStreamFor(ContextMetadata.forBook(book));
			if(bookStream != null) {
				contextStreams.add(bookStream);
			}
		}
		return ContextStream.fromContents(bibleCtx, contextStreams);
	}

	private static ContextMetadata getAncestor(ContextMetadata meta) {
		if(meta.type != ContextType.CHAPTER && meta.chapter > 0) {
			return ContextMetadata.forChapter(meta.book, meta.chapter);
		}
		else if(meta.type != ContextType.BOOK && meta.book != null) {
			return ContextMetadata.forBook(meta.book);
		}
		else if(meta.type != ContextType.BIBLE) {
			return ContextMetadata.forBible();
		}
		return null;
	}

	/**
	 * Scrape and stream bible contents from a remote source.
	 * @param wantedContext The wanted context. If requesting just a subset (a book, a chapter...), the scraper may
	 *                      be able to avoid fetching the full bible contents.
	 * @return The stream of context events, starting with the opening of the wanted context and ending with its closure.
	 * Empty stream if the wanted context was not found.
	 */
	public final ContextStream.Single stream(ContextMetadata wantedContext) {
		ContextStream contextStream = getContextStreamFor(wantedContext);
		ContextMetadata rootContextMeta = wantedContext;
		while(contextStream == null && rootContextMeta != null) {
			rootContextMeta = getAncestor(rootContextMeta);
			contextStream = getContextStreamFor(rootContextMeta);
		}

		return contextStream.extractStream(wantedContext);
	}
}