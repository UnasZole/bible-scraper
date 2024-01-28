package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.ContextEvent;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ContextStreamEditor {

    private final ContextMetadata rootContext;
    private Stream<ContextEvent> stream;

    public ContextStreamEditor(ContextStream originalStream) {
        this.rootContext = originalStream.rootContext;
        this.stream = originalStream.getStream();
    }

    private enum TaskStatus {BeforeTarget, InTarget, AfterTarget, Complete}

    public ContextStreamEditor hide(final ContextMetadata metadata) {
        final TaskStatus[] status = { TaskStatus.BeforeTarget };

        this.stream = stream.filter(e -> {
            switch(status[0]) {
                case BeforeTarget:
                case InTarget:
                    // Before or within the context, we keep searching for the context opening or closing events.
                    if(Objects.equals(e.metadata, metadata)) {
                        // We switch the status accordingly.
                        status[0] = e.type == ContextEvent.Type.OPEN ? TaskStatus.InTarget : TaskStatus.Complete;
                        // Opening or closing events are always removed.
                        return false;
                    }
                    return status[0] != TaskStatus.InTarget;

                case Complete:
                default:
                    // Nothing left to filter.
                    return true;
            }
        });

        return this;
    }

    public enum InjectionPosition {
        BEFORE(ContextEvent.Type.OPEN, true),
        AT_START(ContextEvent.Type.OPEN, false),
        AT_END(ContextEvent.Type.CLOSE, true),
        AFTER(ContextEvent.Type.CLOSE, false);

        /**
         * Whether we inject on opening or closing the given context.
         */
        public final ContextEvent.Type injectionEventType;
        /**
         * Whether we inject before (true) or after (false) the injection event.
         */
        public final boolean injectBeforeEvent;

        InjectionPosition(ContextEvent.Type injectionEventType,
                          boolean injectBeforeEvent) {
            this.injectionEventType = injectionEventType;
            this.injectBeforeEvent = injectBeforeEvent;
        }
    }

    public ContextStreamEditor inject(final InjectionPosition pos,
                                      final ContextMetadata metadata, final ContextStream... contextStreams) {
        final TaskStatus[] status = { TaskStatus.BeforeTarget };

        this.stream = stream.flatMap(e -> {
            switch (status[0]) {
                case BeforeTarget:
                    if(e.type == pos.injectionEventType && Objects.equals(e.metadata, metadata)) {
                        List<Stream<ContextEvent>> streams = new ArrayList<>();
                        for(ContextStream cs: contextStreams) {
                            streams.add(cs.getStream());
                        }
                        streams.add(pos.injectBeforeEvent ? streams.size() : 0, Stream.of(e));

                        status[0] = TaskStatus.Complete;

                        return streams.stream().flatMap(s -> s);
                    }
                    else {
                        return Stream.of(e);
                    }

                case Complete:
                default:
                    return Stream.of(e);
            }
        });

        return this;
    }

    public ContextStream process() {
        return new ContextStream(rootContext, stream);
    }
}
