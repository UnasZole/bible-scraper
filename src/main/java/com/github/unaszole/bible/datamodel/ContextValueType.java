package com.github.unaszole.bible.datamodel;

import org.crosswire.jsword.versification.BibleBook;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public enum ContextValueType {
    NO_VALUE(Objects::isNull, true, null),
    INTEGER(Pattern.compile("^\\d+$").asPredicate(), true, "1"),
    STRING(Pattern.compile("^.*$", Pattern.DOTALL).asPredicate(), true, ""),
    INTEGER_OR_ROMAN(Pattern.compile("^(\\d+|[MmDdCcLlXxVvIi]+)$").asPredicate(), true, "1"),
    INTEGER_OR_ROMAN_LIST(Pattern.compile("^((\\d+|[MmDdCcLlXxVvIi]+)[^\\w]*)+$").asPredicate(), true, "1"),
    BOOK_ID(b -> b != null && BibleBook.fromOSIS(b) != null, false, null);

    private final Predicate<String> validator;
    public final boolean implicitAllowed;
    public final String implicitValue;

    ContextValueType(Predicate<String> validator, boolean implicitAllowed, String implicitValue) {
        this.validator = validator;
        this.implicitAllowed = implicitAllowed;
        this.implicitValue = implicitValue;
    }

    /**
     *
     *
     * @param value The value to validate.
     * @return True if the given value is valid, false otherwise.
     */
    public boolean isValid(String value) {
        return validator.test(value);
    }
}
