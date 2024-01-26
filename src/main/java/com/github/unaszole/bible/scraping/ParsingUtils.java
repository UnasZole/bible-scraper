package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import org.crosswire.jsword.versification.BibleBook;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

    public static <T> Iterator<T> toFlatIterator(Iterator<List<T>> listIt) {
        final Deque<T> buffer = new LinkedList<>();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                while(buffer.isEmpty() && listIt.hasNext()) {
                    buffer.addAll(listIt.next());
                }
                return !buffer.isEmpty();
            }

            @Override
            public T next() {
                return buffer.removeFirst();
            }
        };
    }

    public static <T> Stream<T> toStream(Iterator<T> it) {
        return Stream.iterate((T)null, p -> {
            if(it.hasNext()) {
                return it.next();
            }
            return null;
        }).dropWhile(Objects::isNull).takeWhile(Objects::nonNull);
    }

    public static Stream<ContextEvent> aggregateContextStream(Context rootContext, List<Stream<ContextEvent>> childStreams) {
        List<Stream<ContextEvent>> streams = new ArrayList<>();
        streams.add(Stream.of(new ContextEvent(ContextEvent.Type.OPEN, rootContext)));
        streams.addAll(childStreams);
        streams.add(Stream.of(new ContextEvent(ContextEvent.Type.CLOSE, rootContext)));
        return streams.stream().flatMap(s -> s);
    }

    public static Stream<ContextEvent> aggregateContextStream(Context rootContext, Stream<ContextEvent>... childStreams) {
        return aggregateContextStream(rootContext, Arrays.asList(childStreams));
    }

    public static Stream<ContextEvent> extract(Stream<ContextEvent> stream, ContextMetadata wantedContext) {
        final boolean[] closed = new boolean[] {false};
        return stream
                .dropWhile(e -> !(e.type == ContextEvent.Type.OPEN && Objects.equals(e.context.metadata, wantedContext)))
                .takeWhile(e -> {
                    if(closed[0]) {
                        return false;
                    }
                    closed[0] = e.type == ContextEvent.Type.CLOSE && Objects.equals(e.context.metadata, wantedContext);
                    return true;
                })
                //.peek(e -> System.out.println("Extracting context " + wantedContext + " : found event " + e))
                ;
    }
}
