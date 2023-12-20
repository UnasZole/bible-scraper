package com.github.unaszole.bible.osisbuilder.versification.characteristics;

import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.Versification;

public class CharacteristicsOf implements VersificationCharacteristics {
	
	private final Versification v;
	
	public CharacteristicsOf(Versification v) {
		this.v = v;
	}
	
	@Override
	public Boolean containsBook(BibleBook book) {
		return v.containsBook(book);
	}
	
	@Override
	public Integer getOrdinal(BibleBook book) {
		return v.getOrdinal(book);
	}
	
	@Override
	public Integer getLastChapter(BibleBook book) {
		return v.getLastChapter(book);
	}
	
	@Override
	public Integer getLastVerse(BibleBook book, int chapter) {
		return v.getLastVerse(book, chapter);
	}
}