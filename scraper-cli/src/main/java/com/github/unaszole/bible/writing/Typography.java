package com.github.unaszole.bible.writing;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class Typography {

    private static class French {
        private static final char NO_BREAK_SPACE = ' ';
        private static final char NARROW_NO_BREAK_SPACE = ' ';
        private static final Pattern FR_PUNCT_BEGIN = Pattern.compile("([«])\\h*");
        private static final Pattern FR_PUNCT_END = Pattern.compile("\\h*([»:])");
        private static final Pattern FR_PUNCT_END_NARROW = Pattern.compile("\\h*([;?!])");

        public static String fixFrenchTypography(String s) {
            String ret = s;
            ret = FR_PUNCT_BEGIN.matcher(ret).replaceAll(r -> r.group(1) + NO_BREAK_SPACE);
            ret = FR_PUNCT_END.matcher(ret).replaceAll(r -> NO_BREAK_SPACE + r.group(1));
            ret = FR_PUNCT_END_NARROW.matcher(ret).replaceAll(r -> NARROW_NO_BREAK_SPACE + r.group(1));
            return ret;
        };
    }


    public enum Fixer { NONE, FRENCH }
    public static UnaryOperator<String> getFixer(Fixer fixer) {
        switch (fixer) {
            case FRENCH:
                return Typography.French::fixFrenchTypography;
            case NONE:
            default:
                return s -> s;
        }
    }
}
