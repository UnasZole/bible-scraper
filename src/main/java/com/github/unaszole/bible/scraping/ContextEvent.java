package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;

import java.util.ArrayList;
import java.util.List;

public class ContextEvent {

    /**
     * OPEN means the given context was just opened. Only its metadata and direct content is relevant, all its children are missing.
     * CLOSE means the given context is complete : all of its children are included.
     */
    public enum Type {OPEN, CLOSE};

    public static List<ContextEvent> fromContext(Context context) {
        List<ContextEvent> events = new ArrayList<>();

        events.add(new ContextEvent(Type.OPEN, context));
        for(Context child: context.getChildren()) {
            events.addAll(fromContext(child));
        }
        events.add(new ContextEvent(Type.CLOSE, context));

        return events;
    }

    public final Type type;
    public final Context context;

    public ContextEvent(Type type, Context context) {
        this.type = type;
        this.context = context;
    }
}
