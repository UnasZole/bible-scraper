package com.github.unaszole.bible.osisbuilder.versification;

import com.github.unaszole.bible.osisbuilder.versification.characteristics.VersificationCharacteristics;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.crosswire.jsword.versification.BibleBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersificationDiff {
	
	private static final Logger LOG = LoggerFactory.getLogger(VersificationDiff.class);
	
	public interface DifferenceHandler {
		public boolean handleContainsBookDifference(BibleBook book, boolean aContainsBook, boolean bContainsBook);
		public boolean handleBookOrdinalDifference(BibleBook book, int aOrdinal, int bOrdinal);
		public boolean handleLastChapterDifference(BibleBook book, int aLastChapter, int bLastChapter);
		public boolean handleLastVerseDifference(BibleBook book, int chapter, int aLastVerse, int bLastVerse);
	}
	
	public static void handleDifferences(VersificationCharacteristics a, VersificationCharacteristics b, DifferenceHandler handler) {
		// Find a difference between a and b
		
		Set<BibleBook> presentBooks = new HashSet<>();
		
		// First kind of difference : is a book present or missing ?
		for (BibleBook book : BibleBook.values()) {
			Boolean aHasBook = a.containsBook(book);
			Boolean bHasBook = b.containsBook(book);
			
			if(aHasBook != null && bHasBook != null && aHasBook.booleanValue() != bHasBook.booleanValue()) {
				LOG.info("Difference : Has book {}. {} vs. {}", book.getOSIS(), aHasBook, bHasBook);
				if(!handler.handleContainsBookDifference(book, aHasBook, bHasBook)) {
					return;
				}
			}
			
			if(aHasBook != null && aHasBook) {
				presentBooks.add(book);
			}
		}
		
		Set<BibleBook> booksWithChapters = new HashSet<>();
		
		// The same books are on both sides.
		// Second kind of difference : do books have the same number of chapters ?
		for (BibleBook book : presentBooks) {
			Integer aNbChapters = a.getLastChapter(book);
			Integer bNbChapters = b.getLastChapter(book);
			if(aNbChapters != null && bNbChapters != null && aNbChapters.intValue() != bNbChapters.intValue()) {
				LOG.info("Difference : chapters in {}. {} vs. {}", book.getOSIS(), aNbChapters, bNbChapters);
				if(!handler.handleLastChapterDifference(book, aNbChapters, bNbChapters)) {
					return;
				}
			}
			
			if(aNbChapters != null && aNbChapters != 0) {
				booksWithChapters.add(book);
			}
		}
		
		// Same books with same number of chapters on both sides.
		// Third kind of difference : do chapters have the same number of verses ?
		for (BibleBook book : booksWithChapters) {
			int nbChapters = a.getLastChapter(book);
			for(int i = 0; i < nbChapters; i++) {
				Integer aNbVerses = a.getLastVerse(book, i);
				Integer bNbVerses = b.getLastVerse(book, i);
				
				if(aNbVerses != null && bNbVerses != null && aNbVerses.intValue() != bNbVerses.intValue()) {
					LOG.info("Difference : verses in {}:{}. {} vs. {}", book.getOSIS(), i, aNbVerses, bNbVerses);
					if(!handler.handleLastVerseDifference(book, i, aNbVerses, bNbVerses)) {
						return;
					}
				}
			}
		}
		
		// No difference in contents...
		// Fourth kind of difference : are books in the same order ?
		for (BibleBook book : presentBooks) {
			Integer aOrdinal = a.getOrdinal(book);
			Integer bOrdinal = b.getOrdinal(book);
			
			if(aOrdinal != null && bOrdinal != null && aOrdinal.intValue() != bOrdinal.intValue()) {
				LOG.info("Difference : ordinal for {}. {} vs. {}", book.getOSIS(), aOrdinal, bOrdinal);
				if(!handler.handleBookOrdinalDifference(book, aOrdinal, bOrdinal)) {
					return;
				}
			}
		}
		
		LOG.info("No difference left");
	}
	
	public static boolean match(VersificationCharacteristics a, VersificationCharacteristics b) {
		final AtomicReference<Boolean> ret = new AtomicReference(true);
		// If any actual difference is found, set as not matching and stop counting.
		handleDifferences(a, b, new DifferenceHandler() {
			public boolean handleContainsBookDifference(BibleBook book, boolean aContainsBook, boolean bContainsBook) {
				ret.set(false);
				return false;
			}
			public boolean handleBookOrdinalDifference(BibleBook book, int aOrdinal, int bOrdinal) {
				ret.set(false);
				return false;
			}
			public boolean handleLastChapterDifference(BibleBook book, int aLastChapter, int bLastChapter) {
				ret.set(false);
				return false;
			}
			public boolean handleLastVerseDifference(BibleBook book, int chapter, int aLastVerse, int bLastVerse) {
				ret.set(false);
				return false;
			}
		});
		
		return ret.get();
	}
	
	public static int getDistance(VersificationCharacteristics a, VersificationCharacteristics b) {
		final AtomicReference<Integer> ret = new AtomicReference(0);
		// When actual differences are found, increase the distance appropriately and continue.
		handleDifferences(a, b, new DifferenceHandler() {
			public boolean handleContainsBookDifference(BibleBook book, boolean aContainsBook, boolean bContainsBook) {
				ret.set(ret.get() + 100);
				return true;
			}
			public boolean handleBookOrdinalDifference(BibleBook book, int aOrdinal, int bOrdinal) {
				ret.set(ret.get() + 1);
				return true;
			}
			public boolean handleLastChapterDifference(BibleBook book, int aLastChapter, int bLastChapter) {
				ret.set(ret.get() + 10);
				return true;
			}
			public boolean handleLastVerseDifference(BibleBook book, int chapter, int aLastVerse, int bLastVerse) {
				ret.set(ret.get() + (aLastVerse - bLastVerse));
				return true;
			}
		});
		
		return ret.get();
	}
}