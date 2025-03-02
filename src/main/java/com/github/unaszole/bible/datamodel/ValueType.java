package com.github.unaszole.bible.datamodel;

import com.github.unaszole.bible.datamodel.valuetypes.*;

public interface ValueType<ValueClass> {
    NoValue NO_VALUE = new NoValue();
    StringValue STRING = new StringValue();
    IntegerValue INTEGER = new IntegerValue();
    IntegerListValue INTEGER_LIST = new IntegerListValue();
    UriValue URI = new UriValue();
    BookIdValue BOOK_ID = new BookIdValue();

    Class<ValueClass> getValueClass();

    ValueClass valueOf(String value) throws IllegalArgumentException;

    default ValueClass of(Object value) throws IllegalArgumentException {
        if(getValueClass().isInstance(value)) {
            return (ValueClass) value;
        }
        else if(value instanceof String) {
            return valueOf((String) value);
        }
        else {
            throw new IllegalArgumentException("Value '" + value + "' cannot be interpreted as a " + getValueClass());
        }
    }
}
