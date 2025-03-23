package com.github.unaszole.bible.writing.datamodel;

import java.util.Locale;

public class DocumentMetadata {
    /**
     * Locale that indicates the language of the document.
     */
    public final Locale locale;
    /**
     * Unique identifier for the document. Should be short and without special characters.
     */
    public final String systemName;
    /**
     * Title of the document.
     */
    public final String title;
    /**
     * Reference system used by the document.
     */
    public final String refSystem;

    public DocumentMetadata(Locale locale, String systemName, String title, String refSystem) {
        this.locale = locale;
        this.systemName = systemName;
        this.title = title;
        this.refSystem = refSystem;
    }
}
