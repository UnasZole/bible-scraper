package com.github.unaszole.bible.datamodel;

import com.github.unaszole.bible.datamodel.idfieldtypes.BibleBookField;
import com.github.unaszole.bible.datamodel.idfieldtypes.IntegerListField;
import com.github.unaszole.bible.datamodel.idfieldtypes.IntegerField;

import java.util.Map;
import java.util.Optional;

public enum IdField {
    BIBLE_BOOK(new BibleBookField()),
    BIBLE_CHAPTER(new IntegerField()),
    BIBLE_VERSES(new IntegerListField());

    public interface FieldType<FieldClass> {
        Class<FieldClass> getFieldClass();

        /**
         * Parse a string into an ID field. Reciprocal to {@link #toString(Object)}.
         * @param field A string representation of the field.
         * @return The instantiated field.
         */
        FieldClass valueOf(String field);

        /**
         * Serialise a field into a string. Reciprocal to {@link #valueOf(String)}.
         * @param field An instantiated field.
         * @return A string representation of that field.
         */
        String toString(FieldClass field);

        /**
         * NOTE : in most cases the default implementation of this method should not be overridden.
         *
         * @param field An field that may either be fully instantiated, or passed as a string.
         * @return The fully instantiated field.
         * @throws IllegalArgumentException If the given object cannot be interpreted as a field of the needed type.
         */
        default FieldClass of(Object field) throws IllegalArgumentException {
            if(getFieldClass().isInstance(field)) {
                return (FieldClass) field;
            }
            else if(field instanceof String) {
                return valueOf((String) field);
            }
            else {
                throw new IllegalArgumentException("Field ID '" + field + "' cannot be interpreted as a " + getFieldClass());
            }
        }

        /**
         *
         * @return The value of this field for a first context in a sequence, if this type defines an order.
         * Field types that don't have an explicit order should keep the default implementation .
         */
        default Optional<FieldClass> getFirst() {
            return Optional.empty();
        }

        /**
         *
         * @param field An field of the given type.
         * @return A field value for the "next" context of the same type in a sequence, if this type defines an order.
         * Field types that don't have an explicit order should keep the default implementation .
         */
        default Optional<FieldClass> getNext(FieldClass field) {
            return Optional.empty();
        }
    }

    public final FieldType fieldType;

    IdField(FieldType<?> fieldType) {
        this.fieldType = fieldType;
    }

    public Map.Entry<IdField, Object> of(Object value) {
        return Map.entry(this, fieldType.of(value));
    }
}
