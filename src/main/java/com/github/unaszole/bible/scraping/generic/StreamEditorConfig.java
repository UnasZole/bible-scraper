package com.github.unaszole.bible.scraping.generic;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.stream.ContextStream;
import com.github.unaszole.bible.stream.ContextStreamEditor;
import org.crosswire.jsword.versification.BibleBook;

import java.util.Arrays;

public class StreamEditorConfig {
    public static class Metadata {
        public ContextType type;
        public BibleBook book;
        public Integer chapter;
        public Integer verse;

        public ContextMetadata get(BibleBook defaultBook, int defaultChapter) {
            return new ContextMetadata(type,
                    book != null ? book : defaultBook,
                    chapter != null ? chapter : defaultChapter,
                    verse
            );
        }
    }

    public static class VersificationUpdate {
        public Integer shiftChapter;
        public Integer shiftVerse;

        public ContextStreamEditor.VersificationUpdater getUpdater() {
            ContextStreamEditor.VersificationUpdater updater = new ContextStreamEditor.VersificationUpdater();
            if (shiftChapter != null) {
                updater.chapterNb(m -> m.chapter + shiftChapter);
            }
            if (shiftVerse != null) {
                updater.verseNbs(m -> Arrays.stream(m.verses).map(v -> v + shiftVerse).toArray());
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
            editor.updateVersification(fromMeta, toMeta, updateVersification.getUpdater());
        }
        return editor;
    }
}
