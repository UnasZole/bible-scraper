package com.github.unaszole.bible.datamodel;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public interface ValueType<ValueClass> {
    class ClassBased<ValueClass> implements ValueType<ValueClass> {
        private final Class<ValueClass> valueClass;
        private final Function<String, ValueClass> parser;

        public ClassBased(Class<ValueClass> valueClass, Function<String, ValueClass> parser) {
            this.valueClass = valueClass;
            this.parser = parser;
        }

        @Override
        public ValueClass valueOf(Object value) throws IllegalArgumentException {
            if(valueClass.isInstance(value)) {
                return (ValueClass) value;
            }
            else if(value instanceof String) {
                return parser.apply((String) value);
            }
            else {
                throw new IllegalArgumentException("Value '" + value + "' cannot be interpreted as a " + valueClass);
            }
        }
    }

    class ClassBasedSequential<ValueClass> extends ClassBased<ValueClass> {
        private final ValueClass first;
        private final Function<ValueClass, ValueClass> nextGetter;

        public ClassBasedSequential(Class<ValueClass> valueClass, Function<String, ValueClass> parser,
                                    ValueClass first, Function<ValueClass, ValueClass> nextGetter) {
            super(valueClass, parser);
            this.first = first;
            this.nextGetter = nextGetter;
        }

        @Override
        public Optional<ValueClass> first() {
            return Optional.of(first);
        }

        @Override
        public Optional<ValueClass> next(Object previous) {
            return Optional.of(nextGetter.apply((ValueClass) previous));
        }
    }

    /**
     *
     * @param value The value as returned by the parser. May be of any type (often String).
     * @return The value in the canonical representation of this type. This returned object must implement equals and
     * hashCode properly if this value is used in a context ID field.
     * @throws IllegalArgumentException If the given value could not be interpreted as a value of this type.
     */
    ValueClass valueOf(Object value) throws IllegalArgumentException;

    /**
     *
     * @return The first value of this type, if the type defines a discrete order.
     */
    default Optional<ValueClass> first() {
        return Optional.empty();
    }

    /**
     *
     * @param previous A previous value of this type.
     * @return The following value of this type, if the type defines a discrete order.
     */
    default Optional<ValueClass> next(Object previous) {
        return Optional.empty();
    }

    /**
     *
     * @param value A value of this type.
     * @return The string representation of this value.
     */
    default String toString(ValueClass value) {
        return Objects.toString(value);
    }
}
