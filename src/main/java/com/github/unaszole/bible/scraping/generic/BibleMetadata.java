package com.github.unaszole.bible.scraping.generic;

import com.github.unaszole.bible.datamodel.DocumentMetadata;

import java.util.Map;

public class BibleMetadata {
    /**
     * Must be a "primary language subtag" as defined in BCP 47.
     * See https://www.w3.org/International/questions/qa-choosing-language-tags
     * If unset, defaults to "en".
     */
    public String language;
    /**
     * The system name for the bible. Should be short and without special characters.
     * If unset, defaults to
     */
    public String systemName;
    /**
     * The title of the bible.
     */
    public String title;
    /**
     * The versification used by the bible.
     */
    public String versification;

    private String evaluateField(String fieldValue, final Map<String, String> args, String defaultValue) {
        if(fieldValue == null) {
            return defaultValue;
        }
        return PatternContainer.substituteArgs(fieldValue, args::get);
    }

    public DocumentMetadata getDocMeta(Map<String, String> args) {
        String v = evaluateField(versification, args, null);

        return new DocumentMetadata(
                evaluateField(language, args, "en"),
                evaluateField(systemName, args, "scrapedBible").replaceAll("[^a-zA-Z0-9]+", ""),
                evaluateField(title, args, "Scraped bible"),
                v == null ? "Bible" : "Bible." + v
        );
    }
}
