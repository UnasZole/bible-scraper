package com.github.unaszole.bible.datamodel;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static com.github.unaszole.bible.datamodel.ContextSequence.*;

public enum ContextType {
	
	// Content nodes are read from actual data.
	TEXT(false),
	PARAGRAPH_BREAK(false),
	
	// A text structure may be implicitly built to encompass found content.
	NOTE(true, atLeastOne(TEXT)),
	FLAT_TEXT(true, atLeastOne(NOTE, TEXT)),
	SECTION_TITLE(true, atLeastOne(TEXT)),
	MAJOR_SECTION_TITLE(true, atLeastOne(TEXT)),
	STRUCTURED_TEXT(true, any(MAJOR_SECTION_TITLE, SECTION_TITLE, FLAT_TEXT, PARAGRAPH_BREAK)),
	
	// Verse must be built from a lexeme that provides a verse number.
	VERSE(false, one(STRUCTURED_TEXT)),
	
	// Chapter must be built from a lexeme that provides a chapter number.
	// The chapter title can be derived implicitly.
	// The chapter may start with some structure elements before the verses (usually just a section title).
	// Other structure element delimiters (ie section titles, paragraphs.) will be contained inside each verse context.
	CHAPTER_TITLE(true, atLeastOne(TEXT)),
	CHAPTER(false, atMostOne(CHAPTER_TITLE), atMostOne(STRUCTURED_TEXT), atLeastOne(VERSE)),
	
	// Book must be built from a lexeme that provides a book identifier.
	// The contained book title and intro can be derived implicitly.
	BOOK_INTRO(true, one(STRUCTURED_TEXT)),
	BOOK_TITLE(true, atLeastOne(TEXT)),
	BOOK(false, atMostOne(BOOK_TITLE), atMostOne(BOOK_INTRO), atLeastOne(CHAPTER)),
	
	// Bible is the root element, it does not contain any metadata nor data.
	BIBLE(true, atLeastOne(BOOK));
	
	/**
		True if this context can be implicitly opened, copying metadata from the parent context.
		False if this context requires additional metadata or contents from the parent, and
		must therefore be explicitly opened by a data-carrying lexeme.
	*/
	public final boolean implicitAllowed;
	
	/**
		The sequences of allowed children for this type of context.
	*/
	final ContextSequence[] allowedChildren;
	
	ContextType(boolean implicitAllowed, ContextSequence... allowedChildren) {
		this.implicitAllowed = implicitAllowed;
		this.allowedChildren = allowedChildren;
	}
	
	public Set<ContextType> getAllowedTypesForNextChild(List<ContextType> currentChildrenTypes) {
		Set<ContextType> allowedTypes = new HashSet<>();
		
		// Add all children to a queue.
		ArrayDeque<ContextType> childrenQueue = new ArrayDeque<>(currentChildrenTypes);
		
		for(ContextSequence childrenSequence: allowedChildren) {
			// Iterate through all allowed children sequences.
			
			// Consume as many children as this sequence accepts.
			switch(childrenSequence.consume(childrenQueue)) {
				case INCOMPLETE:
					// This sequence is incomplete. Next child can either extend a previous sequence, or fill this one, but nothing further.
					// Return immediately.
					allowedTypes.addAll(childrenSequence.allowedTypes);
					return allowedTypes;
				case OPEN:
					// This sequence is open : this sequence's types are accepted, but a next sequence might be allowed as well.
					// Keep iterating.
					allowedTypes.addAll(childrenSequence.allowedTypes);
					break;
				case CLOSED:
					// This sequence is closed : it does not contribute to the allowed types.
					// Keep iterating.
					break;
			}
		}
		
		return allowedTypes;
	}
	
	public boolean isIncomplete(List<ContextType> currentChildrenTypes) {
		// Add all children to a queue.
		ArrayDeque<ContextType> childrenQueue = new ArrayDeque<>(currentChildrenTypes);
		
		for(ContextSequence childrenSequence: allowedChildren) {
			// Iterate through all allowed children sequences.
			
			// Consume as many children as this sequence accepts.
			switch(childrenSequence.consume(childrenQueue)) {
				case INCOMPLETE:
					// This sequence is incomplete, so the full context is incomplete.
					return true;
				default:
					// Sequence can be considered complete, just move to the next.
					break;
			}
		}
		
		// All children sequences are complete.
		return false;
	}
	
	public Set<ContextType> getAllowedTypesForFirstChild() {
		return getAllowedTypesForNextChild(List.of());
	}
}
