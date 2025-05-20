package com.github.unaszole.bible.datamodel.contexttypes;

import com.github.unaszole.bible.datamodel.*;

import static com.github.unaszole.bible.datamodel.ContextChildrenSpec.ContextSequence.*;
import static com.github.unaszole.bible.datamodel.IdType.NO_ID;
import static com.github.unaszole.bible.datamodel.ImplicitValue.*;
import static com.github.unaszole.bible.datamodel.ValueType.*;

public enum FlatText implements ContextType {
    /**
     * An atomic text node.
     * Has a context value : its text contents.
     * A sequence of such text nodes should be considered as a single string, ie. their values concatenated without additional spaces.
     */
    TEXT(NO_ID, STRING, NO_IMPLICIT),

    /**
     * An addition by the translator for easier understanding, not present in the original text.
     */
    TRANSLATION_ADD(NO_ID, NO_VALUE, NO_IMPLICIT, atLeastOne(TEXT)),
    QUOTE(NO_ID, NO_VALUE, NO_IMPLICIT, atLeastOne(TEXT)),
    OT_QUOTE(NO_ID, NO_VALUE, NO_IMPLICIT, atLeastOne(TEXT)),
    SPEAKER(NO_ID, NO_VALUE, NO_IMPLICIT, atLeastOne(TEXT)),
    /**
     * Book reference : context value must be a valid OSIS book ID.
     */
    REF_BOOK(NO_ID, BOOK_ID, NO_IMPLICIT),
    /**
     * Chapter reference : context value must be a valid chapter number, either integer or a roman numeral.
     */
    REF_CHAPTER(NO_ID, INTEGER, NO_IMPLICIT),
    REF_VERSES(NO_ID, INTEGER_LIST, NO_IMPLICIT),
    /**
     * Full reference data unambiguously references a target text, by providing at least a book and chapter.
     * Verses can be omitted to refer to a full chapter.
     */
    FULL_REF(NO_ID, NO_VALUE, NO_IMPLICIT, one(REF_BOOK), one(REF_CHAPTER), atMostOne(REF_VERSES)),
    /**
     * Continued reference may omit the book or chapter, that should be inherited from the previous
     * reference. Should only be used if there is a FULL_REF reference shortly before in the same text.
     */
    CONTINUED_REF(NO_ID, NO_VALUE, NO_IMPLICIT, atMostOne(REF_BOOK), atMostOne(REF_CHAPTER), atMostOne(REF_VERSES)),
    /**
     * Local reference may omit the book or chapter, that should be the one of the container where this reference is
     * being written.
     */
    LOCAL_REF(NO_ID, NO_VALUE, NO_IMPLICIT, atMostOne(REF_BOOK), atMostOne(REF_CHAPTER), atMostOne(REF_VERSES)),
    /**
     * A reference to another location in the text.
     * The _REF child specifies exactly which text is targeted.
     * The TEXT children contain the actual text of the reference to be rendered.
     */
    REFERENCE(NO_ID, NO_VALUE, NO_IMPLICIT, one(FULL_REF, CONTINUED_REF, LOCAL_REF), atLeastOne(TEXT)),
    /**
     * A link to an external document.
     * The value contains the URL of the link.
     * The TEXT children contain the text of the link to be rendered.
     */
    LINK(NO_ID, URI, NO_IMPLICIT, atLeastOne(TEXT)),

    MARKUP(NO_ID, NO_VALUE, NULL, one(TRANSLATION_ADD, QUOTE, OT_QUOTE, SPEAKER, REFERENCE, LINK)),
    /**
     * An inline text, ie. a text that does not have any structure or any note whatsoever.
     * It may only contain semantic or formatting markup around portions of the text.
     */
    INLINE_TEXT(NO_ID, NO_VALUE, NULL, atLeastOne(MARKUP, TEXT)),

    CATCHPHRASE(NO_ID, NO_VALUE, NO_IMPLICIT, atLeastOne(TEXT)),
    ALTERNATE_TRANSLATION(NO_ID, NO_VALUE, NO_IMPLICIT, atLeastOne(TEXT)),
    NOTE_MARKUP(NO_ID, NO_VALUE, NULL, one(MARKUP, CATCHPHRASE, ALTERNATE_TRANSLATION)),
    NOTE_TEXT(NO_ID, NO_VALUE, NULL, atLeastOne(NOTE_MARKUP, TEXT)),

    /**
     * A note that may be inserted at any point in a flat text, and has access to specific markup.
     */
    NOTE(NO_ID, NO_VALUE, NO_IMPLICIT, atLeastOne(NOTE_TEXT)),

    /**
     * A flat text, ie. a text that does not have any structure - but may contain notes.
     * All its contents should be considered as a single string, with the notes either rendered directly in-place or
     * via an in-place reference to an external rendering (eg. footnote).
     */
    FLAT_TEXT(NO_ID, NO_VALUE, NULL, atLeastOne(INLINE_TEXT, NOTE));

    private final IdType idType;
    private final ValueType valueType;
    private final ImplicitValue implicitValue;
    private final ContextChildrenSpec childrenSpec;

    FlatText(IdType idType, ValueType valueType, ImplicitValue implicitValue, ContextChildrenSpec.ContextSequence... allowedChildren) {
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
