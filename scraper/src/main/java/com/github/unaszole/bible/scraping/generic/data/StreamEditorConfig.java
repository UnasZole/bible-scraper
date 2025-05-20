package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.IdField;
import com.github.unaszole.bible.datamodel.IdType;
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
            switch(type.idType()) {
                case BIBLE_BOOK:
                    return new ContextMetadata(type, IdType.BIBLE_BOOK.ofFields(
                            IdField.BIBLE_BOOK.of(book != null ? book : defaultBook)
                    ));
                case BIBLE_CHAPTER:
                    return new ContextMetadata(type, IdType.BIBLE_CHAPTER.ofFields(
                            IdField.BIBLE_BOOK.of(book != null ? book : defaultBook),
                            IdField.BIBLE_CHAPTER.of(chapter != null ? chapter : defaultChapter)
                    ));
                case BIBLE_VERSE:
                    return new ContextMetadata(type, IdType.BIBLE_VERSE.ofFields(
                            IdField.BIBLE_BOOK.of(book != null ? book : defaultBook),
                            IdField.BIBLE_CHAPTER.of(chapter != null ? chapter : defaultChapter),
                            IdField.BIBLE_VERSES.of(List.of(verse))
                    ));
                default:
                    return new ContextMetadata(type);
            }
        }
    }

    public static class VersificationUpdate {
        public Integer shiftChapter;
        public Integer shiftVerse;

        public VersificationUpdater getUpdater() {
            VersificationUpdater updater = new VersificationUpdater();
            if (shiftChapter != null) {
                updater.chapterNb(m -> (Integer)m.id.get(IdField.BIBLE_CHAPTER) + shiftChapter);
            }
            if (shiftVerse != null) {
                updater.verseNbs(m -> ((List<Integer>)m.id.get(IdField.BIBLE_VERSES)).stream()
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
