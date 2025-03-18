package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.writing.datamodel.DocumentMetadata;
import com.github.unaszole.bible.downloading.SourceFile;
import com.github.unaszole.bible.stream.ContextStream;
import com.github.unaszole.bible.stream.ContextStreamEditor;
import org.crosswire.jsword.versification.BibleBook;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Bible extends PagesContainer {
    public BibleMetadata metadata;
    public List<Book> books;

    /**
     * Configuration for a stream editor.
     */
    public List<StreamEditorConfig> edit;

    public DocumentMetadata getDocMeta(PatternContainer globalDefaults) {
        final PatternContainer bibleDefaults = this.defaultedBy(globalDefaults);
        BibleMetadata meta = metadata != null ? metadata : new BibleMetadata();
        return meta.getDocMeta(bibleDefaults.args);
    }

    public List<BibleBook> getBooks() {
        return books.stream().map(b -> b.osis).collect(Collectors.toList());
    }

    public Book getBook(BibleBook book) {
        return books.stream()
                .filter(b -> b.osis == book)
                .findAny()
                .orElse(null);
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

    public List<SourceFile> getBibleFiles(PatternContainer bibleDefaults, SourceFile.Builder sourceFileBuilder) {
        return getPageFiles(bibleDefaults, a -> a, "bible", sourceFileBuilder);
    }

    public ContextStream.Single streamBible(PatternContainer globalDefaults, ContextMetadata bibleCtxMeta,
                                            final SourceFile.Builder sourceFileBuilder,
                                            final BiFunction<Context, List<SourceFile>, ContextStream.Single> ctxStreamer) {
        assert bibleCtxMeta.type == ContextType.BIBLE;
        final PatternContainer bibleDefaults = this.defaultedBy(globalDefaults);

        // Build the list of context streams for all books, to append to the bible page stream.
        List<ContextStream.Single> bookStreams;
        if(books != null && !books.isEmpty()) {
            // If we have a books defined, build context streams for each.
            bookStreams = books.stream()
                    .map(book -> book.streamBook(bibleDefaults, ContextMetadata.forBook(book.osis),
                            sourceFileBuilder, ctxStreamer))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        else {
            // No information on books, nothing to append to the bible page stream.
            bookStreams = List.of();
        }

        // Build the bible stream.
        Context bibleCtx = new Context(bibleCtxMeta);
        ContextStream.Single bibleStream = null;

        List<SourceFile> bibleFiles = getBibleFiles(bibleDefaults, sourceFileBuilder);
        if(!bibleFiles.isEmpty()) {
            // We have pages for this bible, prepare a context with the given streamer and append books at the end.
            bibleStream = ctxStreamer.apply(bibleCtx, bibleFiles).edit().inject(
                    ContextStreamEditor.InjectionPosition.AT_END, bibleCtxMeta, bookStreams
            ).process();
        }
        else if(!bookStreams.isEmpty()) {
            // If we don't have a book page but we have chapter contents, just aggregate them.
            bibleStream = ContextStream.fromContents(bibleCtx, bookStreams);
        }

        // Configure editor if provided and we have a bible to return.
        if(bibleStream != null && edit != null) {
            ContextStreamEditor<ContextStream.Single> editor = bibleStream.edit();
            for(StreamEditorConfig cfg: edit) {
                cfg.configureEditor(editor, null, 0);
            }
            bibleStream = editor.process();
        }

        return bibleStream;
    }
}
