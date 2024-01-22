package com.github.unaszole.bible.writing;

import com.github.unaszole.bible.datamodel.Context;
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
    private boolean ignoreText = false;

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
            case BOOK_TITLE:
            case CHAPTER_TITLE:
            case MAJOR_SECTION_TITLE:
            case SECTION_TITLE:
                // These tags can contain TEXT contexts, that needs to be aggregated on closing.
                // So ignore all TEXT events until they are closed.
                ignoreText = true;
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
        switch(context.metadata.type) {
            // The following contexts are written on closure.
            case TEXT:
                if(!ignoreText && currentWriter.isStructuredTextWriter()) {
                    return currentWriter.asStructuredTextWriter().text(context.content + " ");
                }
                else {
                    return null;
                }
            case PARAGRAPH_BREAK:
                return currentWriter.asStructuredTextWriter().paragraph();

            // The following contexts aggregate their text content when closing.
            case BOOK_TITLE:
                ignoreText = false;
                return currentWriter.asBookWriter().title(aggregateContents(context));
            case CHAPTER_TITLE:
                ignoreText = false;
                return currentWriter.asBookContentsWriter().chapterTitle(aggregateContents(context));
            case MAJOR_SECTION_TITLE:
                ignoreText = false;
                return currentWriter.asStructuredTextWriter().majorSection(aggregateContents(context));
            case SECTION_TITLE:
                ignoreText = false;
                return currentWriter.asStructuredTextWriter().section(aggregateContents(context));

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
                break;
            case CLOSE:
                currentWriter = Optional.ofNullable(closeContext(context)).map(WriterHolder::new).orElse(currentWriter);
                break;
        }
        return Instruction.CONTINUE;
    }
}
