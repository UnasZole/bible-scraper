package com.github.unaszole.bible.datamodel;

import com.github.unaszole.bible.datamodel.valuetypes.*;

public enum ValueType {
    NO_VALUE(new NoValue()),
    STRING(new StringValue()),
    INTEGER(new IntegerValue()),
    INTEGER_LIST(new IntegerListValue()),
    URI(new UriValue()),
    BOOK_ID(new BookIdValue());

    public interface Definition<ValueClass> {
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

    public final Definition<?> def;

    ValueType(Definition<?> def) {
        this.def = def;
    }

    public Object of(Object value) throws IllegalArgumentException {
        return def.of(value);
    }
}
