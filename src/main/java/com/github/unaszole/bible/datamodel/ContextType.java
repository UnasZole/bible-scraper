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
	 * An addition by the translator for easier understanding, not present in the original text.
	 */
	TRANSLATION_ADD(NO_META, NO_VALUE, atLeastOne(TEXT)),
	QUOTE(NO_META, NO_VALUE, atLeastOne(TEXT)),
	OT_QUOTE(NO_META, NO_VALUE, atLeastOne(TEXT)),
	/**
	 * Book reference : context value must be a valid OSIS book ID.
	 */
	REF_BOOK(NO_META, BOOK_ID),
	/**
	 * Chapter reference : context value must be a valid chapter number, either integer or a roman numeral.
	 */
	REF_CHAPTER(NO_META, INTEGER_OR_ROMAN),
	REF_VERSES(NO_META, INTEGER_OR_ROMAN_LIST),
	/**
	 * Full reference data unambiguously references a target text, by providing at least a book and chapter.
	 * Verses can be omitted to refer to a full chapter.
	 */
	FULL_REF(NO_META, NO_VALUE, one(REF_BOOK), one(REF_CHAPTER), atMostOne(REF_VERSES)),
	/**
	 * Continued reference may omit the book or chapter, that should be inherited from the previous
	 * reference. Should only be used if there is a FULL_REF reference shortly before in the same text.
	 */
	CONTINUED_REF(NO_META, NO_VALUE, atMostOne(REF_BOOK), atMostOne(REF_CHAPTER), atMostOne(REF_VERSES)),
	/**
	 * Local reference may omit the book or chapter, that should be the one of the container where this reference is
	 * being written.
	 */
	LOCAL_REF(NO_META, NO_VALUE, atMostOne(REF_BOOK), atMostOne(REF_CHAPTER), atMostOne(REF_VERSES)),
	/**
	 * A reference to another location in the text.
	 * The REF_DATA child specifies exactly which text is targeted.
	 * The TEXT children contain the actual text of the reference to be rendered.
	 */
	REFERENCE(NO_META, NO_VALUE, one(FULL_REF, CONTINUED_REF, LOCAL_REF), atLeastOne(TEXT)),

	MARKUP(NO_META, NO_VALUE, one(TRANSLATION_ADD, QUOTE, OT_QUOTE, REFERENCE)),
	/**
	 * An inline text, ie. a text that does not have any structure or any note whatsoever.
	 * It may only contain semantic or formatting markup around portions of the text.
	 */
	INLINE_TEXT(NO_META, NO_VALUE, atLeastOne(TEXT, MARKUP)),

	CATCHPHRASE(NO_META, NO_VALUE, atLeastOne(TEXT)),
	ALTERNATE_TRANSLATION(NO_META, NO_VALUE, atLeastOne(TEXT)),
	NOTE_MARKUP(NO_META, NO_VALUE, one(MARKUP, CATCHPHRASE, ALTERNATE_TRANSLATION)),
	NOTE_TEXT(NO_META, NO_VALUE, atLeastOne(TEXT, NOTE_MARKUP)),

	/**
	 * A note that may be inserted at any point in a flat text, and has access to specific markup.
	 */
	NOTE(NO_META, NO_VALUE, atLeastOne(NOTE_TEXT)),

	/**
	 * A flat text, ie. a text that does not have any structure - but may contain notes.
	 * All its contents should be considered as a single string, with the notes either rendered directly in-place or
	 * via an in-place reference to an external rendering (eg. footnote).
	 */
	FLAT_TEXT(NO_META, NO_VALUE, atLeastOne(INLINE_TEXT, NOTE)),
	/**
	 * An explicit paragraph break.
	 */
	PARAGRAPH_BREAK(NO_META, NO_VALUE),
	/**
	 * This marks the beginning of a line of poetry.
	 * Anything until the next structure marker is considered part of this line.
	 * Has a context value : a positive integer denoting the indent level. If unsure, put 1.
	 */
	POETRY_LINE_START(NO_META, INTEGER),
	/**
	 * This marks the beginning of a refrain line in poetry.
	 * Anything until the next structure marker is considered part of this line.
	 */
	POETRY_REFRAIN_START(NO_META, NO_VALUE),
	POETRY_ACROSTIC_START(NO_META, NO_VALUE),
	POETRY_SELAH_START(NO_META, NO_VALUE),
	/**
	 * This marks the end of a stanza, ie a group of lines in a poem.
	 */
	POETRY_STANZA_BREAK(NO_META, NO_VALUE),
	POETRY_MARKER(NO_META, NO_VALUE, one(POETRY_LINE_START, POETRY_REFRAIN_START, POETRY_ACROSTIC_START, POETRY_SELAH_START, POETRY_STANZA_BREAK)),
	MINOR_SECTION_TITLE(NO_META, NO_VALUE, one(FLAT_TEXT)),
	SECTION_TITLE(NO_META, NO_VALUE, one(FLAT_TEXT)),
	MAJOR_SECTION_TITLE(NO_META, NO_VALUE, one(FLAT_TEXT)),
	SECTION_MARKER(NO_META, NO_VALUE, one(MAJOR_SECTION_TITLE, SECTION_TITLE, MINOR_SECTION_TITLE)),
	STRUCTURE_MARKER(NO_META, NO_VALUE, one(SECTION_MARKER, POETRY_MARKER, PARAGRAPH_BREAK)),

	/**
	 * A structured text, ie. flat texts joined by structural delimiters.
	 */
	STRUCTURED_TEXT(NO_META, NO_VALUE, any(FLAT_TEXT, STRUCTURE_MARKER)),

	/**
	 * A canonical psalm title. If several psalm titles are written successively with no STRUCTURED_TEXT in between
	 * (typically, when the title spans several verses or before the first verse), they should usually be rendered as
	 * one single title.
	 */
	PSALM_TITLE(NO_META, NO_VALUE, one(FLAT_TEXT)),

	/**
	 * A verse.
	 * Has a context value : the string representation of the verse number in the source document.
	 */
	VERSE(VERSE_LEVEL, STRING, atLeastOne(STRUCTURED_TEXT, PSALM_TITLE)),
	
	CHAPTER_INTRO(CHAPTER_LEVEL, NO_VALUE, one(FLAT_TEXT)),
	CHAPTER_TITLE(CHAPTER_LEVEL, NO_VALUE, one(FLAT_TEXT)),
	/**
	 * A chapter.
	 * Has a context value : the string representation of the chapter number in the source document.
	 */
	CHAPTER(CHAPTER_LEVEL, STRING, atMostOne(CHAPTER_TITLE), atMostOne(CHAPTER_INTRO), atMostOne(PSALM_TITLE), atMostOne(STRUCTURED_TEXT), atLeastOne(VERSE)),

	BOOK_INTRO_TITLE(BOOK_LEVEL, NO_VALUE, one(FLAT_TEXT)),
	BOOK_INTRO(BOOK_LEVEL, NO_VALUE, atMostOne(BOOK_INTRO_TITLE), one(STRUCTURED_TEXT)),
	BOOK_TITLE(BOOK_LEVEL, NO_VALUE, one(FLAT_TEXT)),
	/**
	 * A book.
	 * Has a context value : the OSIS ID of the book.
	 */
	BOOK(BOOK_LEVEL, BOOK_ID, atMostOne(BOOK_TITLE), atMostOne(BOOK_INTRO), atLeastOne(CHAPTER)),
	
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
