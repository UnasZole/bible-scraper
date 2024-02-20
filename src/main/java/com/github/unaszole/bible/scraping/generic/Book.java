package com.github.unaszole.bible.scraping.generic;

import org.crosswire.jsword.versification.BibleBook;

import java.net.URL;
import java.util.List;

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

    public List<URL> getUrls(PatternContainer bookDefaults) {
        return getPageUrls(bookDefaults, a -> a);
    }
}
