package com.github.unaszole.bible.stream;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;

import java.util.ArrayList;
import java.util.List;

public class ContextEvent {

    public enum Type {OPEN, CLOSE};

    public static List<ContextEvent> fromContext(Context context) {
        List<ContextEvent> events = new ArrayList<>();

        events.add(new ContextEvent(Type.OPEN, context));
        for(Context child: context.children) {
            events.addAll(fromContext(child));
        }
        events.add(new ContextEvent(Type.CLOSE, context));

        return events;
    }

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
