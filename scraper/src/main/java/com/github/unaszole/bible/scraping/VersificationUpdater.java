package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.datamodel.idtypes.BibleIdFields;
import com.github.unaszole.bible.stream.ContextEvent;
import com.github.unaszole.bible.stream.ContextStreamEditor;
import org.crosswire.jsword.versification.BibleBook;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class VersificationUpdater extends ContextStreamEditor.ContextUpdater {

    private Function<ContextMetadata, String> chapterValueUpdater = null;
    private Function<ContextMetadata, String> verseValueUpdater = null;

    public VersificationUpdater book(Function<ContextMetadata, BibleBook> bookUpdater) {
        return (VersificationUpdater) idField(BibleIdFields.BOOK, (m, v) -> Optional.of(bookUpdater.apply(m)));
    }

    public VersificationUpdater chapterNb(ToIntFunction<ContextMetadata> chapterNbUpdater) {
        return (VersificationUpdater) idField(BibleIdFields.CHAPTER, (m, v) -> Optional.of(chapterNbUpdater.applyAsInt(m)));
    }

    public VersificationUpdater chapterValue(Function<ContextMetadata, String> chapterValueUpdater) {
        this.chapterValueUpdater = chapterValueUpdater;
        return this;
    }

    public VersificationUpdater verseNbs(Function<ContextMetadata, List<Integer>> verseNbsUpdater) {
        return (VersificationUpdater) idField(BibleIdFields.VERSES, (m, v) -> Optional.of(verseNbsUpdater.apply(m)));
    }

    public VersificationUpdater verseValue(Function<ContextMetadata, String> verseValueUpdater) {
        this.verseValueUpdater = verseValueUpdater;
        return this;
    }

    @Override
    public ContextEvent apply(ContextEvent e) {
        if (chapterValueUpdater != null || verseValueUpdater != null) {
            // If we have any value updater, register it.
            value((m, v) -> {
                if (m.type == BibleContainers.CHAPTER && chapterValueUpdater != null) {
                    return Optional.of(chapterValueUpdater.apply(m));
                }
                if (m.type == BibleContainers.VERSE && verseValueUpdater != null) {
                    return Optional.of(verseValueUpdater.apply(m));
                }
                return Optional.empty();
            });
        }

        return super.apply(e);
    }
}
