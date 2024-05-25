package com.github.unaszole.bible.writing;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.stream.ContextEvent;
import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import com.github.unaszole.bible.writing.interfaces.BookWriter;
import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.util.Iterator;
import java.util.Objects;
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

    private <W extends TextWriter> void writeFlatText(W w) {
        while(hasNext()) {
            ContextEvent event = next();

            if(isOpen(ContextType.NOTE, event)) {
                w.note(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(ContextType.TRANSLATION_ADD, event)) {
                w.translationAdd(consumeAndAggregateValues(event.metadata));
            }
            if(isClose(ContextType.TEXT, event)) {
                w.text(textTransformer.apply(event.value));
            }


            if(isClose(ContextType.FLAT_TEXT, event)) {
                return;
            }
        }
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

            if(isOpen(ContextType.POETRY_LINE_INDENT, event)) {
                w.poetryLine(Integer.parseInt(event.value));
            }
            if(isOpen(ContextType.POETRY_REFRAIN_INDENT, event)) {
                w.poetryRefrainLine();
            }

            if(isOpen(ContextType.FLAT_TEXT, event)) {
                if(isClose(ContextType.FLAT_TEXT, getLast())) {
                    // Implicit paragraph break if two flat texts immediately follow each other.
                    w.paragraph();
                }
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
            writer.chapter(getCurrent().metadata.chapter, getCurrent().value);
        }

        writeStructuredText(writer, (w, event) -> {
            if(isOpen(ContextType.CHAPTER, event)) {
                w.chapter(event.metadata.chapter, event.value);
            }
            if(isOpen(ContextType.CHAPTER_TITLE, event)) {
                w.chapterTitle(this::writeFlatText);
            }
            if(isOpen(ContextType.CHAPTER_INTRO, event)) {
                w.chapterIntro(this::writeFlatText);
            }
            if(isOpen(ContextType.VERSE, event)) {
                w.verse(event.metadata.verses, event.value);
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
                w.book(event.metadata.book, this::writeBook);
            }
        }
    }

    public void writeBibleSubset(BibleWriter w, ContextMetadata rootMetadata) {
        switch (rootMetadata.type) {
            case CHAPTER:
                w.book(rootMetadata.book, wb -> {
                    wb.contents(this::writeBookContents);
                });
                break;
            case BOOK:
                w.book(rootMetadata.book, this::writeBook);
                break;
            case BIBLE:
                writeBible(w);
                break;
        }
    }
}
