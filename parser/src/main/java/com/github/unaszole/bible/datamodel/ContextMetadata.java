package com.github.unaszole.bible.datamodel;

import org.crosswire.jsword.versification.BibleBook;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

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
	 * The verse numbers in standardised OSIS-compatible form, ie. the position of the verses within the book,
	 * starting at 1.
	 * In most cases, this should only contain one verse number, as one VERSE context should usually contain a single
	 * verse. However, in some rare cases, translators merge several verses into one in order to reorder the words into
	 * something more natural in the target language : in these cases, one single VERSE context may actually correspond
	 * to several actual verses. All the corresponding verse numbers must then be listed here.
	 */
	public final int[] verses;
	
	public ContextMetadata(ContextType type, BibleBook book, int chapter, int[] verses) {
		this.type = type;
		this.book = type.metaType.hasBook ? book : null;
		this.chapter = type.metaType.hasChapter ? chapter : 0;
		this.verses = type.metaType.hasVerses ? verses : null;
	}

	public ContextMetadata(ContextType type, BibleBook book, int chapter, int verse) {
		this(type, book, chapter, new int[] { verse });
	}

	public ContextMetadata(ContextType type, BibleBook book, int chapter) {
		this(type, book, chapter, null);
	}

	public ContextMetadata(ContextType type, BibleBook book) {
		this(type, book, 0);
	}

	public ContextMetadata(ContextType type) {
		this(type, null);
	}
	
	public static ContextMetadata forBible() {
		return new ContextMetadata(ContextType.BIBLE);
	}
	
	public static ContextMetadata forBook(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK, book);
	}
	
	public static ContextMetadata forBookTitle(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK_TITLE, book);
	}
	
	public static ContextMetadata forBookIntro(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK_INTRO, book);
	}

	public static ContextMetadata forBookIntroTitle(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK_INTRO_TITLE, book);
	}
	
	public static ContextMetadata forChapter(BibleBook book, int chapter) {
		return new ContextMetadata(ContextType.CHAPTER, book, chapter);
	}
	
	public static ContextMetadata forChapterTitle(BibleBook book, int chapter) {
		return new ContextMetadata(ContextType.CHAPTER_TITLE, book, chapter);
	}
	
	public static ContextMetadata forVerse(BibleBook book, int chapter, int verse) {
		return new ContextMetadata(ContextType.VERSE, book, chapter, verse);
	}

	public static ContextMetadata forMergedVerses(BibleBook book, int chapter, int[] verses) {
		return new ContextMetadata(ContextType.VERSE, book, chapter, verses);
	}

	public static ContextMetadata forStructuredText() {
		return new ContextMetadata(ContextType.STRUCTURED_TEXT);
	}

	public static ContextMetadata forMajorSectionTitle() {
		return new ContextMetadata(ContextType.MAJOR_SECTION_TITLE);
	}

	public static ContextMetadata forSectionTitle() {
		return new ContextMetadata(ContextType.SECTION_TITLE);
	}

	public static ContextMetadata forMinorSectionTitle() {
		return new ContextMetadata(ContextType.MINOR_SECTION_TITLE);
	}

	public static ContextMetadata forPoetryLineIndent() {
		return new ContextMetadata(ContextType.POETRY_LINE_START);
	}

	public static ContextMetadata forPoetryRefrainIndent() {
		return new ContextMetadata(ContextType.POETRY_REFRAIN_START);
	}

	public static ContextMetadata forPoetryStanzaBreak() {
		return new ContextMetadata(ContextType.POETRY_STANZA_BREAK);
	}

	public static ContextMetadata forParagraphBreak() {
		return new ContextMetadata(ContextType.PARAGRAPH_BREAK);
	}

	public static ContextMetadata forFlatText() {
		return new ContextMetadata(ContextType.FLAT_TEXT);
	}

	public static ContextMetadata forNote() {
		return new ContextMetadata(ContextType.NOTE);
	}

	public static ContextMetadata forText() {
		return new ContextMetadata(ContextType.TEXT);
	}

	public Optional<ContextMetadata> getImplicitChildOfType(ContextType implicitType, ContextMetadata previousOfType) {
		BibleBook implicitBook = this.book;
		if(implicitType.metaType.hasBook && implicitBook == null) {
			// Can't guess a book implicitly, as there is no natural ordering of books.
			return Optional.empty();
		}

		int implicitChapter = this.chapter;
		if(implicitType.metaType.hasChapter && implicitChapter == 0) {
			if(previousOfType == null) {
				// If no chapter has been found yet, allow an implicit first chapter.
				// (To handle books with a single unmarked chapter).
				implicitChapter = 1;
			}
			else {
				// No guessing of more chapters.
				return Optional.empty();
			}
		}

		int[] implicitVerses = this.verses;
		if(implicitType.metaType.hasVerses && implicitVerses == null) {
			// Can't guess a verse number, as those should always be explicitly written.
			return Optional.empty();
		}

		return Optional.of(new ContextMetadata(implicitType, implicitBook, implicitChapter, implicitVerses));
	}
	
	public static ContextMetadata fromParent(ContextType type, ContextMetadata parent) {
		return new ContextMetadata(type, parent.book, parent.chapter, parent.verses);
	}
	
	@Override
	public boolean equals(Object other) {
		return other != null && other.getClass() == this.getClass() &&
			((ContextMetadata) other).type == this.type &&
			((ContextMetadata) other).book == this.book &&
			((ContextMetadata) other).chapter == this.chapter &&
			Arrays.equals(((ContextMetadata) other).verses, this.verses);
	}
	
	@Override
	public String toString() {
		return type +
				(book == null ? "" : "=" + book +
						(chapter == 0 ? "" : "_" + chapter +
								(verses == null || verses.length == 0 ? "" : ":" + Arrays.stream(verses)
										.mapToObj(Integer::toString).collect(Collectors.joining(",")))));
	}
}