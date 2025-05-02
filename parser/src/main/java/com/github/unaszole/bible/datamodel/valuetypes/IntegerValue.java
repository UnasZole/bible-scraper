package com.github.unaszole.bible.datamodel.valuetypes;

import com.github.unaszole.bible.datamodel.ValueType;

import java.util.regex.Pattern;

public class IntegerValue implements ValueType.Definition<Integer> {

    private static final Pattern INTEGER = Pattern.compile("^\\d+$");
    private static final Pattern ROMAN_NUM = Pattern.compile("^[MDCLXVI]+$");

    public static int parseRomanNumeral(String roman) {
        if(roman.isEmpty()) {
            return 0;
        }
        if(roman.startsWith("M")) { return 1000 + parseRomanNumeral(roman.substring(1)); }
        if(roman.startsWith("CM")) { return 900 + parseRomanNumeral(roman.substring(2)); }
        if(roman.startsWith("D")) { return 500 + parseRomanNumeral(roman.substring(1)); }
        if(roman.startsWith("CD")) { return 400 + parseRomanNumeral(roman.substring(2)); }
        if(roman.startsWith("C")) { return 100 + parseRomanNumeral(roman.substring(1)); }
        if(roman.startsWith("XC")) { return 90 + parseRomanNumeral(roman.substring(2)); }
        if(roman.startsWith("L")) { return 50 + parseRomanNumeral(roman.substring(1)); }
        if(roman.startsWith("XL")) { return 40 + parseRomanNumeral(roman.substring(2)); }
        if(roman.startsWith("X")) { return 10 + parseRomanNumeral(roman.substring(1)); }
        if(roman.startsWith("IX")) { return 9 + parseRomanNumeral(roman.substring(2)); }
        if(roman.startsWith("V")) { return 5 + parseRomanNumeral(roman.substring(1)); }
        if(roman.startsWith("IV")) { return 4 + parseRomanNumeral(roman.substring(2)); }
        if(roman.startsWith("I")) { return 1 + parseRomanNumeral(roman.substring(1)); }
        throw new NumberFormatException(roman + " is not a valid roman numeral");
    }

    public static int parseInt(String str) {
        if(INTEGER.matcher(str).matches()) {
            return Integer.parseInt(str);
        }
        else if(ROMAN_NUM.matcher(str.toUpperCase()).matches()) {
            return parseRomanNumeral(str.toUpperCase());
        }
        throw new NumberFormatException(str + " is not a valid integer");
    }

    @Override
    public Class<Integer> getValueClass() {
        return Integer.class;
    }

    @Override
    public Integer valueOf(String value) throws IllegalArgumentException {
        return parseInt(value);
    }
}
