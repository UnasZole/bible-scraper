package com.github.unaszole.bible.datamodel.idtypes;

import com.github.unaszole.bible.datamodel.IdField;
import com.github.unaszole.bible.datamodel.valuetypes.StdValueTypes;
import org.crosswire.jsword.versification.BibleBook;

import java.util.List;

public class BibleIdFields {
    public static final IdField<BibleBook> BOOK = new IdField<>(StdValueTypes.BIBLE_BOOK);
    public static final IdField<Integer> CHAPTER = new IdField<>(StdValueTypes.INTEGER);
    public static final IdField<List<Integer>> VERSES = new IdField<>(StdValueTypes.INTEGER_LIST);
}
