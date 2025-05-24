package com.github.unaszole.bible.datamodel.contexttypes;

import com.github.unaszole.bible.datamodel.*;

import static com.github.unaszole.bible.datamodel.ContextChildrenSpec.ContextSequence.*;
import static com.github.unaszole.bible.datamodel.IdType.NO_ID;
import static com.github.unaszole.bible.datamodel.ImplicitValue.*;
import static com.github.unaszole.bible.datamodel.valuetypes.StdValueTypes.*;

public enum StructureMarkers implements ContextType {
    /**
     * An explicit paragraph break.
     */
    PARAGRAPH_BREAK(NO_ID, NO_VALUE, NO_IMPLICIT),
    /**
     * This marks the beginning of a line of poetry.
     * Anything until the next structure marker is considered part of this line.
     * Has a context value : a positive integer denoting the indent level. If unsure, put 1.
     */
    POETRY_LINE_START(NO_ID, INTEGER, NO_IMPLICIT),
    /**
     * This marks the beginning of a refrain line in poetry.
     * Anything until the next structure marker is considered part of this line.
     */
    POETRY_REFRAIN_START(NO_ID, NO_VALUE, NO_IMPLICIT),
    POETRY_ACROSTIC_START(NO_ID, NO_VALUE, NO_IMPLICIT),
    POETRY_SELAH_START(NO_ID, NO_VALUE, NO_IMPLICIT),
    /**
     * This marks the end of a stanza, ie a group of lines in a poem.
     */
    POETRY_STANZA_BREAK(NO_ID, NO_VALUE, NO_IMPLICIT),
    POETRY_MARKER(NO_ID, NO_VALUE, NULL, one(POETRY_LINE_START, POETRY_REFRAIN_START, POETRY_ACROSTIC_START, POETRY_SELAH_START, POETRY_STANZA_BREAK)),
    MINOR_SECTION_TITLE(NO_ID, NO_VALUE, NO_IMPLICIT, one(FlatText.FLAT_TEXT)),
    SECTION_TITLE(NO_ID, NO_VALUE, NO_IMPLICIT, one(FlatText.FLAT_TEXT)),
    MAJOR_SECTION_TITLE(NO_ID, NO_VALUE, NO_IMPLICIT, one(FlatText.FLAT_TEXT)),
    SECTION_MARKER(NO_ID, NO_VALUE, NULL, one(MAJOR_SECTION_TITLE, SECTION_TITLE, MINOR_SECTION_TITLE)),
    STRUCTURE_MARKER(NO_ID, NO_VALUE, NULL, one(SECTION_MARKER, POETRY_MARKER, PARAGRAPH_BREAK)),

    /**
     * A structured text, ie. flat texts joined by structural delimiters.
     */
    STRUCTURED_TEXT(NO_ID, NO_VALUE, NULL, any(FlatText.FLAT_TEXT, STRUCTURE_MARKER));

    private final IdType idType;
    private final ValueType<?> valueType;
    private final ImplicitValue implicitValue;
    private final ContextChildrenSpec childrenSpec;

    StructureMarkers(IdType idType, ValueType<?> valueType, ImplicitValue implicitValue, ContextChildrenSpec.ContextSequence... allowedChildren) {
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
    public ValueType<?> valueType() {
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
