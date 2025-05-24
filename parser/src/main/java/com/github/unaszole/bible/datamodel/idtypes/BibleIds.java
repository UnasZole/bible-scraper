package com.github.unaszole.bible.datamodel.idtypes;

import com.github.unaszole.bible.datamodel.IdField;
import com.github.unaszole.bible.datamodel.IdType;

public class BibleIds {
    public static final IdType BOOK_ID = new IdType(new IdField[]{ BibleIdFields.BOOK});
    public static final IdType CHAPTER_ID = new IdType(new IdField[]{ BibleIdFields.BOOK, BibleIdFields.CHAPTER});
    public static final IdType VERSE_ID = new IdType(new IdField[]{ BibleIdFields.BOOK, BibleIdFields.CHAPTER, BibleIdFields.VERSES});
}
