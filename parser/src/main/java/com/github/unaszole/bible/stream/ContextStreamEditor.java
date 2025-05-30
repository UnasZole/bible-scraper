package com.github.unaszole.bible.stream;

import com.github.unaszole.bible.datamodel.ContextId;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.IdField;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContextStreamEditor<StreamType extends ContextStream<StreamType>> {

    public static class Action {
        /**
         * Starting condition of this action.
         * If null, this action will start running from the first event it sees.
         */
        public final Predicate<ContextEvent> from;
        /**
         * If a from predicate is given, this determines if the substitution is applied to the event that matched.
         */
        public final boolean fromIncluded;
        /**
         * Ending condition of this action.
         * If null, this action will keep running until the end of the stream.
         */
        public final Predicate<ContextEvent> until;
        /**
         * If an until predicate is given, this determines if the substitution is applied to the event that matched.
         * If not, this closing event may immediately trigger the next action.
         */
        public final boolean untilIncluded;
        /**
         * The substitution to apply on events while this action is active.
         * If null, the events are forwarded unchanged.
         */
        public final Function<ContextEvent, Stream<ContextEvent>> substitution;

        public Action(Predicate<ContextEvent> from, boolean fromIncluded,
                      Predicate<ContextEvent> until, boolean untilIncluded,
                      Function<ContextEvent, Stream<ContextEvent>> substitution) {
            this.from = from;
            this.fromIncluded = fromIncluded;
            this.until = until;
            this.untilIncluded = untilIncluded;
            this.substitution = substitution;
        }

        public static Action singleEvent(Predicate<ContextEvent> at,
                                         Function<ContextEvent, Stream<ContextEvent>> substitution) {
            return new Action(at, true, at.negate(), false, substitution);
        }
    }

    private final StreamType originalStream;
    private final Stream<ContextEvent> stream;
    private final List<Action> actions = new ArrayList<>();

    public ContextStreamEditor(StreamType originalStream) {
        this.originalStream = originalStream;
        this.stream = originalStream.getStream();
    }

    public ContextStreamEditor<StreamType> replace(final BiPredicate<ContextMetadata, Object> from,
                                                   final BiPredicate<ContextMetadata, Object> until,
                                                   final List<? extends ContextStream<?>> by) {
        // Start deleting all events from the OPEN of first element, included.
        actions.add(new Action(
                e -> e.type == ContextEvent.Type.OPEN && from.test(e.metadata, e.value), true,
                e -> e.type == ContextEvent.Type.CLOSE && until.test(e.metadata, e.value), false,
                e -> Stream.of()
        ));
        // Replace the CLOSE event of the last element by the given streams.
        actions.add(Action.singleEvent(
                e -> e.type == ContextEvent.Type.CLOSE && until.test(e.metadata, e.value),
                e -> StreamUtils.concatStreams(by.stream().map(ContextStream::getStream).collect(Collectors.toList()))
        ));
        return this;
    }

    public ContextStreamEditor<StreamType> replace(BiPredicate<ContextMetadata, Object> from,
                                                   BiPredicate<ContextMetadata, Object> until,
                                                   ContextStream<?>... by) {
        return replace(from, until, Arrays.asList(by));
    }

    public ContextStreamEditor<StreamType> remove(BiPredicate<ContextMetadata, Object> from,
                                                  BiPredicate<ContextMetadata, Object> until) {
        return replace(from, until);
    }

    public ContextStreamEditor<StreamType> remove(final ContextMetadata from, final ContextMetadata until) {
        return remove(
                (m, v) -> Objects.equals(m, from),
                (m, v) -> Objects.equals(m, until)
        );
    }

    public ContextStreamEditor<StreamType> remove(BiPredicate<ContextMetadata, Object> elt) {
        return remove(elt, elt);
    }

    public ContextStreamEditor<StreamType> remove(ContextMetadata elt) {
        return remove(elt, elt);
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
                                                  final BiPredicate<ContextMetadata, Object> target,
                                                  final List<? extends ContextStream<?>> contextStreams) {
        actions.add(Action.singleEvent(
                        e -> e.type == pos.injectionEventType && target.test(e.metadata, e.value),
                        e -> {
                            List<Stream<ContextEvent>> streams = new ArrayList<>();
                            for(ContextStream<?> cs: contextStreams) {
                                streams.add(cs.getStream());
                            }
                            streams.add(pos.injectBeforeEvent ? streams.size() : 0, Stream.of(e));

                            return StreamUtils.concatStreams(streams);
                        }
                )
        );
        return this;
    }

    public ContextStreamEditor<StreamType> inject(InjectionPosition pos, final ContextMetadata target,
                                                  List<? extends ContextStream<?>> contextStreams) {
        return inject(pos, (m, v) -> Objects.equals(m, target), contextStreams);
    }

    public ContextStreamEditor<StreamType> inject(InjectionPosition pos, BiPredicate<ContextMetadata, Object> target,
                                                  ContextStream<?>... contextStreams) {
        return inject(pos, target, Arrays.asList(contextStreams));
    }

    public ContextStreamEditor<StreamType> inject(InjectionPosition pos, ContextMetadata target,
                                                  ContextStream<?>... contextStreams) {
        return inject(pos, target, Arrays.asList(contextStreams));
    }

    public ContextStreamEditor<StreamType> doNothingUntil(InjectionPosition pos,
                                                          BiPredicate<ContextMetadata, Object> target) {
        return inject(pos, target);
    }

    public ContextStreamEditor<StreamType> doNothingUntil(InjectionPosition pos, ContextMetadata target) {
        return inject(pos, target);
    }

    public static class ContextUpdater {
        private final Map<IdField<?>, BiFunction<ContextMetadata, Object, ? extends Optional<?>>> idFieldUpdaters = new HashMap<>();
        private BiFunction<ContextMetadata, Object, Optional<?>> valueUpdater = null;

        public <T> ContextUpdater idField(IdField<T> idField, BiFunction<ContextMetadata, Object, Optional<T>> updater) {
            idFieldUpdaters.put(idField, updater);
            return this;
        }

        public ContextUpdater value(BiFunction<ContextMetadata, Object, Optional<?>> valueUpdater) {
            this.valueUpdater = valueUpdater;
            return this;
        }

        private <T> Optional<BiFunction<ContextMetadata, Object, Optional<T>>> getUpdater(IdField<T> field) {
            return Optional.ofNullable((BiFunction<ContextMetadata, Object, Optional<T>>) idFieldUpdaters.get(field));
        }

        private <T> void updateField(ContextId.Builder builder, ContextEvent e, IdField<T> field) {
            getUpdater(field)
                    .flatMap(u -> u.apply(e.metadata, e.value))
                    .ifPresent(nfv -> builder.with(field, nfv));
        }

        public ContextEvent apply(ContextEvent e) {
            ContextId newId = e.metadata.id;
            if(newId != null) {
                ContextId.Builder newIdBuilder = new ContextId.Builder(newId);
                // Loop through all ID fields to check for an updater.
                for (IdField<?> field : e.metadata.type.idType().fields) {
                    updateField(newIdBuilder, e, field);
                }
                newId = newIdBuilder.build();
            }

            // If we have a value updater, and it returns a new value, use it.
            Object newValue = Optional.ofNullable(valueUpdater)
                    .flatMap(u -> (Optional<Object>) u.apply(e.metadata, e.value))
                    .orElse(e.value);

            return new ContextEvent(e.type,
                    new ContextMetadata(e.metadata.type, newId),
                    newValue
            );
        }
    }

    public ContextStreamEditor<StreamType> updateContexts(final ContextMetadata from, final ContextMetadata until,
                                                          final ContextUpdater updater) {
        actions.add(new Action(
                e -> e.type == ContextEvent.Type.OPEN && Objects.equals(e.metadata, from),
                true,
                e -> e.type == ContextEvent.Type.CLOSE && Objects.equals(e.metadata, until),
                true,
                e -> Stream.of(updater.apply(e)))
        );
        return this;
    }

    public ContextStreamEditor<StreamType> updateContextsUntilTheEnd(final ContextUpdater updater) {
        actions.add(new Action(
                e -> true,
                true,
                e -> false,
                true,
                e -> Stream.of(updater.apply(e))
        ));
        return this;
    }

    public ContextStreamEditor<StreamType> mergeSiblings(final ContextMetadata firstMeta, final ContextMetadata secondMeta) {
        final ContextEvent[] fistItemClosing = { null };
        // When encountering the first item's close event, save it and remove it from the stream.
        actions.add(Action.singleEvent(
                e -> e.type == ContextEvent.Type.CLOSE && Objects.equals(e.metadata, firstMeta),
                e -> {
                    fistItemClosing[0] = e;
                    return Stream.of();
                }
        ));
        // When encountering the second item's open event, remove it from the stream.
        actions.add(Action.singleEvent(
                e -> e.type == ContextEvent.Type.OPEN && Objects.equals(e.metadata, secondMeta),
                e -> Stream.of()
        ));
        // When encountering the second item's close event, replace it by the first.
        actions.add(Action.singleEvent(
                e -> e.type == ContextEvent.Type.CLOSE && Objects.equals(e.metadata, secondMeta),
                e -> Stream.of(fistItemClosing[0])
        ));
        return this;
    }

    private Stream<ContextEvent> applyToStream() {
        final int[] currentActionIndex = { -1 };
        final boolean[] currentActionComplete = { true };

        return StreamUtils.lazyFlatMap(stream, e -> {
            if(currentActionIndex[0] >= 0 && !currentActionComplete[0]) {
                // There is a current action which is not yet complete.
                Action currentAction = actions.get(currentActionIndex[0]);

                // Check if it terminates.
                if(currentAction.until != null && currentAction.until.test(e)) {
                    // The current action terminates now...
                    currentActionComplete[0] = true;
                    if(currentAction.untilIncluded) {
                        // ... but after this event. Process the substitution.
                        return currentAction.substitution.apply(e);
                    }
                }
                else {
                    // Process the substitution normally.
                    return currentAction.substitution.apply(e);
                }
            }

            // Check if a next action is ready to start.
            if(currentActionIndex[0] < actions.size() - 1) {
                Action nextAction = actions.get(currentActionIndex[0] + 1);

                if(nextAction.from == null || nextAction.from.test(e)) {
                    // The next action starts now...
                    currentActionIndex[0]++;
                    currentActionComplete[0] = false;
                    if(nextAction.fromIncluded) {
                        // ... including this event. Perform the substitution.
                        return nextAction.substitution.apply(e);
                    }
                }
            }

            // Else, no action took place, return the event unmodified.
            return Stream.of(e);
        });
    }

    public StreamType process() {
        return originalStream.build(originalStream.firstRoot, originalStream.lastRoot, applyToStream());
    }
}
