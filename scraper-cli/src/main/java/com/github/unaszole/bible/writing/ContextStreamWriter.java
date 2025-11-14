package com.github.unaszole.bible.writing;

import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.datamodel.contexttypes.FlatText;
import com.github.unaszole.bible.datamodel.contexttypes.StructureMarkers;
import com.github.unaszole.bible.datamodel.idtypes.BibleIdFields;
import com.github.unaszole.bible.datamodel.valuetypes.Attachment;
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
        if(values.containsKey(FlatText.REF_BOOK)) {
            book = (BibleBook) values.get(FlatText.REF_BOOK).get(0);
        }

        int chapter = inheritFrom != null ? inheritFrom.chapter : 0;
        if(values.containsKey(FlatText.REF_CHAPTER)) {
            chapter = (int) values.get(FlatText.REF_CHAPTER).get(0);
        }

        int firstVerse = 0;
        int lastVerse = 0;
        if(values.containsKey(FlatText.REF_VERSES)) {
            List<Integer> verseRange = (List<Integer>) values.get(FlatText.REF_VERSES).get(0);
            firstVerse = verseRange.getFirst();
            if(verseRange.size() > 1) {
                lastVerse = verseRange.getLast();
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

            if(isOpen(FlatText.TRANSLATION_ADD, event)) {
                w.translationAdd(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(FlatText.QUOTE, event)) {
                w.quote(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(FlatText.OT_QUOTE, event)) {
                w.oldTestamentQuote(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(FlatText.SPEAKER, event)) {
                w.speaker(consumeAndAggregateValues(event.metadata));
            }
            if(isClose(FlatText.TEXT, event)) {
                w.text(textTransformer.apply((String) event.value));
            }
            if(isOpen(FlatText.NOTE, event)) {
                w.note(iw -> writeText(iw, FlatText.NOTE));
            }

            if(isOpen(FlatText.REFERENCE, event)) {
                Map<ContextType, List<Object>> values = consumeAndListValuesByType(event.metadata);

                BibleRef[] refs = null;
                if(values.containsKey(FlatText.FULL_REF)) {
                    refs = buildRefs(values, null);
                    currentFullRef = refs[0];
                }
                else if(values.containsKey(FlatText.CONTINUED_REF)) {
                    refs = buildRefs(values, currentFullRef);
                }
                else if(values.containsKey(FlatText.LOCAL_REF)) {
                    refs = buildRefs(values, currentLocalRef);
                }
                else {
                    throw new IllegalArgumentException("REFERENCE is missing actual FULL_REF/CONTINUED_REF/LOCAL_REF child");
                }

                StringBuilder fullString = new StringBuilder();
                for(Object textValue: values.get(FlatText.TEXT)) {
                    fullString.append(textValue);
                }

                w.reference(refs[0], refs.length > 1 ? refs[1] : null, fullString.toString());
            }

            if(isOpen(FlatText.LINK, event)) {
                w.link((URI) event.value, consumeAndAggregateValues(event.metadata));
            }

            if(isOpen(FlatText.FIGURE, event)) {
                Map<ContextType, List<Object>> values = consumeAndListValuesByType(event.metadata);
                String alt = (String) Optional.ofNullable(values.get(FlatText.FIG_ALT)).map(List::getFirst).orElse(null);
                String caption = (String) Optional.ofNullable(values.get(FlatText.FIG_CAPTION)).map(List::getFirst).orElse(null);
                Attachment attachment = (Attachment) event.value;
                w.figure(attachment.getName(), attachment::getBytes, alt, caption);
            }

            // If inside a note, we have other specific markup available.
            if(endCtx == FlatText.NOTE) {
                NoteTextWriter nw = (NoteTextWriter) w;
                if(isOpen(FlatText.CATCHPHRASE, event)) {
                    nw.catchphraseQuote(consumeAndAggregateValues(event.metadata));
                }
                if(isOpen(FlatText.ALTERNATE_TRANSLATION, event)) {
                    nw.alternateTranslationQuote(consumeAndAggregateValues(event.metadata));
                }
            }

            if(isClose(endCtx, event)) {
                return;
            }
        }
    }

    private <W extends TextWriter> void writeFlatText(W w) {
        writeText(w, FlatText.FLAT_TEXT);
    }

    private <W extends StructuredTextWriter> void writeStructuredText(W w, BiPredicate<W, ContextEvent> specialisedBehaviour) {
        while(hasNext()) {
            ContextEvent event = next();

            if(isOpen(StructureMarkers.MAJOR_SECTION_TITLE, event)) {
                w.majorSection(this::writeFlatText);
            }
            if(isOpen(StructureMarkers.SECTION_TITLE, event)) {
                w.section(this::writeFlatText);
            }
            if(isOpen(StructureMarkers.MINOR_SECTION_TITLE, event)) {
                w.minorSection(this::writeFlatText);
            }

            if(isOpen(StructureMarkers.POETRY_LINE_START, event)) {
                w.poetryLine((Integer) event.value);
            }
            if(isOpen(StructureMarkers.POETRY_REFRAIN_START, event)) {
                w.poetryRefrainLine();
            }
            if(isOpen(StructureMarkers.POETRY_ACROSTIC_START, event)) {
                w.poetryAcrosticLine();
            }
            if(isOpen(StructureMarkers.POETRY_SELAH_START, event)) {
                w.poetrySelahLine();
            }

            if(isOpen(FlatText.FLAT_TEXT, event)) {
                w.flatText(this::writeFlatText);
            }

            if(isClose(StructureMarkers.POETRY_STANZA_BREAK, event)) {
                w.poetryStanza();
            }
            if(isClose(StructureMarkers.PARAGRAPH_BREAK, event)) {
                w.paragraph();
            }

            if(specialisedBehaviour.test(w, event)) {
                return;
            }
        }
    }

    public void writeBookIntro(StructuredTextWriter.BookIntroWriter writer) {
        writeStructuredText(writer, (w, event) -> {
            if(isOpen(BibleContainers.BOOK_INTRO_TITLE, event)) {
                w.title(this::writeFlatText);
            }

            return isClose(BibleContainers.BOOK_INTRO, event);
        });
    }

    public void writeBookContents(StructuredTextWriter.BookContentsWriter writer) {
        if(isOpen(BibleContainers.CHAPTER, getCurrent())) {
            BibleBook chapterBook = getCurrent().metadata.id.get(BibleIdFields.BOOK);
            int chapterNb = getCurrent().metadata.id.get(BibleIdFields.CHAPTER);
            currentLocalRef = new BibleRef(chapterBook, chapterNb, 0);
            writer.chapter(chapterNb, (String) getCurrent().value);
        }

        writeStructuredText(writer, (w, event) -> {
            if(isOpen(BibleContainers.CHAPTER, event)) {
                BibleBook chapterBook = event.metadata.id.get(BibleIdFields.BOOK);
                int chapterNb = event.metadata.id.get(BibleIdFields.CHAPTER);
                currentLocalRef = new BibleRef(chapterBook, chapterNb, 0);
                w.chapter(chapterNb, (String) event.value);
            }
            if(isOpen(BibleContainers.CHAPTER_TITLE, event)) {
                w.chapterTitle(this::writeFlatText);
            }
            if(isOpen(BibleContainers.CHAPTER_INTRO, event)) {
                w.chapterIntro(this::writeFlatText);
            }
            if(isOpen(BibleContainers.VERSE, event)) {
                w.verse(
                        event.metadata.id.get(BibleIdFields.VERSES).stream()
                                .mapToInt(v -> v)
                                .toArray(),
                        (String) event.value
                );
            }
            if(isOpen(BibleContainers.PSALM_TITLE, event)) {
                w.psalmTitle(this::writeFlatText);
            }

            return isClose(BibleContainers.BOOK, event);
        });
    }

    public void writeBook(BookWriter w) {
        while(hasNext()) {
            ContextEvent event = next();

            if(isOpen(BibleContainers.BOOK_TITLE, event)) {
                w.title(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(BibleContainers.BOOK_INTRO, event)) {
                w.introduction(this::writeBookIntro);
            }
            if(isOpen(BibleContainers.CHAPTER, event)) {
                w.contents(this::writeBookContents);
                if(isClose(BibleContainers.BOOK, getCurrent())) {
                    return;
                }
            }

            if(isClose(BibleContainers.BOOK, getCurrent())) {
                return;
            }
        }
    }

    public void writeBible(BibleWriter w) {
        while(hasNext()) {
            ContextEvent event = next();

            if(isOpen(BibleContainers.BOOK, event)) {
                currentLocalRef = new BibleRef(event.metadata.id.get(BibleIdFields.BOOK), 0, 0);
                w.book(event.metadata.id.get(BibleIdFields.BOOK), this::writeBook);
            }
        }
    }

    public void writeBibleSubset(BibleWriter w, ContextMetadata rootMetadata) {
        if(rootMetadata.type == BibleContainers.CHAPTER) {
            w.book(rootMetadata.id.get(BibleIdFields.BOOK), wb -> wb.contents(this::writeBookContents));
        }
        else if(rootMetadata.type == BibleContainers.BOOK) {
            w.book(rootMetadata.id.get(BibleIdFields.BOOK), this::writeBook);
        }
        else if(rootMetadata.type == BibleContainers.BIBLE) {
            writeBible(w);
        }
    }
}
