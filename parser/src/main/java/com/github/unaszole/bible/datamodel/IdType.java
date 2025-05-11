package com.github.unaszole.bible.datamodel;

import com.github.unaszole.bible.parsing.Context;

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

    public Optional<ContextId> getFirst(List<Context> ancestorStack) {
        IdField lastField = fields[fields.length - 1];

        Optional newLastField = lastField.fieldType.getFirst();
        if(newLastField.isPresent()) {
            Map<IdField, Object> nextIdMap = new HashMap<>();
            // Loop through all ancestors to collect their ID fields.
            for(Context ancestor: ancestorStack) {
                if(ancestor.metadata.type.idType.fields != null) {
                    for(IdField fieldDef: ancestor.metadata.type.idType.fields) {
                        if(Arrays.stream(fields).anyMatch(f -> f == fieldDef)){
                            // Field from ancestor is also needed for current ID : take it.
                            nextIdMap.computeIfAbsent(fieldDef, ancestor.metadata.id::get);
                        }
                    }
                }
            }

            nextIdMap.put(lastField, newLastField.get());
            return Optional.of(new ContextId(this, nextIdMap));
        }

        return Optional.empty();
    }

    public Optional<ContextId> getNewId(ContextMetadata previousOfType, List<Context> ancestorStack) {
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
