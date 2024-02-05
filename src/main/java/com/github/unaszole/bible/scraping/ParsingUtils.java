package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import org.crosswire.jsword.versification.BibleBook;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingUtils {

    private static final Pattern VERSE_NB = Pattern.compile("^(\\d+)?([A-Za-z])?$");

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
                else {
                    // Else, we just increment the previous verse number.
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

        return new ContextMetadata(ContextType.VERSE, parent.book, parent.chapter, actualVerseNbs);
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
