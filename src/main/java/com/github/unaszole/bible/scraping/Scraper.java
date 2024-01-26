package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.implementations.TheoPlace;
import org.crosswire.jsword.versification.BibleBook;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class Scraper {

	public static class ContextStream {
		public final Context rootContext;
		private final Stream<ContextEvent> stream;

		/**
		 *
		 * @param rootContext The root context.
		 * @param stream The events contained within that context. (EXCLUDING the open/close of this context itself !)
		 *               Those will be added automatically.
		 */
		public ContextStream(Context rootContext, Stream<ContextEvent> stream) {
			this.rootContext = rootContext;
			this.stream = stream;
		}

		public static ContextStream fromSequence(Context rootContext, List<ContextStream> contextStreams) {
			List<Stream<ContextEvent>> streams = new ArrayList<>();

			for(final ContextStream contextStream: contextStreams) {
				if(contextStream.rootContext == rootContext) {
					// Stream contributes to the same root context directly.
					streams.add(contextStream.stream);
				}
				else {
					// Stream contributes to a child of the root.
					// We need to make sure to append the context to the root when we start streaming.
					// (hence why we add manually, and NOT with the getStream() public method).
					streams.add(Stream.of(new ContextEvent(ContextEvent.Type.OPEN, contextStream.rootContext)).peek(e -> {
							rootContext.addChild(contextStream.rootContext);
					}));
					streams.add(contextStream.stream);
					streams.add(Stream.of(new ContextEvent(ContextEvent.Type.CLOSE, contextStream.rootContext)));
				}
			}

			return new ContextStream(rootContext, streams.stream().flatMap(s -> s));
		}

		/**
		 *
		 * @return The stream of events for this context, including the OPEN and CLOSE events for this context.
		 */
		public Stream<ContextEvent> getStream() {
			return Stream.of(
					Stream.of(new ContextEvent(ContextEvent.Type.OPEN, rootContext)),
					stream,
					Stream.of(new ContextEvent(ContextEvent.Type.CLOSE, rootContext))
			).flatMap(s -> s);
		}
	}

	/**
	 *
	 * @param rootContextMeta Metadata for a potential root context.
	 * @return A context stream if the given context can indeed be used as root for a document, null otherwise.
	 */
	protected abstract ContextStream getContextStreamFor(ContextMetadata rootContextMeta);

	/**
	 *
	 * @return The list of books available in the bible exposed by this scraper, or null if unknown.
	 */
	protected abstract List<BibleBook> getBooks();

	/**
	 *
	 * @param book A book present in this bible.
	 * @return The number of chapters in this book, or null if unknown.
	 */
	protected abstract int getNbChapters(BibleBook book);

	private ContextStream getDefaultedContextStreamFor(ContextMetadata rootContextMeta) {
		ContextStream fromImpl = getContextStreamFor(rootContextMeta);
		if(fromImpl != null) {
			return fromImpl;
		}

		List<ContextStream> contextStreams;
		switch(rootContextMeta.type) {
			case BOOK:
				Context bookCtx = new Context(rootContextMeta);
				contextStreams = new ArrayList<>();
				int nbChapters = getNbChapters(rootContextMeta.book);
				if(nbChapters == -1) {
					// If we don't know the number of chapters, stop here and try at higher level.
					return null;
				}
				for(int i = 1; i <= nbChapters; i++) {
					ContextStream chapterStream = getDefaultedContextStreamFor(ContextMetadata.forChapter(rootContextMeta.book, i));
					if(chapterStream == null) {
						// If we failed to build a context for the chapter, stop here and try at higher level.
						return null;
					}
					contextStreams.add(chapterStream);
				}
				return ContextStream.fromSequence(bookCtx, contextStreams);

			case BIBLE:
				Context bibleCtx = new Context(rootContextMeta);
				contextStreams = new ArrayList<>();
				List<BibleBook> books = getBooks();
				if(books == null) {
					// If we don't know the list of books, stop here and try at higher level.
					return null;
				}
				for(BibleBook book: books) {
					ContextStream bookStream = getDefaultedContextStreamFor(ContextMetadata.forBook(book));
					if(bookStream == null) {
						// If we failed to build a context for the book, stop here and try at higher level.
						return null;
					}
					contextStreams.add(bookStream);
				}
				return ContextStream.fromSequence(bibleCtx, contextStreams);
		}
		return null;
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
	public final Stream<ContextEvent> stream(ContextMetadata wantedContext) {
		ContextStream contextStream = getDefaultedContextStreamFor(wantedContext);
		ContextMetadata rootContextMeta = wantedContext;
		while(contextStream == null && rootContextMeta != null) {
			rootContextMeta = getAncestor(rootContextMeta);
			contextStream = getDefaultedContextStreamFor(rootContextMeta);
		}

		return ParsingUtils.extract(contextStream.getStream(), wantedContext);
	}

	/**
	 * Fetch bible contents from a remote source.
	 * @return The context with requested metadata, or null if it couldn't be found.
	 */
	public final Context fetch(ContextMetadata wantedContext) {
		return stream(wantedContext)
				//.peek(e -> System.out.println("####### " + e))
				.filter(e -> e.type == ContextEvent.Type.CLOSE && Objects.equals(e.context.metadata, wantedContext))
				.findAny()
				.map(e -> e.context)
				.orElse(null);
	}
}