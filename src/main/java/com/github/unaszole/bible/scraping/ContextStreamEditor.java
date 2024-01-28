package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContextStreamEditor {

    private final Context rootContext;
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
                    if(Objects.equals(e.context.metadata, metadata)) {
                        // We switch the status accordingly.
                        status[0] = e.type == ContextEvent.Type.OPEN ? TaskStatus.InTarget : TaskStatus.AfterTarget;
                        // Opening or closing events are always removed.
                        return false;
                    }
                    return status[0] != TaskStatus.InTarget;

                case AfterTarget:
                    // After the context, we search for its direct parent to remove the context.
                    if(e.context.children.removeIf(c -> Objects.equals(c.metadata, metadata))) {
                        // Context was removed from parent, switch the task to completed.
                        status[0] = TaskStatus.Complete;
                    }
                    return true;

                case Complete:
                default:
                    // Nothing left to filter.
                    return true;
            }
        });

        return this;
    }

    private static int getInsertionPositionIn(ContextMetadata metadata, ContextEvent e, boolean atStart) {
        if(e.type == (atStart ? ContextEvent.Type.OPEN : ContextEvent.Type.CLOSE)
                && Objects.equals(e.context.metadata, metadata)) {
            return atStart ? 0 : e.context.children.size();
        }
        return -1;
    }

    private static int getInsertionPositionInParentOf(ContextMetadata metadata, ContextEvent e, boolean before) {
        if(e.type == ContextEvent.Type.CLOSE) {
            int posInParent = ParsingUtils.indexOf(e.context.children, c -> Objects.equals(c.metadata, metadata));
            if(posInParent >= 0) {
                return posInParent + (before ? 0 : 1);
            }
        }
        return -1;
    }

    public enum InjectionPosition {
        BEFORE(ContextEvent.Type.OPEN, true,
                (m, e) -> getInsertionPositionInParentOf(m, e, true)
        ),
        AT_START(ContextEvent.Type.OPEN, false,
                (m, e) -> getInsertionPositionIn(m, e, true)
        ),
        AT_END(ContextEvent.Type.CLOSE, true,
                (m, e) -> getInsertionPositionIn(m, e, false)
        ),
        AFTER(ContextEvent.Type.CLOSE, false,
                (m, e) -> getInsertionPositionInParentOf(m, e, false)
        );

        /**
         * Whether we inject on opening or closing the given context.
         */
        public final ContextEvent.Type injectionEventType;
        /**
         * Whether we inject before (true) or after (false) the injection event.
         */
        public final boolean injectBeforeEvent;
        /**
         * Function that computes the insertion position of the injected contexts within the event's context.
         * Returns -1 if the given event is not the one where we can inject.
         */
        public final BiFunction<ContextMetadata, ContextEvent, Integer> getInsertionPosition;

        InjectionPosition(ContextEvent.Type injectionEventType,
                          boolean injectBeforeEvent,
                          BiFunction<ContextMetadata, ContextEvent, Integer> getInsertionPosition
        ) {
            this.injectionEventType = injectionEventType;
            this.injectBeforeEvent = injectBeforeEvent;
            this.getInsertionPosition = getInsertionPosition;
        }
    }

    private boolean tryToInsert(InjectionPosition pos, ContextMetadata metadata, ContextEvent e, ContextStream... contextStreams) {
        int insertionPosition = pos.getInsertionPosition.apply(metadata, e);
        if(insertionPosition >= 0) {
            e.context.children.addAll(insertionPosition, Arrays.stream(contextStreams).map(cs -> cs.rootContext).collect(Collectors.toList()));
            return true;
        }
        return false;
    }

    public ContextStreamEditor inject(final InjectionPosition pos,
                                      final ContextMetadata metadata, final ContextStream... contextStreams) {
        final TaskStatus[] status = { TaskStatus.BeforeTarget };

        this.stream = stream.flatMap(e -> {
            switch (status[0]) {
                case BeforeTarget:
                    if(e.type == pos.injectionEventType && Objects.equals(e.context.metadata, metadata)) {
                        List<Stream<ContextEvent>> streams = new ArrayList<>();
                        for(ContextStream cs: contextStreams) {
                            streams.add(cs.getStream());
                        }
                        streams.add(pos.injectBeforeEvent ? streams.size() : 0, Stream.of(e));

                        status[0] = tryToInsert(pos, metadata, e, contextStreams) ? TaskStatus.Complete : TaskStatus.AfterTarget;

                        return streams.stream().flatMap(s -> s);
                    }
                    else {
                        return Stream.of(e);
                    }

                case AfterTarget:
                    if(tryToInsert(pos, metadata, e, contextStreams)) {
                        status[0] = TaskStatus.Complete;
                    }
                    return Stream.of(e);

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
