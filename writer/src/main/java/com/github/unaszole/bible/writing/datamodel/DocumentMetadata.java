package com.github.unaszole.bible.writing.datamodel;

public class DocumentMetadata {
    /**
     * Must be a "primary language subtag" as defined in BCP 47.
     * See https://www.w3.org/International/questions/qa-choosing-language-tags
     */
    public final String language;
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

    public DocumentMetadata(String language, String systemName, String title, String refSystem) {
        this.language = language;
        this.systemName = systemName;
        this.title = title;
        this.refSystem = refSystem;
    }
}
