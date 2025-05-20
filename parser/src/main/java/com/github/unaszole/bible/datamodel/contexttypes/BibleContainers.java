package com.github.unaszole.bible.datamodel.contexttypes;

import com.github.unaszole.bible.datamodel.*;

import static com.github.unaszole.bible.datamodel.ContextChildrenSpec.ContextSequence.*;
import static com.github.unaszole.bible.datamodel.IdType.*;
import static com.github.unaszole.bible.datamodel.ImplicitValue.*;
import static com.github.unaszole.bible.datamodel.ValueType.*;

public enum BibleContainers implements ContextType {
    /**
     * A canonical psalm title. If several psalm titles are written successively with no STRUCTURED_TEXT in between
     * (typically, when the title spans several verses or before the first verse), they should usually be rendered as
     * one single title.
     */
    PSALM_TITLE(NO_ID, NO_VALUE, NULL, one(FlatText.FLAT_TEXT)),

    /**
     * A verse.
     * Has a context value : the string representation of the verse number in the source document.
     * This context value will be the empty string if the verse was opened implicitly.
     */
    VERSE(BIBLE_VERSE, STRING, EMPTY_STR, atLeastOne(StructureMarkers.STRUCTURED_TEXT, PSALM_TITLE)),

    CHAPTER_INTRO(NO_ID, NO_VALUE, NO_IMPLICIT, one(FlatText.FLAT_TEXT)),
    CHAPTER_TITLE(NO_ID, NO_VALUE, NO_IMPLICIT, one(FlatText.FLAT_TEXT)),
    /**
     * A chapter.
     * Has a context value : the string representation of the chapter number in the source document.
     * This context value will be the empty string if the chapter was opened implicitly.
     */
    CHAPTER(BIBLE_CHAPTER, STRING, EMPTY_STR, atMostOne(CHAPTER_TITLE), atMostOne(CHAPTER_INTRO), atMostOne(PSALM_TITLE), atMostOne(StructureMarkers.STRUCTURED_TEXT), atLeastOne(VERSE)),

    BOOK_INTRO_TITLE(NO_ID, NO_VALUE, NO_IMPLICIT, one(FlatText.FLAT_TEXT)),
    BOOK_INTRO(NO_ID, NO_VALUE, NULL, atMostOne(BOOK_INTRO_TITLE), one(StructureMarkers.STRUCTURED_TEXT)),
    BOOK_TITLE(NO_ID, NO_VALUE, NO_IMPLICIT, one(FlatText.FLAT_TEXT)),
    /**
     * A book.
     * Has a context value : the OSIS ID of the book.
     */
    BOOK(BIBLE_BOOK, BOOK_ID, NO_IMPLICIT, atMostOne(BOOK_TITLE), atMostOne(BOOK_INTRO), atLeastOne(CHAPTER)),
    BOOK_GROUP(NO_ID, NO_VALUE, NO_IMPLICIT, atLeastOne(BOOK)),

    // Bible is the root element, it does not contain any metadata nor data.
    BIBLE(NO_ID, NO_VALUE, NULL, atLeastOne(BOOK_GROUP, BOOK));

    private final IdType idType;
    private final ValueType valueType;
    private final ImplicitValue implicitValue;
    private final ContextChildrenSpec childrenSpec;

    BibleContainers(IdType idType, ValueType valueType, ImplicitValue implicitValue, ContextChildrenSpec.ContextSequence... allowedChildren) {
        assert idType != null && valueType != null && implicitValue != null;
        this.idType = idType;
        this.valueType = valueType;
        this.implicitValue = implicitValue;
        this.childrenSpec = new ContextChildrenSpec(allowedChildren);
    }

    @Override
    public IdType idType() {
        return idType;
    }

    @Override
    public ValueType valueType() {
        return valueType;
    }

    @Override
    public ImplicitValue implicitValue() {
        return implicitValue;
    }

    @Override
    public ContextChildrenSpec childrenSpec() {
        return childrenSpec;
    }
}
