package com.github.unaszole.bible.osisbuilder.parser;

import java.util.List;

public enum ContextType {
	VERSE_ADD(List.of()),
	VERSE(List.of(VERSE_ADD)),
	
	SECTION_TITLE_ADD(List.of()),
	SECTION(List.of(SECTION_TITLE_ADD, VERSE)),
	
	CHAPTER_TITLE_ADD(List.of()),
	CHAPTER(List.of(CHAPTER_TITLE_ADD, VERSE, SECTION)),
	
	BOOK_INTRO_ADD(List.of()),
	BOOK_INTRO(List.of(BOOK_INTRO_ADD)),
	
	BOOK_TITLE_ADD(List.of()),
	BOOK(List.of(BOOK_TITLE_ADD, BOOK_INTRO, CHAPTER)),
	
	BIBLE(List.of(BOOK));
	
	public final List<ContextType> allowedChildren;
	
	ContextType(List<ContextType> allowedChildren) {
		this.allowedChildren = allowedChildren;
	}
}
