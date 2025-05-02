package com.github.unaszole.bible.datamodel;

import java.util.*;

public enum IdType {
    NO_ID(null),
    BIBLE_BOOK(new IdField[]{ IdField.BIBLE_BOOK }),
    BIBLE_CHAPTER(new IdField[]{ IdField.BIBLE_BOOK, IdField.BIBLE_CHAPTER }),
    BIBLE_VERSE(new IdField[]{ IdField.BIBLE_BOOK, IdField.BIBLE_CHAPTER, IdField.BIBLE_VERSES });

    public final IdField[] fields;

    IdType(IdField[] fields) {
        this.fields = fields;
    }

    public Optional<Map<IdField, Object>> getNext(Map<IdField, Object> id) {
        assert id.size() == fields.length;

        IdField lastField = fields[fields.length - 1];

        Optional nextLastField = lastField.fieldType.getNext(id.get(lastField));
        if(nextLastField.isPresent()) {
            Map<IdField, Object> nextId = new HashMap<>(id);
            nextId.put(lastField, nextLastField.get());
            return Optional.of(Collections.unmodifiableMap(nextId));
        }

        return Optional.empty();
    }

    public Optional<Map<IdField, Object>> getFirst(Deque<ContextMetadata> ancestorStack) {
        IdField lastField = fields[fields.length - 1];

        Optional newLastField = lastField.fieldType.getFirst();
        if(newLastField.isPresent()) {
            Map<IdField, Object> nextId = new HashMap<>();
            // Loop through all ancestors to collect their ID fields.
            for(ContextMetadata ancestor: ancestorStack) {
                if(ancestor.type.idType.fields != null) {
                    for(IdField fieldDef: ancestor.type.idType.fields) {
                        if(Arrays.stream(fields).anyMatch(f -> f == fieldDef)){
                            // Field from ancestor is also needed for current ID : take it.
                            nextId.computeIfAbsent(fieldDef, ancestor.id::get);
                        }
                    }
                }
            }

            nextId.put(lastField, newLastField.get());
            return Optional.of(Collections.unmodifiableMap(nextId));
        }

        return Optional.empty();
    }

    public Optional<Map<IdField, Object>> getNewId(ContextMetadata previousOfType, Deque<ContextMetadata> ancestorStack) {
        if(previousOfType != null) {
            // We have a previous element of this type : try creating a next ID from it.
            return getNext(previousOfType.id);
        }
        else {
            // Else, try creating a new first ID from its ancestors.
            return getFirst(ancestorStack);
        }
    }

    @SafeVarargs
    public final Map<IdField, Object> ofFields(Map.Entry<IdField, Object>... fieldEntries) {
        Map<IdField, Object> id = new HashMap<>();
        for(Map.Entry<IdField, Object> fieldEntry: fieldEntries) {
            if(Arrays.stream(fields).noneMatch(f -> f == fieldEntry.getKey())) {
                throw new IllegalArgumentException("Wrong fields : unexpected field " + fieldEntry + " for an ID of type " + this);
            }
            id.put(fieldEntry.getKey(), fieldEntry.getValue());
        }
        if(id.size() != fields.length) {
            throw new IllegalArgumentException("Wrong fields : received " + id + " for an ID of type " + this);
        }
        return Collections.unmodifiableMap(id);
    }
}
