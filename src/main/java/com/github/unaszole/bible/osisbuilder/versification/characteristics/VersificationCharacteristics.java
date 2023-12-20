package com.github.unaszole.bible.osisbuilder.versification.characteristics;

import org.crosswire.jsword.versification.BibleBook;

public interface VersificationCharacteristics {
	
	/**
	@param book The book to check.
	@return true if versification contains the book, false if not, null if we don't know.
	*/
	public Boolean containsBook(BibleBook book);
	
	/**
	@param book The book to check.
	@return The index (starting at 1) of the given book in the bible. 0 if the book is not present, null if we don't know.
	*/
	public Integer getOrdinal(BibleBook book);
	
	/**
	@param book The book to check.
	@return The number of chapters in the given book. 0 if the book is not present, null if we don't know.
	*/
	public Integer getLastChapter(BibleBook book);
	
	/**
	@param book The book to check.
	@param chapter The number of the chapter to check.
	@return The number of verses in the given chapter. 0 if the chapter is not present, null if we don't know.
	*/
	public Integer getLastVerse(BibleBook book, int chapter);
}