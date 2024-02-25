package com.github.unaszole.bible.writing.interfaces;

import java.util.function.Consumer;

/**
 * Writer for generic structured text.
 */
public interface StructuredTextWriter extends AutoCloseable {

    /**
     * Mark the start of a new major section.
     * @param writes Logic to write the title.
     */
    void majorSection(Consumer<TextWriter> writes);

    /**
     * Mark the start of a new section.
     * @param writes Logic to write the title.
     */
    void section(Consumer<TextWriter> writes);

    /**
     * Mark the start of a new minor section.
     * @param writes Logic to write the title.
     */
    void minorSection(Consumer<TextWriter> writes);

    /**
     * Mark the start of a new paragraph.
     */
    void paragraph();

    /**
     * Write some text.
     * @param writes Logic to write the text.
     */
    void flatText(Consumer<TextWriter> writes);

    interface BookIntroWriter extends StructuredTextWriter {
        /**
         * Write the introduction title.
         * @param writes Logic to write the title.
         */
        void title(Consumer<TextWriter> writes);
    };

    /**
     This is a specialised version of the structured text writer, that is able to handle chapters and verses.
     Chapters and verses may cross over sections and paragraphs.

     Example of expected usage :

     .paragraph()
     .verse(1)
     .text("Verse 1 is fully contained in a paragraph,")
     .verse(2)
     .text("which also contains verse 2,")
     .verse(3)
     .text("And the beginning of verse 3.")
     .paragraph()
     .text("The end of verse 3 is in another paragraph.")
     .verse(4)
     .text("Verse 4 starts in the middle of the paragraph.")

     */
    interface BookContentsWriter extends StructuredTextWriter {
        /**
         * Mark the start of a new chapter.
         * @param chapterNb The chapter number.
         * @param sourceNb The string representation of the chapter number in the source document.
         */
        void chapter(int chapterNb, String...sourceNb);

        /**
         * Write a chapter title.
         * @param writes Logic to write the title.
         */
        void chapterTitle(Consumer<TextWriter> writes);

        /**
         * Write a chapter introduction.
         * @param writes Logic to write the intro.
         */
        void chapterIntro(Consumer<TextWriter> writes);

        /**
         * Mark the start of a new verse.
         * @param verseNbs The verse numbers. Should usually be just one, but several verses may be combined in some
         *                 translations.
         * @param sourceNb The string representation of the verse number in the source document.
         */
        void verse(int[] verseNbs, String...sourceNb);
    };
}
