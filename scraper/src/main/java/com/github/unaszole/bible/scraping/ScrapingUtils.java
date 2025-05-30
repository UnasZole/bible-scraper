package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.*;
import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.datamodel.contexttypes.FlatText;
import com.github.unaszole.bible.datamodel.idtypes.BibleIdFields;
import com.github.unaszole.bible.datamodel.idtypes.BibleIds;
import com.github.unaszole.bible.datamodel.valuetypes.IntegerValue;
import com.github.unaszole.bible.parsing.Context;
import org.crosswire.jsword.versification.BibleBook;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ScrapingUtils {

    public static ContextMetadata forBible() {
        return new ContextMetadata(BibleContainers.BIBLE);
    }

    public static ContextMetadata forBook(BibleBook book) {
        return new ContextMetadata(BibleContainers.BOOK, BibleIds.BOOK_ID.ofFields(
                BibleIdFields.BOOK.of(book)
        ));
    }

    public static ContextMetadata forChapter(BibleBook book, int chapter) {
        return new ContextMetadata(BibleContainers.CHAPTER, BibleIds.CHAPTER_ID.ofFields(
                BibleIdFields.BOOK.of(book),
                BibleIdFields.CHAPTER.of(chapter)
        ));
    }

    public static ContextMetadata forVerse(BibleBook book, int chapter, int verse) {
        return new ContextMetadata(BibleContainers.VERSE, BibleIds.VERSE_ID.ofFields(
                BibleIdFields.BOOK.of(book),
                BibleIdFields.CHAPTER.of(chapter),
                BibleIdFields.VERSES.of(List.of(verse))
        ));
    }

    /**
     * Utility method for parsers : check if the current context is a descendant of a context of a given type.
     * @param searchedAncestorType The type of ancestor to search for.
     * @param ancestors The ancestor contexts, potentially implicit (first element is the direct parent).
     * @return True if an ancestor of the searched type is present, false otherwise.
     */
    public static boolean hasAncestor(ContextType searchedAncestorType, Collection<Context> ancestors) {
        return ancestors.stream().anyMatch(a -> a.metadata.type == searchedAncestorType);
    }

    public static boolean isInVerseText(Collection<Context> ancestors) {
        return hasAncestor(BibleContainers.VERSE, ancestors) && hasAncestor(FlatText.FLAT_TEXT, ancestors);
    }

    public static ContextMetadata getChapterMetadata(List<Context> ancestorStack,
                                                     ContextMetadata previousChapter,
                                                     String parsedNb) {
        // Expected chapter ID based on the previous and ancestors.
        ContextId expectedChapterId = BibleIds.CHAPTER_ID.getNewId(previousChapter, ancestorStack)
                .orElseThrow(() -> new IllegalArgumentException("Could not find a next chapter ID following" + previousChapter + " with " + ancestorStack));

        // Parse the received chapter number.
        try {
            // Parseable number : use the maximum between this and the expected (to handle skipped chapters).
            int nb = IntegerValue.parseInt(parsedNb);
            return new ContextMetadata(BibleContainers.CHAPTER, BibleIds.CHAPTER_ID.ofFields(
                    BibleIdFields.BOOK.of(expectedChapterId.get(BibleIdFields.BOOK)),
                    BibleIdFields.CHAPTER.of(Math.max(nb, expectedChapterId.get(BibleIdFields.CHAPTER)))
            ));
        }
        catch (NumberFormatException e) {
            // Unparseable number : just return the expected.
            return new ContextMetadata(BibleContainers.CHAPTER, expectedChapterId);
        }
    }

    public static ContextMetadata getVerseMetadata(List<Context> ancestorStack, ContextMetadata previousVerse, String parsedNb) {
        ContextId expectedVerseId = BibleIds.VERSE_ID.getNewId(previousVerse, ancestorStack)
                .orElseThrow(() -> new IllegalArgumentException("Could not find a next verse ID following" + previousVerse + " with " + ancestorStack));

        // A number may contain a dash, denoting an interval.
        String[] parsedNbs = parsedNb.split("-");
        try {
            int startNb = IntegerValue.parseInt(parsedNbs[0]);
            int nbAdditionalVerses = 0;
            if(parsedNbs.length > 1) {
                int endNb = IntegerValue.parseInt(parsedNbs[1]);
                nbAdditionalVerses = endNb - startNb;
            }

            int realStartNb = Math.max(startNb, expectedVerseId.get(BibleIdFields.VERSES).get(0));

            return new ContextMetadata(BibleContainers.VERSE, BibleIds.VERSE_ID.ofFields(
                    BibleIdFields.BOOK.of(expectedVerseId.get(BibleIdFields.BOOK)),
                    BibleIdFields.CHAPTER.of(expectedVerseId.get(BibleIdFields.CHAPTER)),
                    BibleIdFields.VERSES.of(IntStream.rangeClosed(realStartNb, realStartNb + nbAdditionalVerses)
                            .boxed()
                            .collect(Collectors.toList())
                    )
            ));
        }
        catch(NumberFormatException e) {
            // Unparseable number : just return the expected.
            return new ContextMetadata(BibleContainers.VERSE, expectedVerseId);
        }
    }
}
