package com.github.unaszole.bible.stream;

import com.github.unaszole.bible.datamodel.ContextMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ContextStreamEditor<StreamType extends ContextStream<StreamType>> {

    private final StreamType originalStream;
    private Stream<ContextEvent> stream;

    public ContextStreamEditor(StreamType originalStream) {
        this.originalStream = originalStream;
        this.stream = originalStream.getStream();
    }

    private enum TaskStatus {BeforeTarget, InTarget, AfterTarget, Complete}

    public ContextStreamEditor<StreamType> hide(final ContextMetadata metadata) {
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

    public ContextStreamEditor<StreamType> inject(final InjectionPosition pos,
                                      final ContextMetadata metadata, final List<? extends ContextStream<?>> contextStreams) {
        final TaskStatus[] status = { TaskStatus.BeforeTarget };

        this.stream = stream.flatMap(e -> {
            switch (status[0]) {
                case BeforeTarget:
                    if(e.type == pos.injectionEventType && Objects.equals(e.metadata, metadata)) {
                        List<Stream<ContextEvent>> streams = new ArrayList<>();
                        for(ContextStream<?> cs: contextStreams) {
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

    public ContextStreamEditor<StreamType> inject(final InjectionPosition pos,
                                      final ContextMetadata metadata, final ContextStream<?>... contextStreams) {
        return inject(pos, metadata, Arrays.asList(contextStreams));
    }

    public StreamType process() {
        return originalStream.build(originalStream.firstRoot, originalStream.lastRoot, stream);
    }
}
