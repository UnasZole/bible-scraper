package com.github.unaszole.bible.scraping.generic;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.stream.ContextStream;
import com.github.unaszole.bible.stream.ContextStreamEditor;

import java.net.URL;
import java.util.List;
import java.util.function.BiFunction;
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

    public List<String> getChapterNumbers(PatternContainer bookDefaults, final int chapterNb) {
        assert containsChapter(chapterNb);
        return getPageValues(bookDefaults, "chapterPublishedNumber", a -> evalChapterArg(a, chapterNb));
    }

    public List<URL> getChapterUrls(PatternContainer bookDefaults, final int chapterNb) {
        assert containsChapter(chapterNb);
        return getPageUrls(bookDefaults, "chapterUrl", a -> evalChapterArg(a, chapterNb));
    }

    public ContextStream.Single streamChapter(PatternContainer bookDefaults, ContextMetadata chapterCtxMeta,
                                              BiFunction<Context, List<URL>, ContextStream.Single> ctxStreamer) {
        assert chapterCtxMeta.type == ContextType.CHAPTER && containsChapter(chapterCtxMeta.chapter);

        List<URL> chapterUrls = getChapterUrls(bookDefaults, chapterCtxMeta.chapter);

        if(!chapterUrls.isEmpty()) {
            // We have pages for this chapter, proceed.

            // Compute chapter value
            String chapterValue = String.join("-",
                    getChapterNumbers(bookDefaults, chapterCtxMeta.chapter)
            );

            // Prepare context with the provided filler.
            Context chapterCtx = new Context(chapterCtxMeta, chapterValue);
            ContextStream.Single chapterStream = ctxStreamer.apply(chapterCtx, chapterUrls);

            // Configure editor if provided.
            if(edit != null) {
                ContextStreamEditor<ContextStream.Single> editor = chapterStream.edit();
                for(StreamEditorConfig cfg: edit) {
                    cfg.configureEditor(editor, chapterCtxMeta.book, chapterCtxMeta.chapter);
                }
                chapterStream = editor.process();
            }

            return chapterStream;
        }

        // No page to retrieve for the chapter : no context to return.
        return null;
    }
}
