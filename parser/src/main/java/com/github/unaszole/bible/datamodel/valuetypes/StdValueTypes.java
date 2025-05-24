package com.github.unaszole.bible.datamodel.valuetypes;

import com.github.unaszole.bible.datamodel.ValueType;
import org.crosswire.jsword.versification.BibleBook;

import java.net.URI;
import java.util.List;

public class StdValueTypes {
    public static final ValueType<Void> NO_VALUE = o -> {
        if(o != null) {
            throw new IllegalArgumentException("Received " + o + " when no value expected");
        }
        return null;
    };
    public static final ValueType<String> STRING = new ValueType.ClassBased<>(String.class, s -> s);
    public static final ValueType<Integer> INTEGER = new IntegerValue();
    public static final ValueType<List<Integer>> INTEGER_LIST = new IntegerListValue();
    public static final ValueType<URI> URI = new ValueType.ClassBased<>(URI.class, java.net.URI::create);
    public static final ValueType<BibleBook> BIBLE_BOOK = new BibleBookValue();
}
