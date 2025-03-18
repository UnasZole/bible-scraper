package com.github.unaszole.bible.datamodel;

public enum ImplicitValue {
    NO_IMPLICIT,
    NULL(null),
    EMPTY_STR(""),
    ONE(1);

    public final boolean implicitAllowed;
    public final Object implicitValue;

    ImplicitValue() {
        this.implicitAllowed = false;
        this.implicitValue = null;
    }

    ImplicitValue(Object implicitValue) {
        this.implicitAllowed = true;
        this.implicitValue = implicitValue;
    }
}
