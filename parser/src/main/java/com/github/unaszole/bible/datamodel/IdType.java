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

    public Optional<ContextId> getFirst(Deque<ContextMetadata> ancestorStack) {
        IdField lastField = fields[fields.length - 1];

        Optional newLastField = lastField.fieldType.getFirst();
        if(newLastField.isPresent()) {
            Map<IdField, Object> nextIdMap = new HashMap<>();
            // Loop through all ancestors to collect their ID fields.
            for(ContextMetadata ancestor: ancestorStack) {
                if(ancestor.type.idType.fields != null) {
                    for(IdField fieldDef: ancestor.type.idType.fields) {
                        if(Arrays.stream(fields).anyMatch(f -> f == fieldDef)){
                            // Field from ancestor is also needed for current ID : take it.
                            nextIdMap.computeIfAbsent(fieldDef, ancestor.id::get);
                        }
                    }
                }
            }

            nextIdMap.put(lastField, newLastField.get());
            return Optional.of(new ContextId(this, nextIdMap));
        }

        return Optional.empty();
    }

    public Optional<ContextId> getNewId(ContextMetadata previousOfType, Deque<ContextMetadata> ancestorStack) {
        if(previousOfType != null) {
            // We have a previous element of this type : try creating a next ID from it.
            return previousOfType.id.next();
        }
        else {
            // Else, try creating a new first ID from its ancestors.
            return getFirst(ancestorStack);
        }
    }

    @SafeVarargs
    public final ContextId ofFields(Map.Entry<IdField, Object>... fieldEntries) {
        Map<IdField, Object> idMap = new HashMap<>();
        for(Map.Entry<IdField, Object> fieldEntry: fieldEntries) {
            if(Arrays.stream(fields).noneMatch(f -> f == fieldEntry.getKey())) {
                throw new IllegalArgumentException("Wrong fields : unexpected field " + fieldEntry + " for an ID of type " + this);
            }
            idMap.put(fieldEntry.getKey(), fieldEntry.getValue());
        }
        if(idMap.size() != fields.length) {
            throw new IllegalArgumentException("Wrong fields : received " + idMap + " for an ID of type " + this);
        }
        return new ContextId(this, idMap);
    }
}
