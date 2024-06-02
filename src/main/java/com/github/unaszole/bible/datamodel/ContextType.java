package com.github.unaszole.bible.datamodel;

import java.util.*;

import static com.github.unaszole.bible.datamodel.ContextSequence.*;
import static com.github.unaszole.bible.datamodel.ContextValueType.*;
import static com.github.unaszole.bible.datamodel.ContextMetadataType.*;

public enum ContextType {

	/**
	 * An atomic text node.
	 * Has a context value : its text contents.
	 * A sequence of such text nodes should be considered as a single string, ie. their values concatenated without additional spaces.
	 */
	TEXT(NO_META, STRING),

	/**
	 * An inline note, that may be inserted at any point in a flat text.
	 */
	NOTE(NO_META, NO_VALUE, atLeastOne(TEXT)),
	TRANSLATION_ADD(NO_META, NO_VALUE, atLeastOne(TEXT)),
	ANNOTATION(NO_META, NO_VALUE, one(NOTE, TRANSLATION_ADD)),

	/**
	 * A flat text, ie. a text that does not have any structure - but may contain notes.
	 * All its contents should be considered as a single string, with the notes either rendered directly in-place or
	 * via an in-place reference to an external rendering (eg. footnote).
	 */
	FLAT_TEXT(NO_META, NO_VALUE, atLeastOne(TEXT, ANNOTATION)),
	/**
	 * An explicit paragraph break.
	 */
	PARAGRAPH_BREAK(NO_META, NO_VALUE),
	/**
	 * This marks the beginning of a line of poetry.
	 * Anything until the next structure marker is considered part of this line.
	 * Has a context value : a positive integer denoting the indent level. If unsure, put 1.
	 */
	POETRY_LINE_INDENT(NO_META, INTEGER),
	/**
	 * This marks the beginning of a refrain line in poetry.
	 * Anything until the next structure marker is considered part of this line.
	 */
	POETRY_REFRAIN_INDENT(NO_META, NO_VALUE),
	/**
	 * This marks the end of a stanza, ie a group of lines in a poem.
	 */
	POETRY_STANZA_BREAK(NO_META, NO_VALUE),
	POETRY_MARKER(NO_META, NO_VALUE, one(POETRY_LINE_INDENT, POETRY_REFRAIN_INDENT, POETRY_STANZA_BREAK)),
	MINOR_SECTION_TITLE(NO_META, NO_VALUE, one(FLAT_TEXT)),
	SECTION_TITLE(NO_META, NO_VALUE, one(FLAT_TEXT)),
	MAJOR_SECTION_TITLE(NO_META, NO_VALUE, one(FLAT_TEXT)),
	SECTION_MARKER(NO_META, NO_VALUE, one(MAJOR_SECTION_TITLE, SECTION_TITLE, MINOR_SECTION_TITLE)),
	STRUCTURE_MARKER(NO_META, NO_VALUE, one(SECTION_MARKER, POETRY_MARKER, PARAGRAPH_BREAK)),

	/**
	 * A structured text, ie. flat texts joined by structural delimiters.
	 * Two successive flat texts are joined by an implicit paragraph break.
	 * Explicit paragraph breaks should be used when a paragraph starts before the first flat text or after the last.
	 */
	STRUCTURED_TEXT(NO_META, NO_VALUE, any(FLAT_TEXT, STRUCTURE_MARKER)),
	
	// Verse must be built from a lexeme that provides a verse number.
	/**
	 * A verse.
	 * Has a context value : the string representation of the verse number in the source document.
	 */
	VERSE(VERSE_LEVEL, STRING, atLeastOne(STRUCTURED_TEXT)),
	
	CHAPTER_INTRO(CHAPTER_LEVEL, NO_VALUE, one(FLAT_TEXT)),
	CHAPTER_TITLE(CHAPTER_LEVEL, NO_VALUE, one(FLAT_TEXT)),
	/**
	 * A chapter.
	 * Has a context value : the string representation of the chapter number in the source document.
	 */
	CHAPTER(CHAPTER_LEVEL, STRING, atMostOne(CHAPTER_TITLE), atMostOne(CHAPTER_INTRO), atMostOne(STRUCTURED_TEXT), atLeastOne(VERSE)),
	
	// Book must be built from a lexeme that provides a book identifier.
	// The contained book title and intro can be derived implicitly.
	BOOK_INTRO_TITLE(BOOK_LEVEL, NO_VALUE, one(FLAT_TEXT)),
	BOOK_INTRO(BOOK_LEVEL, NO_VALUE, atMostOne(BOOK_INTRO_TITLE), one(STRUCTURED_TEXT)),
	BOOK_TITLE(BOOK_LEVEL, NO_VALUE, one(FLAT_TEXT)),
	BOOK(BOOK_LEVEL, NO_VALUE, atMostOne(BOOK_TITLE), atMostOne(BOOK_INTRO), atLeastOne(CHAPTER)),
	
	// Bible is the root element, it does not contain any metadata nor data.
	BIBLE(NO_META, NO_VALUE, atLeastOne(BOOK));

	public final ContextMetadataType metaType;
	public final ContextValueType valueType;
	
	/**
		The sequences of allowed children for this type of context.
	*/
	final ContextSequence[] allowedChildren;
	
	ContextType(ContextMetadataType metaType, ContextValueType valueType, ContextSequence... allowedChildren) {
		this.metaType = metaType;
		this.valueType = valueType;
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
