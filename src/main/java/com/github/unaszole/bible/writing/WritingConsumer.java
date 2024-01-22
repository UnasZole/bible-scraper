package com.github.unaszole.bible.writing;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.ContextConsumer;

import java.util.Optional;
import java.util.stream.Collectors;

public class WritingConsumer implements ContextConsumer {

    private static class WriterHolder {
        private final Object writer;

        private WriterHolder(Object writer) {
            this.writer = writer;
        }

        public boolean isStructuredTextWriter() {
            return writer instanceof StructuredTextWriter;
        }

        public BibleWriter asBibleWriter() {
            return (BibleWriter) writer;
        }
        public BookWriter asBookWriter() {
            return (BookWriter) writer;
        }

        public StructuredTextWriter<?, ?> asStructuredTextWriter() {
            return (StructuredTextWriter<?, ?>) writer;
        }

        public StructuredTextWriter.BookIntroWriter asBookIntroWriter() {
            return (StructuredTextWriter.BookIntroWriter) writer;
        }

        public StructuredTextWriter.BookContentsWriter asBookContentsWriter() {
            return (StructuredTextWriter.BookContentsWriter) writer;
        }
    }

    private WriterHolder currentWriter;
    private boolean inFlatText = false;
    private boolean justClosedFlatText = false;
    private boolean inNote = false;

    public WritingConsumer(BibleWriter writer) {
        this.currentWriter = new WriterHolder(writer);
    }

    public WritingConsumer(BookWriter writer) {
        this.currentWriter = new WriterHolder(writer);
    }

    public WritingConsumer(StructuredTextWriter.BookContentsWriter writer) {
        this.currentWriter = new WriterHolder(writer);
    }

    public String aggregateContents(Context context) {
        if(context.content != null) {
            return context.content;
        }
        else {
            return context.getChildren().stream()
                    .map(c -> aggregateContents(c))
                    .collect(Collectors.joining(" "));
        }
    }

    private Object openContext(Context context) {
        switch (context.metadata.type) {
            case FLAT_TEXT:
                // A FLAT_TEXT is a subsection of a STRUCTURED_TEXT, that can append text (and notes) to the structured text.
                // (Contrary to other text nodes which are aggregated on parent element close)
                inFlatText = true;
                if(justClosedFlatText) {
                    // Successive flat texts are joined by an implicit paragraph break.
                    return currentWriter.asStructuredTextWriter().paragraph();
                }
                return null;
            case NOTE:
                inNote = true;
                return null;

            case BOOK:
                return currentWriter.asBibleWriter().book(context.metadata.book);
            case BOOK_INTRO:
                return currentWriter.asBookWriter().introduction();

            case CHAPTER:
                if(!currentWriter.isStructuredTextWriter()) {
                    // If we're not in a structured text writer yet, it's the first chapter.
                    // we need to move down from the book to the book contents.
                    currentWriter = new WriterHolder(currentWriter.asBookWriter().contents());
                }
                return currentWriter.asBookContentsWriter().chapter(context.metadata.chapter);
            case VERSE:
                return currentWriter.asBookContentsWriter().verse(context.metadata.verse);
        }
        return null;
    }

    private Object closeContext(Context context) {
        justClosedFlatText = context.metadata.type == ContextType.FLAT_TEXT;

        switch(context.metadata.type) {
            // When exiting a flat text, we stop capturing TEXT elements.
            case FLAT_TEXT:
                inFlatText = false;
                return null;

            // The following contexts are written on closure.
            case TEXT:
                if(inFlatText && !inNote) {
                    return currentWriter.asStructuredTextWriter().text(context.content);
                }
                else {
                    return null;
                }

            case PARAGRAPH_BREAK:
                return currentWriter.asStructuredTextWriter().paragraph();

            // The following contexts aggregate their text content when closing.
            case BOOK_TITLE:
                return currentWriter.asBookWriter().title(aggregateContents(context));
            case CHAPTER_TITLE:
                return currentWriter.asBookContentsWriter().chapterTitle(aggregateContents(context));
            case MAJOR_SECTION_TITLE:
                return currentWriter.asStructuredTextWriter().majorSection(aggregateContents(context));
            case SECTION_TITLE:
                return currentWriter.asStructuredTextWriter().section(aggregateContents(context));
            case MINOR_SECTION_TITLE:
                return currentWriter.asStructuredTextWriter().minorSection(aggregateContents(context));
            case NOTE:
                inNote = false;
                return currentWriter.asStructuredTextWriter().note(aggregateContents(context));

            // The following contexts close the current writer.
            case BOOK_INTRO:
                return currentWriter.asBookIntroWriter().closeText();
            case BOOK:
                if(currentWriter.isStructuredTextWriter()) {
                    // If we're in a structured text writer, we need to move up from the book contents to the book.
                    currentWriter = new WriterHolder(currentWriter.asBookContentsWriter().closeText());
                }
                return currentWriter.asBookWriter().closeBook();
            case BIBLE:
                currentWriter.asBibleWriter().closeBible();
                return null;
        }
        return null;
    }

    @Override
    public Instruction consume(EventType type, Context context) {
        switch(type) {
            case OPEN:
                currentWriter = Optional.ofNullable(openContext(context)).map(WriterHolder::new).orElse(currentWriter);
                justClosedFlatText = false;
                break;
            case CLOSE:
                currentWriter = Optional.ofNullable(closeContext(context)).map(WriterHolder::new).orElse(currentWriter);
                break;
        }
        return Instruction.CONTINUE;
    }
}
