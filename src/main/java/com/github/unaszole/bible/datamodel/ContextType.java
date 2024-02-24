package com.github.unaszole.bible.datamodel;

import java.util.*;

import static com.github.unaszole.bible.datamodel.ContextSequence.*;

public enum ContextType {

	/**
	 * An atomic text node.
	 * Has a context value : its text contents.
	 * A sequence of such text nodes should be considered as a single string, ie. their values concatenated without additional spaces.
	 */
	TEXT(false),
	PARAGRAPH_BREAK(false),

	/**
	 * An inline note, that may be inserted at any point in a flat text.
	 */
	NOTE(false, atLeastOne(TEXT)),
	/**
	 * A flat text, ie. a text that does not have any structure - but may contain notes.
	 * All its contents should be considered as a single string, with the notes either rendered directly in-place or
	 * via an in-place reference to an external rendering (eg. footnote).
	 */
	FLAT_TEXT(true, atLeastOne(NOTE, TEXT)),
	MINOR_SECTION_TITLE(false, one(FLAT_TEXT)),
	SECTION_TITLE(false, one(FLAT_TEXT)),
	MAJOR_SECTION_TITLE(false, one(FLAT_TEXT)),
	/**
	 * A structured text, ie. flat texts joined by structural delimiters.
	 * Two successive flat texts are joined by an implicit paragraph break.
	 * Explicit paragraph breaks should be used when a paragraph starts before the first flat text or after the last.
	 */
	STRUCTURED_TEXT(true, any(MAJOR_SECTION_TITLE, SECTION_TITLE, MINOR_SECTION_TITLE, FLAT_TEXT, PARAGRAPH_BREAK)),
	
	// Verse must be built from a lexeme that provides a verse number.
	/**
	 * A verse.
	 * Has a context value : the string representation of the verse number in the source document.
	 */
	VERSE(false, atLeastOne(STRUCTURED_TEXT)),
	
	// Chapter must be built from a lexeme that provides a chapter number.
	// The chapter title can be derived implicitly.
	// The chapter may start with some structure elements before the verses (usually just a section title).
	// Other structure element delimiters (ie section titles, paragraphs.) will be contained inside each verse context.
	CHAPTER_TITLE(true, one(FLAT_TEXT)),
	/**
	 * A chapter.
	 * Has a context value : the string representation of the chapter number in the source document.
	 */
	CHAPTER(false, atMostOne(CHAPTER_TITLE), atMostOne(STRUCTURED_TEXT), atLeastOne(VERSE)),
	
	// Book must be built from a lexeme that provides a book identifier.
	// The contained book title and intro can be derived implicitly.
	BOOK_INTRO_TITLE(false, one(FLAT_TEXT)),
	BOOK_INTRO(true, atMostOne(BOOK_INTRO_TITLE), one(STRUCTURED_TEXT)),
	BOOK_TITLE(false, one(FLAT_TEXT)),
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
	
	public List<ContextType> getAllowedTypesForNextChild(List<ContextType> currentChildrenTypes) {
		List<ContextType> allowedTypes = new ArrayList<>();
		
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
	
	public List<ContextType> getAllowedTypesForFirstChild() {
		return getAllowedTypesForNextChild(List.of());
	}
}
