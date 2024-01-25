package com.github.unaszole.bible.writing;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.ContextConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WritingConsumer implements ContextConsumer {


    private BibleWriter writer;
    private boolean inFlatText = false;
    private boolean justClosedFlatText = false;
    private boolean inNote = false;

    private List<Consumer<BookWriter>> pendingBookWrites = null;
    private List<Consumer<? super StructuredTextWriter.BookIntroWriter>> pendingBookIntroWrites = null;
    private List<Consumer<? super StructuredTextWriter.BookContentsWriter>> pendingBookContentsWrites = null;

    public WritingConsumer(BibleWriter writer) {
        this.writer = writer;
    }

    public String aggregateContents(Context context) {
        if(context.value != null) {
            return context.value;
        }
        else {
            return context.getChildren().stream()
                    .map(c -> aggregateContents(c))
                    .collect(Collectors.joining(" "));
        }
    }

    private void addStructuredTextWrite(Consumer<StructuredTextWriter> write) {
        if(pendingBookIntroWrites != null) {
            pendingBookIntroWrites.add(write);
        }
        else if(pendingBookContentsWrites != null) {
            pendingBookContentsWrites.add(write);
        }
    }

    private void openContext(Context context) {
        switch (context.metadata.type) {
            case BOOK:
                pendingBookWrites = new ArrayList<>();
                break;

            case BOOK_INTRO:
                pendingBookIntroWrites = new ArrayList<>();
                break;

            case CHAPTER:
                if(pendingBookContentsWrites == null) {
                    pendingBookContentsWrites = new ArrayList<>();
                }
                pendingBookContentsWrites.add(w -> {
                    w.chapter(context.metadata.chapter, context.value);
                });
                break;

            case VERSE:
                pendingBookContentsWrites.add(w -> {
                    w.verse(context.metadata.verse, context.value);
                });
                break;

            case FLAT_TEXT:
                // A FLAT_TEXT is a subsection of a STRUCTURED_TEXT, that can append text (and notes) to the structured text.
                // (Contrary to other text nodes which are aggregated on parent element close)
                inFlatText = true;
                if(justClosedFlatText) {
                    // Successive flat texts are joined by an implicit paragraph break.
                    addStructuredTextWrite(w -> w.paragraph());
                }
                break;

            case NOTE:
                inNote = true;
                break;
        }
    }

    private void closeContext(Context context) {
        justClosedFlatText = context.metadata.type == ContextType.FLAT_TEXT;

        switch(context.metadata.type) {

            // When exiting a flat text, we stop capturing TEXT elements.
            case FLAT_TEXT:
                inFlatText = false;
                break;

            // The following contexts are written on closure.
            case TEXT:
                if(inFlatText && !inNote) {
                    addStructuredTextWrite(w -> w.text(context.value));
                }
                break;

            case PARAGRAPH_BREAK:
                addStructuredTextWrite(w -> w.paragraph());
                break;

            // The following contexts aggregate their text content when closing.
            case BOOK_TITLE:
                pendingBookWrites.add(w -> w.title(aggregateContents(context)));
                break;
            case BOOK_INTRO_TITLE:
                pendingBookIntroWrites.add(w -> w.title(aggregateContents(context)));
                break;
            case CHAPTER_TITLE:
                pendingBookContentsWrites.add(w -> w.chapterTitle(aggregateContents(context)));
                break;
            case MAJOR_SECTION_TITLE:
                addStructuredTextWrite(w -> w.majorSection(aggregateContents(context)));
                break;
            case SECTION_TITLE:
                addStructuredTextWrite(w -> w.section(aggregateContents(context)));
                break;
            case MINOR_SECTION_TITLE:
                addStructuredTextWrite(w -> w.minorSection(aggregateContents(context)));
                break;
            case NOTE:
                inNote = false;
                addStructuredTextWrite(w -> w.note(aggregateContents(context)));
                break;


            case BOOK_INTRO:
                final List<Consumer<? super StructuredTextWriter.BookIntroWriter>> bookIntroWrites = new ArrayList<>(pendingBookIntroWrites);
                pendingBookWrites.add(w -> {
                    w.introduction(w2 -> {
                        for(Consumer<? super StructuredTextWriter.BookIntroWriter> write: bookIntroWrites) {
                            write.accept(w2);
                        }
                    });
                });
                pendingBookIntroWrites = null;
                break;

            case BOOK:
                if(pendingBookContentsWrites != null) {
                    final List<Consumer<? super StructuredTextWriter.BookContentsWriter>> bookContentsWrites = new ArrayList<>(pendingBookContentsWrites);
                    pendingBookWrites.add(w -> {
                        w.contents(w2 -> {
                            for(Consumer<? super StructuredTextWriter.BookContentsWriter> write: bookContentsWrites) {
                                write.accept(w2);
                            }
                        });
                    });
                    pendingBookContentsWrites = null;
                }

                writer.book(context.metadata.book, w -> {
                    for(Consumer<BookWriter> write: pendingBookWrites) {
                        write.accept(w);
                    }
                });
                pendingBookWrites = null;
                break;

            case BIBLE:
                try {
                    writer.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
        }
    }

    @Override
    public Instruction consume(EventType type, Context context) {
        switch(type) {
            case OPEN:
                openContext(context);
                justClosedFlatText = false;
                break;
            case CLOSE:
                closeContext(context);
                break;
        }
        return Instruction.CONTINUE;
    }
}
