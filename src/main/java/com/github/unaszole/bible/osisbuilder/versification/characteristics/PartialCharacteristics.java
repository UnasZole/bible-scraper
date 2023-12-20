package com.github.unaszole.bible.osisbuilder.versification.characteristics;

import java.util.HashMap;
import java.util.Map;

import org.crosswire.jsword.versification.BibleBook;

public class PartialCharacteristics implements VersificationCharacteristics {
	
	private final Map<BibleBook, Boolean> bookIsPresent = new HashMap<>();
	private final Map<BibleBook, Integer> bookOrdinals = new HashMap<>();
	private final Map<BibleBook, Integer> bookNbChapters = new HashMap<>();
	private final Map<BibleBook, Map<Integer, Integer>> bookChapterNbVerses = new HashMap<>();
	
	@Override
	public Boolean containsBook(BibleBook book) {
		return bookIsPresent.get(book);
	}
	
	@Override
	public Integer getOrdinal(BibleBook book) {
		// If book is absent or unknown...
		Boolean isPresent = containsBook(book);
		if(isPresent == null) {
			return null;
		}
		if(!isPresent) {
			return 0;
		}
		
		// If book is present, then check if we know its ordinal.
		Integer ordinal = bookOrdinals.get(book);
		if(ordinal == null) {
			return null;
		}
		return ordinal;
	}
	
	@Override
	public Integer getLastChapter(BibleBook book) {
		// If book is absent or unknown...
		Boolean isPresent = containsBook(book);
		if(isPresent == null) {
			return null;
		}
		if(!isPresent) {
			return 0;
		}
		
		// If book is present, then check if we know its number of chapters.
		Integer nbChapters = bookNbChapters.get(book);
		if(nbChapters == null) {
			return null;
		}
		return nbChapters;
	}
	
	@Override
	public Integer getLastVerse(BibleBook book, int chapter) {
		// If book is absent or unknown...
		Boolean isPresent = containsBook(book);
		if(isPresent == null) {
			return null;
		}
		if(!isPresent) {
			return 0;
		}
		
		// If chapter is known to be absent...
		int lastChapter = getLastChapter(book);
		if(lastChapter >= 0 && lastChapter < chapter) {
			return 0;
		}
		
		// If we don't have verse information for this book...
		Map<Integer, Integer> chaptersNbVerses = bookChapterNbVerses.get(book);
		if(chaptersNbVerses == null) {
			return null;
		}
		
		// If we don't have verse information for this chapter...
		Integer nbVerses = chaptersNbVerses.get(chapter);
		if(nbVerses == null) {
			return null;
		}
		return nbVerses;
	}
	
	public void setContainsBook(BibleBook book, boolean containsBook) {
		bookIsPresent.put(book, containsBook);
	}
	
	public void setOrdinal(BibleBook book, int ordinal) {
		setContainsBook(book, true);
		bookOrdinals.put(book, ordinal);
	}
	
	public void setLastChapter(BibleBook book, int nbChapters) {
		setContainsBook(book, true);
		bookNbChapters.put(book, nbChapters);
	}
	
	public void setLastVerse(BibleBook book, int chapter, int nbVerses) {
		setContainsBook(book, true);
		
		Map<Integer, Integer> chaptersNbVerses = bookChapterNbVerses.get(book);
		if(chaptersNbVerses == null) {
			chaptersNbVerses = new HashMap<>();
			bookChapterNbVerses.put(book, chaptersNbVerses);
		}
		
		chaptersNbVerses.put(chapter, nbVerses);
	}
	
	@Override
	public String toString() {
		return "bookIsPresent:" + bookIsPresent + 
			" bookOrdinals:" + bookOrdinals +
			" bookNbChapters:" + bookNbChapters +
			" bookChapterNbVerses:" + bookChapterNbVerses;
	}
}