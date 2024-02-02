package com.github.unaszole.bible.writing.interfaces;

/**
 * Writer for generic structured text.
 */
public interface StructuredTextWriter extends AutoCloseable {

    /**
     * Mark the start of a new major section.
     * @param title The title.
     */
    void majorSection(String title);

    /**
     * Mark the start of a new section.
     * @param title The title.
     */
    void section(String title);

    /**
     * Mark the start of a new minor section.
     * @param title The title.
     */
    void minorSection(String title);

    /**
     * Mark the start of a new paragraph.
     */
    void paragraph();

    /**
     * Write text contents.
     * @param str The text.
     */
    void text(String str);

    /**
     * Write a note.
     * @param str The text of the note.
     */
    void note(String str);

    interface BookIntroWriter extends StructuredTextWriter {
        /**
         * Write the introduction title.
         * @param title The title.
         */
        void title(String title);
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
         * @param title The title.
         */
        void chapterTitle(String title);

        /**
         * Mark the start of a new verse.
         * @param verseNbs The verse numbers. Should usually be just one, but several verses may be combined in some
         *                 translations.
         * @param sourceNb The string representation of the verse number in the source document.
         */
        void verse(int[] verseNbs, String...sourceNb);
    };
}
