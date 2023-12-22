package com.github.unaszole.bible.osisbuilder.parser;

import java.util.List;

public enum ContextType {
	VERSE_TEXT(List.of()),
	VERSE(List.of(ContextType.VERSE_TEXT)),
	SECTION(List.of(ContextType.VERSE)),
	CHAPTER(List.of(ContextType.VERSE, ContextType.SECTION)),
	BOOK_INTRO_TEXT(List.of()),
	BOOK_INTRO(List.of(BOOK_INTRO_TEXT)),
	BOOK_TITLE_TEXT(List.of()),
	BOOK(List.of(ContextType.BOOK_TITLE_TEXT, ContextType.BOOK_INTRO, ContextType.CHAPTER)),
	DOCUMENT(List.of(ContextType.BOOK));
	
	public final List<ContextType> allowedChildren;
	
	ContextType(List<ContextType> allowedChildren) {
		this.allowedChildren = allowedChildren;
	}
}
