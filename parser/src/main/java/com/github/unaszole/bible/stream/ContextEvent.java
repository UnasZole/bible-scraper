package com.github.unaszole.bible.stream;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;

public class ContextEvent {

    public enum Type {OPEN, CLOSE};

    public final Type type;
    public final ContextMetadata metadata;
    public final Object value;

    public ContextEvent(Type type, ContextMetadata metadata, Object value) {
        this.type = type;
        this.metadata = metadata;
        this.value = value;
    }

    public ContextEvent(Type type, Context context) {
        this(type, context.metadata, context.value);
    }

    @Override
    public String toString() {
        return "ContextEvent{" +
                "type=" + type +
                ", metadata=" + metadata +
                ", value='" + value + '\'' +
                '}';
    }
}
