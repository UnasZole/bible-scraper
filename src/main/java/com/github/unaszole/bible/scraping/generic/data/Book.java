package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.downloading.SourceFile;
import com.github.unaszole.bible.stream.ContextStream;
import com.github.unaszole.bible.stream.ContextStreamEditor;
import org.crosswire.jsword.versification.BibleBook;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Specifies the contents of a book to retrieve from the source.
 *
 * Pages defined at book level, if any, will be scraped to initialise this book's context - else an empty book context
 * will be initialised.
 * If {@link #chapters} are specified, the corresponding pages will be scraped and appended at the end of the book context.
 */
public class Book extends PagesContainer {

    /**
     * The OSIS ID of the book.
     */
    public BibleBook osis;

    /**
     * The names under which this book is referenced within the bible. This is used when parsing cross-references.
     */
    public List<String> names;

    /**
     * Description of this book's contents as a sequence of chapters.
     * May be left empty if the {@link #pages} already contain all chapters of the book.
     */
    public List<ChapterSeq> chapters;
    /**
     * Configuration for a stream editor.
     */
    public List<StreamEditorConfig> edit;

    public ChapterSeq getChapterSeq(int chapterNb) {
        if(chapters == null) {
            return null;
        }
        for (ChapterSeq seq : chapters) {
            if (seq.containsChapter(chapterNb)) {
                return seq;
            }
        }
        return null;
    }

    public List<SourceFile> getBookFiles(PatternContainer bookDefaults, SourceFile.Builder sourceFileBuilder) {
        return getPageFiles(bookDefaults, a -> a, "book", sourceFileBuilder);
    }

    public ContextStream.Single streamBook(PatternContainer bibleDefaults, ContextMetadata bookCtxMeta,
                                           final SourceFile.Builder sourceFileBuilder,
                                           final BiFunction<Context, List<SourceFile>, ContextStream.Single> ctxStreamer) {
        assert bookCtxMeta.type == ContextType.BOOK && bookCtxMeta.book == osis;
        final PatternContainer bookDefaults = this.defaultedBy(bibleDefaults);

        // Build the list of context streams for all chapters, to append to the book page stream.
        List<ContextStream.Single> chapterStreams;
        if(chapters != null && !chapters.isEmpty()) {
            // If we have a chapter structure defined, build context streams for each.
            chapterStreams = chapters.stream()
                    .flatMap(seq -> seq.listChapters()
                            .map(chapterNb -> seq.streamChapter(
                                    bookDefaults,
                                    ContextMetadata.forChapter(osis, chapterNb),
                                    sourceFileBuilder,
                                    ctxStreamer
                            )))
                    .collect(Collectors.toList());
        }
        else {
            // No information on chapters, nothing to append to the book page stream.
            chapterStreams = List.of();
        }

        // Build the book stream.
        Context bookCtx = new Context(bookCtxMeta, bookCtxMeta.book.getOSIS());
        ContextStream.Single bookStream = null;

        List<SourceFile> bookFiles = getBookFiles(bookDefaults, sourceFileBuilder);
        if(!bookFiles.isEmpty()) {
            // We have pages for this book, prepare a context with the given streamer and append chapters at the end.
            bookStream = ctxStreamer.apply(bookCtx, bookFiles).edit().inject(
                    ContextStreamEditor.InjectionPosition.AT_END, bookCtxMeta, chapterStreams
            ).process();
        }
        else if(!chapterStreams.isEmpty()) {
            // If we don't have a book page but we have chapter contents, just aggregate them.
            bookStream = ContextStream.fromContents(bookCtx, chapterStreams);
        }

        // Configure editor if provided and we have a book to return.
        if(bookStream != null && edit != null) {
            ContextStreamEditor<ContextStream.Single> editor = bookStream.edit();
            for(StreamEditorConfig cfg: edit) {
                cfg.configureEditor(editor, bookCtxMeta.book, 0);
            }
            bookStream = editor.process();
        }

        return bookStream;
    }
}
