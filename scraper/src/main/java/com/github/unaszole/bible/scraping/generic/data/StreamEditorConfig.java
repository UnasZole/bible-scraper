package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.idtypes.BibleIdFields;
import com.github.unaszole.bible.datamodel.idtypes.BibleIds;
import com.github.unaszole.bible.stream.ContextStream;
import com.github.unaszole.bible.stream.ContextStreamEditor;
import com.github.unaszole.bible.scraping.VersificationUpdater;
import org.crosswire.jsword.versification.BibleBook;

import java.util.List;
import java.util.stream.Collectors;

public class StreamEditorConfig {
    public static class Metadata {
        public ContextType type;
        public BibleBook book;
        public Integer chapter;
        public Integer verse;

        public ContextMetadata get(BibleBook defaultBook, int defaultChapter) {
            if(type.idType() == BibleIds.BOOK_ID) {
                return new ContextMetadata(type, BibleIds.BOOK_ID.ofFields(
                        BibleIdFields.BOOK.of(book != null ? book : defaultBook)
                ));
            }

            if(type.idType() == BibleIds.CHAPTER_ID) {
                return new ContextMetadata(type, BibleIds.CHAPTER_ID.ofFields(
                        BibleIdFields.BOOK.of(book != null ? book : defaultBook),
                        BibleIdFields.CHAPTER.of(chapter != null ? chapter : defaultChapter)
                ));
            }

            if(type.idType() == BibleIds.VERSE_ID) {
                return new ContextMetadata(type, BibleIds.VERSE_ID.ofFields(
                        BibleIdFields.BOOK.of(book != null ? book : defaultBook),
                        BibleIdFields.CHAPTER.of(chapter != null ? chapter : defaultChapter),
                        BibleIdFields.VERSES.of(List.of(verse))
                ));
            }

            return new ContextMetadata(type);
        }
    }

    public static class VersificationUpdate {
        public Integer shiftChapter;
        public Integer shiftVerse;

        public VersificationUpdater getUpdater() {
            VersificationUpdater updater = new VersificationUpdater();
            if (shiftChapter != null) {
                updater.chapterNb(m -> m.id.get(BibleIdFields.CHAPTER) + shiftChapter);
            }
            if (shiftVerse != null) {
                updater.verseNbs(m -> m.id.get(BibleIdFields.VERSES).stream()
                        .map(v -> v + shiftVerse)
                        .collect(Collectors.toList()));
            }
            return updater;
        }
    }

    public Metadata from;
    public Metadata to;
    public VersificationUpdate updateVersification;

    public <T extends ContextStream<T>> ContextStreamEditor<T> configureEditor(ContextStreamEditor<T> editor, BibleBook defaultBook, int defaultChapter) {
        ContextMetadata fromMeta = from.get(defaultBook, defaultChapter);
        ContextMetadata toMeta = to.get(defaultBook, defaultChapter);
        if (updateVersification != null) {
            editor.updateContexts(fromMeta, toMeta, updateVersification.getUpdater());
        }
        return editor;
    }
}
