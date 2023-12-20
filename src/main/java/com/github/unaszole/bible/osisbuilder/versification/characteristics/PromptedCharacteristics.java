package com.github.unaszole.bible.osisbuilder.versification.characteristics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.BibleNames;

public class PromptedCharacteristics implements VersificationCharacteristics {
	
	private final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	public final PartialCharacteristics partial = new PartialCharacteristics();
	
	private String getBookNameForPrompt(BibleBook book) {
		return book.getOSIS() + " (" + BibleNames.instance().getPreferredName(book) + ")";
	}
	
	@Override
	public Boolean containsBook(BibleBook book) {
		Boolean ret = partial.containsBook(book);
		if(ret == null) {
			System.out.println("Does it have " + getBookNameForPrompt(book) + " ? 1 for yes, 0 for no");
			try {
				ret = Integer.parseInt(br.readLine()) == 1;
			}
			catch(Exception e) {
				ret = null;
			}
		}
		
		if(ret != null)
		{
			partial.setContainsBook(book, ret);
		}
		return ret;
	}
	
	@Override
	public Integer getOrdinal(BibleBook book) {
		Integer ret = partial.getOrdinal(book);
		if(ret == null) {
			System.out.println("What is the ordinal for " + getBookNameForPrompt(book) + " ?");
			try {
				ret = Integer.parseInt(br.readLine());
			}
			catch(Exception e) {
				ret = null;
			}
		}
		
		if(ret != null)
		{
			partial.setOrdinal(book, ret);
		}
		return ret;
	}
	
	@Override
	public Integer getLastChapter(BibleBook book) {
		Integer ret = partial.getLastChapter(book);
		if(ret == null) {
			System.out.println("How many chapters are there in " + getBookNameForPrompt(book) + " ?");
			try {
				ret = Integer.parseInt(br.readLine());
			}
			catch(Exception e) {
				ret = null;
			}
		}
		
		if(ret != null)
		{
			partial.setLastChapter(book, ret);
		}
		return ret;
	}
	
	@Override
	public Integer getLastVerse(BibleBook book, int chapter) {
		Integer ret = partial.getLastVerse(book, chapter);
		if(ret == null) {
			System.out.println("How many verses are there in " + getBookNameForPrompt(book) + " chapter " + chapter + " ?");
			try {
				ret = Integer.parseInt(br.readLine());
			}
			catch(Exception e) {
				ret = null;
			}
		}
		
		if(ret != null)
		{
			partial.setLastVerse(book, chapter, ret);
		}
		return ret;
	}
}