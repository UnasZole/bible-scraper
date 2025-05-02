package com.github.unaszole.bible.datamodel;

import org.crosswire.jsword.versification.BibleBook;

import java.util.List;
import java.util.Objects;

public class ContextMetadata {
	public final ContextType type;
	public final ContextId id;
	
	public ContextMetadata(ContextType type, ContextId id) {
		this.type = type;
		this.id = id;
	}

	public ContextMetadata(ContextType type) {
		this(type, null);
	}

	public static ContextMetadata forBible() {
		return new ContextMetadata(ContextType.BIBLE);
	}

	public static ContextMetadata forBook(BibleBook book) {
		return new ContextMetadata(ContextType.BOOK, IdType.BIBLE_BOOK.ofFields(
				IdField.BIBLE_BOOK.of(book)
		));
	}

	public static ContextMetadata forChapter(BibleBook book, int chapter) {
		return new ContextMetadata(ContextType.CHAPTER, IdType.BIBLE_CHAPTER.ofFields(
				IdField.BIBLE_BOOK.of(book),
				IdField.BIBLE_CHAPTER.of(chapter)
		));
	}

	public static ContextMetadata forVerse(BibleBook book, int chapter, int verse) {
		return new ContextMetadata(ContextType.VERSE, IdType.BIBLE_VERSE.ofFields(
				IdField.BIBLE_BOOK.of(book),
				IdField.BIBLE_CHAPTER.of(chapter),
				IdField.BIBLE_VERSES.of(List.of(verse))
		));
	}
	
	@Override
	public String toString() {
		return type + (id != null ? "=" + id : "");
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		ContextMetadata that = (ContextMetadata) o;
		return type == that.type && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, id);
	}
}