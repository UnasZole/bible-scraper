package com.github.unaszole.bible.writing;

import com.github.unaszole.bible.datamodel.IdField;
import com.github.unaszole.bible.writing.datamodel.BibleRef;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.stream.ContextEvent;
import com.github.unaszole.bible.writing.interfaces.*;
import org.crosswire.jsword.versification.BibleBook;

import java.net.URI;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ContextStreamWriter {
    private final UnaryOperator<String> textTransformer;
    private final Iterator<ContextEvent> it;
    private ContextEvent last = null;
    private ContextEvent current = null;

    public ContextStreamWriter(Stream<ContextEvent> stream, UnaryOperator<String> textTransformer) {
        this.it = stream.iterator();
        this.textTransformer = textTransformer;
    }

    public ContextStreamWriter(Stream<ContextEvent> stream) {
        this(stream, s -> s);
    }

    private boolean hasNext() {
        return it.hasNext();
    }
    private ContextEvent next() {
        this.last = current;
        this.current = it.next();
        return current;
    }
    private ContextEvent getCurrent() {
        return current;
    }
    private ContextEvent getLast() {
        return last;
    }

    private static boolean isOpen(ContextType type, ContextEvent event) {
        return event != null && event.type == ContextEvent.Type.OPEN && event.metadata.type == type;
    }

    private static boolean isClose(ContextType type, ContextEvent event) {
        return event != null && event.type == ContextEvent.Type.CLOSE && event.metadata.type == type;
    }

    private String consumeAndAggregateValues(ContextMetadata metadata) {
        StringBuilder contents = new StringBuilder();

        while(hasNext()) {
            ContextEvent event = next();

            if(event.type == ContextEvent.Type.OPEN && event.value != null) {
                contents.append(event.value);
            }

            if(event.type == ContextEvent.Type.CLOSE && Objects.equals(event.metadata, metadata)) {
                // Event closes the context : time to aggregate and return.
                return textTransformer.apply(contents.toString());
            }
        }

        throw new IllegalStateException("Did not find " + metadata + " closing event.");
    }

    private Map<ContextType, List<Object>> consumeAndListValuesByType(ContextMetadata metadata) {
        Map<ContextType, List<Object>> values = new HashMap<>();

        while(hasNext()) {
            ContextEvent event = next();

            if(event.type == ContextEvent.Type.OPEN) {
                if(!values.containsKey(event.metadata.type)) {
                    values.put(event.metadata.type, new ArrayList<>());
                }

                if(event.value != null) {
                    values.get(event.metadata.type).add(event.value);
                }
            }

            if(event.type == ContextEvent.Type.CLOSE && Objects.equals(event.metadata, metadata)) {
                // Event closes the context.
                return values;
            }
        }

        throw new IllegalStateException("Did not find " + metadata + " closing event.");
    }

    BibleRef currentFullRef = null;
    BibleRef currentLocalRef = null;

    private BibleRef[] buildRefs(Map<ContextType, List<Object>> values, BibleRef inheritFrom) {
        BibleBook book = inheritFrom != null ? inheritFrom.book : null;
        if(values.containsKey(ContextType.REF_BOOK)) {
            book = (BibleBook) values.get(ContextType.REF_BOOK).get(0);
        }

        int chapter = inheritFrom != null ? inheritFrom.chapter : 0;
        if(values.containsKey(ContextType.REF_CHAPTER)) {
            chapter = (int) values.get(ContextType.REF_CHAPTER).get(0);
        }

        int firstVerse = 0;
        int lastVerse = 0;
        if(values.containsKey(ContextType.REF_VERSES)) {
            int[] verseRange = (int[]) values.get(ContextType.REF_VERSES).get(0);
            firstVerse = verseRange[0];
            if(verseRange.length > 1) {
                lastVerse = verseRange[1];
            }
        }

        if(lastVerse == 0) {
            return new BibleRef[] { new BibleRef(book, chapter, firstVerse) };
        }
        else {
            return new BibleRef[] {
                    new BibleRef(book, chapter, firstVerse),
                    new BibleRef(book, chapter, lastVerse)
            };
        }
    }

    private <W extends TextWriter> void writeText(W w, ContextType endCtx) {
        while(hasNext()) {
            ContextEvent event = next();

            if(isOpen(ContextType.TRANSLATION_ADD, event)) {
                w.translationAdd(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(ContextType.QUOTE, event)) {
                w.quote(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(ContextType.OT_QUOTE, event)) {
                w.oldTestamentQuote(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(ContextType.SPEAKER, event)) {
                w.speaker(consumeAndAggregateValues(event.metadata));
            }
            if(isClose(ContextType.TEXT, event)) {
                w.text(textTransformer.apply((String) event.value));
            }
            if(isOpen(ContextType.NOTE, event)) {
                w.note(iw -> writeText(iw, ContextType.NOTE));
            }

            if(isOpen(ContextType.REFERENCE, event)) {
                Map<ContextType, List<Object>> values = consumeAndListValuesByType(event.metadata);

                BibleRef[] refs = null;
                if(values.containsKey(ContextType.FULL_REF)) {
                    refs = buildRefs(values, null);
                    currentFullRef = refs[0];
                }
                else if(values.containsKey(ContextType.CONTINUED_REF)) {
                    refs = buildRefs(values, currentFullRef);
                }
                else if(values.containsKey(ContextType.LOCAL_REF)) {
                    refs = buildRefs(values, currentLocalRef);
                }
                else {
                    throw new IllegalArgumentException("REFERENCE is missing actual FULL_REF/CONTINUED_REF/LOCAL_REF child");
                }

                StringBuilder fullString = new StringBuilder();
                for(Object textValue: values.get(ContextType.TEXT)) {
                    fullString.append(textValue);
                }

                w.reference(refs[0], refs.length > 1 ? refs[1] : null, fullString.toString());
            }

            if(isOpen(ContextType.LINK, event)) {
                w.link((URI) event.value, consumeAndAggregateValues(event.metadata));
            }

            // If inside a note, we have other specific markup available.
            if(endCtx == ContextType.NOTE) {
                NoteTextWriter nw = (NoteTextWriter) w;
                if(isOpen(ContextType.CATCHPHRASE, event)) {
                    nw.catchphraseQuote(consumeAndAggregateValues(event.metadata));
                }
                if(isOpen(ContextType.ALTERNATE_TRANSLATION, event)) {
                    nw.alternateTranslationQuote(consumeAndAggregateValues(event.metadata));
                }
            }

            if(isClose(endCtx, event)) {
                return;
            }
        }
    }

    private <W extends TextWriter> void writeFlatText(W w) {
        writeText(w, ContextType.FLAT_TEXT);
    }

    private <W extends StructuredTextWriter> void writeStructuredText(W w, BiPredicate<W, ContextEvent> specialisedBehaviour) {
        while(hasNext()) {
            ContextEvent event = next();

            if(isOpen(ContextType.MAJOR_SECTION_TITLE, event)) {
                w.majorSection(this::writeFlatText);
            }
            if(isOpen(ContextType.SECTION_TITLE, event)) {
                w.section(this::writeFlatText);
            }
            if(isOpen(ContextType.MINOR_SECTION_TITLE, event)) {
                w.minorSection(this::writeFlatText);
            }

            if(isOpen(ContextType.POETRY_LINE_START, event)) {
                w.poetryLine((Integer) event.value);
            }
            if(isOpen(ContextType.POETRY_REFRAIN_START, event)) {
                w.poetryRefrainLine();
            }
            if(isOpen(ContextType.POETRY_ACROSTIC_START, event)) {
                w.poetryAcrosticLine();
            }
            if(isOpen(ContextType.POETRY_SELAH_START, event)) {
                w.poetrySelahLine();
            }

            if(isOpen(ContextType.FLAT_TEXT, event)) {
                w.flatText(this::writeFlatText);
            }

            if(isClose(ContextType.POETRY_STANZA_BREAK, event)) {
                w.poetryStanza();
            }
            if(isClose(ContextType.PARAGRAPH_BREAK, event)) {
                w.paragraph();
            }

            if(specialisedBehaviour.test(w, event)) {
                return;
            }
        }
    }

    public void writeBookIntro(StructuredTextWriter.BookIntroWriter writer) {
        writeStructuredText(writer, (w, event) -> {
            if(isOpen(ContextType.BOOK_INTRO_TITLE, event)) {
                w.title(this::writeFlatText);
            }

            return isClose(ContextType.BOOK_INTRO, event);
        });
    }

    public void writeBookContents(StructuredTextWriter.BookContentsWriter writer) {
        if(isOpen(ContextType.CHAPTER, getCurrent())) {
            BibleBook chapterBook = getCurrent().metadata.id.get(IdField.BIBLE_BOOK);
            int chapterNb = getCurrent().metadata.id.get(IdField.BIBLE_CHAPTER);
            currentLocalRef = new BibleRef(chapterBook, chapterNb, 0);
            writer.chapter(chapterNb, (String) getCurrent().value);
        }

        writeStructuredText(writer, (w, event) -> {
            if(isOpen(ContextType.CHAPTER, event)) {
                BibleBook chapterBook = event.metadata.id.get(IdField.BIBLE_BOOK);
                int chapterNb = event.metadata.id.get(IdField.BIBLE_CHAPTER);
                currentLocalRef = new BibleRef(chapterBook, chapterNb, 0);
                w.chapter(chapterNb, (String) event.value);
            }
            if(isOpen(ContextType.CHAPTER_TITLE, event)) {
                w.chapterTitle(this::writeFlatText);
            }
            if(isOpen(ContextType.CHAPTER_INTRO, event)) {
                w.chapterIntro(this::writeFlatText);
            }
            if(isOpen(ContextType.VERSE, event)) {
                w.verse(
                        ((List<Integer>) event.metadata.id.get(IdField.BIBLE_VERSES)).stream()
                                .mapToInt(v -> v)
                                .toArray(),
                        (String) event.value
                );
            }
            if(isOpen(ContextType.PSALM_TITLE, event)) {
                w.psalmTitle(this::writeFlatText);
            }

            return isClose(ContextType.BOOK, event);
        });
    }

    public void writeBook(BookWriter w) {
        while(hasNext()) {
            ContextEvent event = next();

            if(isOpen(ContextType.BOOK_TITLE, event)) {
                w.title(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(ContextType.BOOK_INTRO, event)) {
                w.introduction(this::writeBookIntro);
            }
            if(isOpen(ContextType.CHAPTER, event)) {
                w.contents(this::writeBookContents);
                if(isClose(ContextType.BOOK, getCurrent())) {
                    return;
                }
            }

            if(isClose(ContextType.BOOK, getCurrent())) {
                return;
            }
        }
    }

    public void writeBible(BibleWriter w) {
        while(hasNext()) {
            ContextEvent event = next();

            if(isOpen(ContextType.BOOK, event)) {
                currentLocalRef = new BibleRef(event.metadata.id.get(IdField.BIBLE_BOOK), 0, 0);
                w.book(event.metadata.id.get(IdField.BIBLE_BOOK), this::writeBook);
            }
        }
    }

    public void writeBibleSubset(BibleWriter w, ContextMetadata rootMetadata) {
        switch (rootMetadata.type) {
            case CHAPTER:
                w.book(rootMetadata.id.get(IdField.BIBLE_BOOK), wb -> {
                    wb.contents(this::writeBookContents);
                });
                break;
            case BOOK:
                w.book(rootMetadata.id.get(IdField.BIBLE_BOOK), this::writeBook);
                break;
            case BIBLE:
                writeBible(w);
                break;
        }
    }
}
