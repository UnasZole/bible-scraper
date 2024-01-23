package com.github.unaszole.bible.datamodel;

import org.crosswire.jsword.versification.BibleBook;

public class ContextMetadata {
	public final ContextType type;
	public final BibleBook book;
	/**
	 * The chapter number in standardised OSIS-compatible form, ie. the position of the chapter within the book,
	 * starting at 1.
	 * (Since chapter 0 is impossible in OSIS, this field may be set to zero for contexts which don't need to carry
	 * a chapter number).
	 */
	public final int chapter;
	/**
	 * The verse number in standardised OSIS-compatible form, ie. the position of the verse within the book,
	 * starting at 1.
	 * (Since verse number 0 is reserved for pre-verse titles in OSIS, this field may be set to zero for contexts
	 * which don't need to carry a verse number).
	 */
	public final int verse;
	
	public ContextMetadata(ContextType type, BibleBook book, int chapter, int verse) {
		this.type = type;
		this.book = book;
		this.chapter = chapter;
		this.verse = verse;
	}
	
	public static ContextMetadata forBible() {
		return new ContextMetadata(ContextType.BIBLE, null, 0, 0);
	}
	
	public static ContextMetadata forBook(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK, book, 0, 0);
	}
	
	public static ContextMetadata forBookTitle(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK_TITLE, book, 0, 0);
	}
	
	public static ContextMetadata forBookIntro(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK_INTRO, book, 0, 0);
	}
	
	public static ContextMetadata forChapter(BibleBook book, int chapter) {
		return new ContextMetadata(ContextType.CHAPTER, book, chapter, 0);
	}
	
	public static ContextMetadata forChapterTitle(BibleBook book, int chapter) {
		return new ContextMetadata(ContextType.CHAPTER_TITLE, book, chapter, 0);
	}
	
	public static ContextMetadata forVerse(BibleBook book, int chapter, int verse) {
		return new ContextMetadata(ContextType.VERSE, book, chapter, verse);
	}

	public static ContextMetadata forStructuredText() {
		return new ContextMetadata(ContextType.STRUCTURED_TEXT, null, 0, 0);
	}

	public static ContextMetadata forMajorSectionTitle() {
		return new ContextMetadata(ContextType.MAJOR_SECTION_TITLE, null, 0, 0);
	}

	public static ContextMetadata forSectionTitle() {
		return new ContextMetadata(ContextType.SECTION_TITLE, null, 0, 0);
	}

	public static ContextMetadata forMinorSectionTitle() {
		return new ContextMetadata(ContextType.MINOR_SECTION_TITLE, null, 0, 0);
	}

	public static ContextMetadata forParagraphBreak() {
		return new ContextMetadata(ContextType.PARAGRAPH_BREAK, null, 0, 0);
	}

	public static ContextMetadata forFlatText() {
		return new ContextMetadata(ContextType.FLAT_TEXT, null, 0, 0);
	}

	public static ContextMetadata forNote() {
		return new ContextMetadata(ContextType.NOTE, null, 0, 0);
	}

	public static ContextMetadata forText() {
		return new ContextMetadata(ContextType.TEXT, null, 0, 0);
	}




	
	public static ContextMetadata fromParent(ContextType type, ContextMetadata parent) {
		return new ContextMetadata(type, parent.book, parent.chapter, parent.verse);
	}
	
	@Override
	public boolean equals(Object other) {
		return other != null &&
			((ContextMetadata) other).type == this.type &&
			((ContextMetadata) other).book == this.book &&
			((ContextMetadata) other).chapter == this.chapter &&
			((ContextMetadata) other).verse == this.verse;
	}
	
	@Override
	public String toString() {
		return type + (book == null ? "" : "=" + book + (chapter == 0 ? "" : "_" + chapter + (verse == 0 ? "" : ":" + verse)));
	}
}