package com.github.unaszole.bible.scraping.generic;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Specifies a sequence of chapters from this book.
 * In a chapter sequence only (and its included pages), the arguments may be specified as simple integer expressions :
 * these must start by "=" and be of the form "= $i + 1" or "= $i - 2", where $i is the OSIS number of the chapter.
 */
public class ChapterSeq extends PagesContainer {
    /**
     * The OSIS number of this chapter, if this sequence represents a single chapter.
     * If given, then {@link #from} and {@link #to} MUST be omitted.
     */
    public Integer at;
    /**
     * The first OSIS chapter number of this sequence.
     * If given, then {@link #at} MUST be omitted, and {@link #to} MUST be provided.
     */
    public Integer from;
    /**
     * The last OSIS chapter number of this sequence.
     * If given, then {@link #at} MUST be omitted, and {@link #from} MUST be provided.
     */
    public Integer to;

    /**
     * Configuration for a stream editor.
     */
    public List<StreamEditorConfig> edit;

    private static final Pattern CHAPTER_EXPR = Pattern.compile("^=\\s*([^\\s]+)\\s*(([+-])\\s*([^\\s]+)\\s*)?$");

    private int eval(String str, int chapterNb) {
        return str.equals("$i") ? chapterNb : Integer.parseInt(str);
    }

    public Stream<Integer> listChapters() {
        if (at != null) {
            return Stream.of(at);
        } else {
            return Stream.iterate(from, n -> n <= to, n -> n + 1);
        }
    }

    public boolean containsChapter(int chapterNb) {
        if (at != null) {
            assert from == null && to == null : this + " invalid : if 'at' is specified, 'from' and 'to' are forbidden.";
            return at == chapterNb;
        } else {
            assert from != null && to != null : this + " invalid : if 'at' unspecified, 'from' and 'to' are mandatory.";
            return from <= chapterNb && chapterNb <= to;
        }
    }

    private String evalChapterArg(String arg, int chapterNb) {
        if (!arg.startsWith("=")) {
            // Argument is not a numeric expression : return it as is.
            return arg;
        }

        Matcher expr = CHAPTER_EXPR.matcher(arg);
        if (expr.matches()) {
            int val = eval(expr.group(1), chapterNb);
            if (expr.group(2) != null) {
                if ("+".equals(expr.group(3))) {
                    val += eval(expr.group(4), chapterNb);
                } else {
                    val -= eval(expr.group(4), chapterNb);
                }
            }
            return Integer.toString(val);
        }
        throw new IllegalArgumentException("Invalid expression " + arg);
    }

    public List<String> getPageValues(PatternContainer seqDefaults, String patternName, final int chapterNb) {
        assert containsChapter(chapterNb);
        return getPageValues(seqDefaults, patternName, a -> evalChapterArg(a, chapterNb));
    }

    public List<URL> getPageUrls(PatternContainer seqDefaults, final int chapterNb) {
        assert containsChapter(chapterNb);
        return getPageUrls(seqDefaults, a -> evalChapterArg(a, chapterNb));
    }
}
