package com.github.unaszole.bible.scraping;

import org.crosswire.jsword.versification.BibleBook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingUtils {
    public static class CatholicVersifications {
        public static int letterToIntVerseNb(int nbForA, char wantedLetter) {
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
}
