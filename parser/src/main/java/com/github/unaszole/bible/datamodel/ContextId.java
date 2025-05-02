package com.github.unaszole.bible.datamodel;

import java.util.*;

public class ContextId {
    private final IdType idType;
    private final Map<IdField, Object> idMap;

    public ContextId(IdType idType, Map<IdField, Object> idMap) {
        assert idMap.entrySet().stream().allMatch(e ->
                Arrays.stream(idType.fields).anyMatch(tf -> tf == e.getKey())
                && e.getKey().fieldType.getFieldClass().isInstance(e.getValue())
        );
        this.idType = idType;
        this.idMap = Collections.unmodifiableMap(idMap);
    }

    public <T> T get(IdField field) {
        return (T) idMap.get(field);
    }

    public ContextId with(IdField field, Object value) {
        assert Arrays.stream(idType.fields).anyMatch(f -> f == field) : "Field " + field + " is not allowed in ID type " + idType;
        Map<IdField, Object> newIdMap = new HashMap<>(idMap);
        newIdMap.put(field, field.fieldType.of(value));
        return new ContextId(idType, newIdMap);
    }

    public Optional<ContextId> next() {
        IdField lastField = idType.fields[idType.fields.length - 1];

        Optional nextLastField = lastField.fieldType.getNext(get(lastField));
        if(nextLastField.isPresent()) {
            return Optional.of(with(lastField, nextLastField.get()));
        }

        return Optional.empty();
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
