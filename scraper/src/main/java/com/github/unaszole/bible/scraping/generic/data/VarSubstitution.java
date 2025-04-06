package com.github.unaszole.bible.scraping.generic.data;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VarSubstitution {
    private static final Pattern VAR_REFERENCE = Pattern.compile("\\{([A-Z0-9_]+)}");

    public static boolean hasVars(String str) {
        return VAR_REFERENCE.matcher(str).find();
    }

    public static String substituteVars(String str, final Function<String, String> varGetter) {
        Matcher varRefs = VAR_REFERENCE.matcher(str);
        return varRefs.replaceAll(r -> varGetter.apply(r.group(1)));
    }
}
