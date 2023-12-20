package com.github.unaszole.bible.osisbuilder.versification;

import com.github.unaszole.bible.osisbuilder.versification.characteristics.CharacteristicsOf;
import com.github.unaszole.bible.osisbuilder.versification.characteristics.PartialCharacteristics;
import com.github.unaszole.bible.osisbuilder.versification.characteristics.PromptedCharacteristics;
import com.github.unaszole.bible.osisbuilder.versification.characteristics.VersificationCharacteristics;

import java.util.Iterator;

import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.Versification;
import org.crosswire.jsword.versification.system.Versifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersificationSelector {
	
	private static final Logger LOG = LoggerFactory.getLogger(VersificationSelector.class);
	
	public Versification selectVersification(VersificationCharacteristics wantedCharacteristics) {
		// We will loop through all known versifications.
		Versifications versifications = Versifications.instance();
		Iterator<String> known = versifications.iterator();
		
		// We start with an empty set of known characteristics.
		final PartialCharacteristics knownCharacteristics = new PartialCharacteristics();
		
		// The current candidate, if not null, must always match the known characteristics at the beginning and end of each iteration.
		// Our first candidate versification is the first in the list, which of course matches the empty initial characteristics.
		Versification currentCandidate = known.hasNext() ? versifications.getVersification(known.next()) : null;
		
		LOG.info("Starting selection with candidate {}", currentCandidate.getName());
		
		while(known.hasNext()) {
			// We have another candidate versification to consider.
			Versification nextCandidate = versifications.getVersification(known.next());
			
			LOG.info("Checking next candidate {}", nextCandidate.getName());
			
			// Current candidate matches by design if not null.
			boolean currentMatches = currentCandidate != null;
			// Does next candidate match ?
			boolean nextMatches = VersificationDiff.match(knownCharacteristics, new CharacteristicsOf(nextCandidate));
			
			while(currentMatches && nextMatches) {
				// As long as both candidates are matching, we have a problem, we want only one.
				
				LOG.info("Both {} and {} match the current known characteristics {}", currentCandidate.getName(), nextCandidate.getName(), knownCharacteristics);
				
				VersificationCharacteristics currentCharacteristics = new CharacteristicsOf(currentCandidate);
				VersificationCharacteristics nextCharacteristics = new CharacteristicsOf(nextCandidate);
				
				// Find the first difference and use it to fill the known characteristics.
				VersificationDiff.handleDifferences(currentCharacteristics, nextCharacteristics, new VersificationDiff.DifferenceHandler() {
					public boolean handleContainsBookDifference(BibleBook book, boolean aContainsBook, boolean bContainsBook) {
						Boolean wContainsBook = wantedCharacteristics.containsBook(book);
						if(wContainsBook != null) {
							knownCharacteristics.setContainsBook(book, wContainsBook);
							return false;
						}
						return true;
					}
					public boolean handleBookOrdinalDifference(BibleBook book, int aOrdinal, int bOrdinal) {
						Integer wOrdinal = wantedCharacteristics.getOrdinal(book);
						if(wOrdinal != null) {
							knownCharacteristics.setOrdinal(book, wOrdinal);
							return false;
						}
						return true;
					}
					public boolean handleLastChapterDifference(BibleBook book, int aLastChapter, int bLastChapter) {
						Integer wLastChapter = wantedCharacteristics.getLastChapter(book);
						if(wLastChapter != null) {
							knownCharacteristics.setLastChapter(book, wLastChapter);
							return false;
						}
						return true;
					}
					public boolean handleLastVerseDifference(BibleBook book, int chapter, int aLastVerse, int bLastVerse) {
						Integer wLastVerse = wantedCharacteristics.getLastVerse(book, chapter);
						if(wLastVerse != null) {
							knownCharacteristics.setLastVerse(book, chapter, wLastVerse);
							return false;
						}
						return true;
					}
				});
				
				// With the new criteria, check if both candidates are still matching.
				currentMatches = currentCandidate == null ? false : VersificationDiff.match(knownCharacteristics, currentCharacteristics);
				nextMatches = VersificationDiff.match(knownCharacteristics, nextCharacteristics);
			}
			
			// At most one candidate is still matching.
			// Keep the one that matches - or null if both are now excluded.
			currentCandidate = currentMatches ? currentCandidate : (nextMatches ? nextCandidate : null );
			
			LOG.info("{} matches the current known characteristics {}", currentCandidate == null ? "NONE" : currentCandidate.getName(), knownCharacteristics);
		}
		
		// Return the final candidate versification that matches all known characteristics (may be null if none found).
		return currentCandidate;
	}
	
	public static void main(String args[]) {
		System.out.println("Selected " + (new VersificationSelector().selectVersification(
			new PromptedCharacteristics()
		).getName()));
	}
}