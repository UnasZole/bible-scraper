package com.github.unaszole.bible.writing;

/**
 * Writer for generic structured text.
 * @param <ParentWriter> The parent writer type, to return when closing this writer.
 * @param <ThisWriter> This writer type, to return on all other methods. Allows extensions of this interface
 *                    to maintain the fluent API.
 */
public interface StructuredTextWriter<ParentWriter, ThisWriter> {

    /**
     * Mark the start of a new major section.
     * @param title The title.
     * @return This writer, to keep writing.
     */
    ThisWriter majorSection(String title);

    /**
     * Mark the start of a new (minor) section.
     * @param title The title.
     * @return This writer, to keep writing.
     */
    ThisWriter section(String title);

    /**
     * Mark the start of a new paragraph.
     * @return This writer, to keep writing.
     */
    ThisWriter paragraph();

    /**
     * Write text contents.
     * @param str The text.
     * @return This writer, to keep writing.
     */
    ThisWriter text(String str);

    /**
     * Close this writer and all its pending entities.
     * @return The parent writer to keep writing, or null if no parent.
     */
    ParentWriter closeText();

    interface BookIntroWriter extends StructuredTextWriter<BookWriter, BookIntroWriter> {};

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
    interface BookContentsWriter extends StructuredTextWriter<BookWriter, BookContentsWriter> {
        /**
         * Mark the start of a new chapter, without a title.
         * @param chapterNb The chapter number.
         * @return This writer, to keep writing.
         */
        BookContentsWriter chapter(int chapterNb);

        /**
         * Mark the start of a new chapter, with a title.
         * @param chapterNb The chapter number.
         * @param chapterTitle The title.
         * @return This writer, to keep writing.
         */
        BookContentsWriter chapter(int chapterNb, String chapterTitle);

        /**
         * Mark the start of a new verse.
         * @param verseNb The verse number.
         * @return This writer, to keep writing.
         */
        BookContentsWriter verse(int verseNb);
    };
}