package com.github.unaszole.bible.osisbuilder.parser;

import org.crosswire.jsword.versification.BibleBook;

public class ContextMetadata {
	public final ContextType type;
	public final BibleBook book;
	public final int chapter;
	public final int verse;
	
	public ContextMetadata(ContextType type, BibleBook book, int chapter, int verse) {
		this.type = type;
		this.book = book;
		this.chapter = chapter;
		this.verse = verse;
	}
	
	public static ContextMetadata forBook(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK, book, 0, 0);
	}
	
	public static ContextMetadata forBookTitleText(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK_TITLE_TEXT, book, 0, 0);
	}
	
	public static ContextMetadata forBookIntro(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK_INTRO, book, 0, 0);
	}
	
	public static ContextMetadata forBookIntroText(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK_INTRO_TEXT, book, 0, 0);
	}
	
	public static ContextMetadata forChapter(BibleBook book, int chapter) {
		return new ContextMetadata(ContextType.CHAPTER, book, chapter, 0);
	}
	
	public static ContextMetadata forSection(BibleBook book, int chapter) {
		return new ContextMetadata(ContextType.SECTION, book, chapter, 0);
	}
	
	public static ContextMetadata forVerse(BibleBook book, int chapter, int verse) {
		return new ContextMetadata(ContextType.VERSE, book, chapter, verse);
	}
	
	public static ContextMetadata forVerseText(BibleBook book, int chapter, int verse) {
		return new ContextMetadata(ContextType.VERSE_TEXT, book, chapter, verse);
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
		return type + "=" + book + (chapter == 0 ? "" : "_" + chapter + (verse == 0 ? "" : ":" + verse));
	}
}