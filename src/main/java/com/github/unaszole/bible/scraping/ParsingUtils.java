package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingUtils {

    /**
     * Utility method for parsers : check if the current context is a descendant of a context of a given type.
     * @param searchedAncestorType The type of ancestor to search for.
     * @param ancestors The metadata of the ancestor contexts, potentially implicit (first element is the direct parent).
     * @return True if an ancestor of the searched type is present, false otherwise.
     */
    public static boolean hasAncestor(ContextType searchedAncestorType, Deque<ContextMetadata> ancestors) {
        return ancestors.stream().anyMatch(a -> a.type == searchedAncestorType);
    }

    public static boolean hasAncestorCtx(ContextType searchedAncestorType, Deque<Context> ancestors) {
        return ancestors.stream().anyMatch(a -> a.metadata.type == searchedAncestorType);
    }

    public static boolean isInVerseText(Deque<ContextMetadata> ancestors) {
        return hasAncestor(ContextType.VERSE, ancestors) && hasAncestor(ContextType.FLAT_TEXT, ancestors);
    }

    private static final Pattern CHAPTER_NB = Pattern.compile("^(\\d+)$");
    private static final Pattern VERSE_NB = Pattern.compile("^(\\d+)?([A-Za-z])?$");

    public static ContextMetadata getChapterMetadata(ContextMetadata parent, ContextMetadata previousChapter, String parsedNb) {
        int previousChapterNb = previousChapter != null ? previousChapter.chapter : 0;

        int chapterNb;
        Matcher nbMatcher = CHAPTER_NB.matcher(parsedNb);
        if(nbMatcher.matches()) {
            int nb = Integer.parseInt(nbMatcher.group(1));
            if(nb > previousChapterNb) {
                // If the number we parsed is above the previous chapter number, trust it.
                chapterNb = nb;
            }
            else {
                // Else, we just increment the previous chapter number.
                chapterNb = previousChapterNb + 1;
            }
        }
        else {
            // Unparseable number : just increment the previous one.
            chapterNb = previousChapterNb + 1;
        }

        return ContextMetadata.forChapter(parent.book, chapterNb);
    }

    public static ContextMetadata getVerseMetadata(ContextMetadata parent, ContextMetadata previousVerse, String parsedNb) {
        int previousVerseNb = previousVerse != null ? Arrays.stream(previousVerse.verses).max().orElse(0) : 0;

        String[] parsedNbs = parsedNb.split("-");
        int[] actualVerseNbs = new int[parsedNbs.length];

        for(int i = 0; i < parsedNbs.length; i++) {
            Matcher nbMatcher = VERSE_NB.matcher(parsedNbs[i]);
            boolean matches = nbMatcher.matches();
            if(matches && nbMatcher.group(1) != null && nbMatcher.group(2) == null) {
                // We got a regular verse number. Parse it.
                int nb = Integer.parseInt(nbMatcher.group(1));
                if(nb > previousVerseNb) {
                    // If the number we parsed is above the previous verse number, trust it.
                    previousVerseNb = nb;
                }
                else if(nb != 0 || previousVerseNb != 0) {
                    // Else, we just increment the previous verse number (except for verse 0 at start of a chapter).
                    previousVerseNb++;
                }
                actualVerseNbs[i] = previousVerseNb;
            }
            else {
                // We got a lettered verse number, or an unparseable number.
                // Just increment and use the previous verse number.
                previousVerseNb++;
                actualVerseNbs[i] = previousVerseNb;
            }
        }

        return ContextMetadata.forMergedVerses(parent.book, parent.chapter, actualVerseNbs);
    }

    public static <T> int indexOf(List<T> list, Predicate<? super T> predicate) {
        for(ListIterator<T> iter = list.listIterator(); iter.hasNext(); ) {
            if (predicate.test(iter.next())) {
                return iter.previousIndex();
            }
        }
        return -1;
    }
}
