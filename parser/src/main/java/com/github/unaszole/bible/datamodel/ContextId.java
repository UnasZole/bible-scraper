package com.github.unaszole.bible.datamodel;

import java.util.*;
import java.util.stream.Collectors;

public class ContextId {
    public static class Builder {
        private final IdType idType;
        private final Map<IdField<?>, Object> idMap = new HashMap<>();

        private Builder(IdType idType, Map<IdField<?>, Object> idMap) {
            this.idType = idType;
            this.idMap.putAll(idMap);
        }

        public Builder(IdType idType) {
            this(idType, Collections.emptyMap());
        }

        public Builder(ContextId id) {
            this(id.idType, id.idMap);
        }

        public <T> Builder with(IdField<T> field, T value) {
            assert Arrays.stream(idType.fields).anyMatch(f -> f == field)
                    : "Field " + field + " is not allowed in ID type " + idType;
            this.idMap.put(field, value);
            return this;
        }

        public boolean has(IdField<?> field) {
            return idMap.containsKey(field);
        }

        public ContextId build() {
            return new ContextId(idType, idMap);
        }
    }


    public final IdType idType;
    private final Map<IdField<?>, Object> idMap;

    public ContextId(IdType idType, Map<IdField<?>, Object> idMap) {
        assert idType.isValid(idMap);
        this.idType = idType;
        this.idMap = Collections.unmodifiableMap(idMap);
    }

    public <T> T get(IdField<T> field) {
        return (T) idMap.get(field);
    }

    public <T> ContextId with(IdField<T> field, T value) {
        return new Builder(idType, idMap).with(field, value).build();
    }

    private <T> Optional<ContextId> withNextValueForField(IdField<T> field) {
        Optional<T> nextFieldValue = field.type.next(get(field));
        return nextFieldValue.map(t -> with(field, t));
    }

    public Optional<ContextId> next() {
        return withNextValueForField(idType.fields[idType.fields.length - 1]);
    }

    private <T> String fieldToString(IdField<T> field) {
        return field.type.toString(get(field));
    }

    @Override
    public String toString() {
        return Arrays.stream(idType.fields)
                .map(this::fieldToString)
                .collect(Collectors.joining("."));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ContextId contextId = (ContextId) o;
        return idType == contextId.idType && Objects.equals(idMap, contextId.idMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idType, idMap);
    }
}
