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
    public static class CatholicVersifications {
        private static int letterToIntVerseNb(int nbForA, char wantedLetter) {
            int shift = 0;
            // Letters J and V are skipped in verse numbering.
            if(wantedLetter > 'J') {
                shift++;
            }
            if(wantedLetter > 'V') {
                shift++;
            }
            return nbForA + (wantedLetter - 'A') - shift;
        }

        private static Pattern LETTERED_VERSE_NB = Pattern.compile("^(\\d+)?([A-Za-z])$");

        public static int mapVerseNbToOsisVerseNb(BibleBook book, int chapter, String parsedNb) {
            Matcher matcher = LETTERED_VERSE_NB.matcher(parsedNb);

            int nb;
            char letter;
            if(matcher.matches()) {
                String nbStr = matcher.group(1);
                nb = nbStr == null ? 0 : Integer.parseInt(nbStr);
                letter = matcher.group(2).toUpperCase().charAt(0);

                if(book == BibleBook.ESTH && chapter == 8 && nb == 12 && letter == 'X') {
                    // The verse usually labelled Esth 8 12X is also sometimes labelled 12W...
                    // Align the behaviour for both.
                    letter = 'W';
                }
            }
            else {
                nb = Integer.parseInt(parsedNb);
                letter = 0;
            }

            // Shift the number, depending on the book and chapter.
            if(book == BibleBook.ESTH) {
                switch(chapter) {
                    case 1:
                        if(nb > 0) {
                            nb += 18;
                        }
                        break;

                    case 3:
                        if(nb > 13) {
                            nb += 7;
                        }
                        break;

                    case 4:
                        if(nb > 8) {
                            nb += 2;
                        }
                        break;

                    case 5:
                        if(nb > 2) {
                            nb += 8;
                        }
                        else if(nb > 1) {
                            nb += 6;
                        }

                        break;

                    case 8:
                        if(nb > 12) {
                            nb += 21;
                        }
                        break;
                }
            }

            if(letter == 0) {
                // If there was no letter, just return the number, potentially shifted.
                return nb;
            }
            else {
                // If there was a letter, apply the letter shift from after the verse number.
                return letterToIntVerseNb(nb + 1, letter);
            }
        }
    }

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
