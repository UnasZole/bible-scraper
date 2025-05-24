package com.github.unaszole.bible.datamodel;

import com.github.unaszole.bible.parsing.Context;

import java.util.*;

public class IdType {
    public static final IdType NO_ID = new IdType(null);

    public final IdField<?>[] fields;

    public IdType(IdField<?>[] fields) {
        this.fields = fields;
    }

    public boolean isValid(Map<IdField<?>, ?> idMap) {
        for(IdField<?> field: fields) {
            if(!idMap.containsKey(field)) {
                // ID missing a required field.
                return false;
            }
            Object fieldValue = idMap.get(field);
            if(field.type.valueOf(fieldValue) != fieldValue) {
                // The field value is not yet in canonical form.
                return false;
            }
        }

        // All required fields are well defined in the map : ensure there is no additional unexpected fields.
        return idMap.size() == fields.length;
    }

    private <T> void withFieldFromAncestor(ContextId.Builder builder, Context ancestor, IdField<T> field) {
        if(!builder.has(field)) {
            builder.with(field, ancestor.metadata.id.get(field));
        }
    }

    private ContextId.Builder buildFromAncestors(List<Context> ancestorStack) {
        ContextId.Builder newIdBuilder = new ContextId.Builder(this);
        // Loop through all ancestors to collect their ID fields.
        for(Context ancestor: ancestorStack) {
            if(ancestor.metadata.type.idType().fields != null) {
                // If the ancestory has an ID, check all of its fields.
                for(IdField<?> fieldDef: ancestor.metadata.type.idType().fields) {
                    if(Arrays.stream(fields).anyMatch(f -> f == fieldDef)){
                        // Field from ancestor is also needed for current ID : take it.
                        withFieldFromAncestor(newIdBuilder, ancestor, fieldDef);
                    }
                }
            }
        }
        return newIdBuilder;
    }

    private <T> Optional<ContextId> withNewValueForField(IdField<T> field, List<Context> ancestorStack) {
        Optional<T> newLastField = field.type.first();
        if(newLastField.isPresent()) {
            ContextId.Builder newIdBuilder = buildFromAncestors(ancestorStack);
            newIdBuilder.with(field, newLastField.get());
            return Optional.of(newIdBuilder.build());
        }
        return Optional.empty();
    }

    public Optional<ContextId> getFirst(List<Context> ancestorStack) {
        IdField<?> lastField = fields[fields.length - 1];
        return withNewValueForField(lastField, ancestorStack);
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
    public final ContextId ofFields(Map.Entry<IdField<?>, ?>... fieldEntries) {
        Map<IdField<?>, Object> idMap = new HashMap<>();
        for(Map.Entry<IdField<?>, ?> fieldEntry: fieldEntries) {
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
