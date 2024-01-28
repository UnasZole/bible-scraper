package com.github.unaszole.bible;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.ContextEvent;
import com.github.unaszole.bible.writing.BibleWriter;
import com.github.unaszole.bible.writing.BookWriter;
import com.github.unaszole.bible.writing.StructuredTextWriter;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class ContextStreamWriter {
    private final Iterator<ContextEvent> it;
    private final ContextEvent[] last = new ContextEvent[]{ null };

    public ContextStreamWriter(Stream<ContextEvent> stream) {
        this.it = stream.iterator();
    }

    private boolean hasNext() {
        return it.hasNext();
    }
    private ContextEvent next() {
        this.last[0] = it.next();
        return last[0];
    }
    private ContextEvent getLast() {
        return last[0];
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
                return contents.toString();
            }
        }

        throw new IllegalStateException("Did not find " + metadata + " closing event.");
    }

    private <W extends StructuredTextWriter> void writeStructuredText(W w, BiPredicate<W, ContextEvent> specialisedBehaviour) {
        while(hasNext()) {
            ContextEvent event = next();

            if(isOpen(ContextType.MAJOR_SECTION_TITLE, event)) {
                w.majorSection(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(ContextType.SECTION_TITLE, event)) {
                w.section(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(ContextType.MINOR_SECTION_TITLE, event)) {
                w.minorSection(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(ContextType.NOTE, event)) {
                w.note(consumeAndAggregateValues(event.metadata));
            }

            if(isOpen(ContextType.FLAT_TEXT, event) && isClose(ContextType.FLAT_TEXT, getLast())) {
                // Implicit paragraph break if two flat texts immediately follow each other.
                w.paragraph();
            }

            if(isClose(ContextType.TEXT, event)) {
                w.text(event.value);
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
                w.title(consumeAndAggregateValues(event.metadata));
            }

            return isClose(ContextType.BOOK_INTRO, event);
        });
    }

    public void writeBookContents(StructuredTextWriter.BookContentsWriter writer) {
        if(isOpen(ContextType.CHAPTER, getLast())) {
            writer.chapter(getLast().metadata.chapter, getLast().value);
        }

        writeStructuredText(writer, (w, event) -> {
            if(isOpen(ContextType.CHAPTER, event)) {
                w.chapter(event.metadata.chapter, event.value);
            }
            if(isOpen(ContextType.CHAPTER_TITLE, event)) {
                w.chapterTitle(consumeAndAggregateValues(event.metadata));
            }
            if(isOpen(ContextType.VERSE, event)) {
                w.verse(event.metadata.verse, event.value);
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
                if(isClose(ContextType.BOOK, getLast())) {
                    return;
                }
            }

            if(isClose(ContextType.BOOK, event)) {
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
