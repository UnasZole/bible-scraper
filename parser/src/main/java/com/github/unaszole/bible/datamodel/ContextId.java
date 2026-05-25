package com.github.unaszole.bible.datamodel;

import java.util.*;
import java.util.stream.Collectors;

public class ContextId {
    public static class Builder {
        private final IdType idType;
        private final Map<IdField, Object> idMap = new HashMap<>();

        private Builder(IdType idType, Map<IdField, Object> idMap) {
            this.idType = idType;
            this.idMap.putAll(idMap);
        }

        public Builder(IdType idType) {
            this(idType, Collections.emptyMap());
        }

        public Builder(ContextId id) {
            this(id.idType, id.idMap);
        }

        public <T> Builder with(IdField field, T value) {
            assert Arrays.stream(idType.fields).anyMatch(f -> f == field)
                    : "Field " + field + " is not allowed in ID type " + idType;
            this.idMap.put(field, value);
            return this;
        }

        public boolean has(IdField field) {
            return idMap.containsKey(field);
        }

        public ContextId build() {
            return new ContextId(idType, idMap);
        }
    }


    public final IdType idType;
    public final Map<IdField, Object> idMap;

    public ContextId(IdType idType, Map<IdField, Object> idMap) {
        assert idType.isValid(idMap);
        this.idType = idType;
        this.idMap = Collections.unmodifiableMap(idMap);
    }

    public <T> T get(IdField field) {
        return (T) idMap.get(field);
    }

    public <T> ContextId with(IdField field, T value) {
        return new Builder(idType, idMap).with(field, value).build();
    }

    public boolean isAncestorOf(ContextId potentialDescendant) {
        // A context ID is an ancestor of another if all its ID fields have the same value in the descendant.
        // (The descendant may, and usually will, have additional fields).
        return this.idMap.entrySet().stream()
                .allMatch(e -> Objects.equals(potentialDescendant.idMap.get(e.getKey()), e.getValue()));
    }

    private Optional<ContextId> withNextValueForField(IdField field) {
        Optional<?> nextFieldValue = field.valueType().next(get(field));
        return nextFieldValue.map(t -> with(field, t));
    }

    public Optional<ContextId> next() {
        return withNextValueForField(idType.fields[idType.fields.length - 1]);
    }

    private String fieldToString(IdField field) {
        return field.valueType().toString(get(field));
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
