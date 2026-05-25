package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.datamodel.idtypes.BibleIdFields;
import com.github.unaszole.bible.scraping.generic.PatternConfig;
import com.github.unaszole.bible.scraping.generic.index.providers.*;

import java.util.Map;

/**
 * Specifies a sequence of chapters from this book.
 * In a chapter sequence only (and its included pages), the arguments may be specified as simple integer expressions :
 * these must start by "=" and be of the form "= $i + 1" or "= $i - 2", where $i is the OSIS number of the chapter.
 */
public class ChapterSeq extends PagesContainer {
    /**
     * The OSIS number of this chapter, if this sequence represents a single chapter.
     * If given, then {@link #from} and {@link #to} MUST be omitted.
     */
    public Integer at;
    /**
     * The first OSIS chapter number of this sequence.
     * If given, then {@link #at} MUST be omitted, and {@link #to} MUST be provided.
     */
    public Integer from;
    /**
     * The last OSIS chapter number of this sequence.
     * If given, then {@link #at} MUST be omitted, and {@link #from} MUST be provided.
     */
    public Integer to;

    public Iteration asIteration() {
        PatternConfig chapterPatternConfig = new PatternConfig(
                Map.of("chapterId", "{CHAPTER_NUMBER}"),
                Map.of("CHAPTER_NUMBER", "= $i"),
                "chapter"
        );

        return new Iteration(
                new Iteration.IterationVariable("i", at != null ? at : from, at != null ? at : to),
                new Container(
                        new ContextProvider(
                                BibleContainers.CHAPTER.name(),
                                Map.of(BibleIdFields.CHAPTER.name(), "Id"),
                                "PublishedNumber"
                        ),
                        asPatternConfig().defaultedBy(chapterPatternConfig),
                        getPatternPages().toList()
                )
        );
    }
}
