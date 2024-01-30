package com.github.unaszole.bible.stream;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import org.crosswire.jsword.versification.BibleBook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
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
    private Stream<ContextEvent> stream;
    private final List<Action> actions = new ArrayList<>();

    public ContextStreamEditor(StreamType originalStream) {
        this.originalStream = originalStream;
        this.stream = originalStream.getStream();
    }

    public ContextStreamEditor<StreamType> replace(final ContextMetadata from, final ContextMetadata until,
                                                   final List<? extends ContextStream<?>> by) {
        // Start deleting all events from the OPEN of first element, included.
        actions.add(new Action(
                e -> e.type == ContextEvent.Type.OPEN && Objects.equals(e.metadata, from), true,
                e -> e.type == ContextEvent.Type.CLOSE && Objects.equals(e.metadata, until), false,
                e -> Stream.of()
        ));
        // Replace the CLOSE event of the last element by the given streams.
        actions.add(Action.singleEvent(
                e -> e.type == ContextEvent.Type.CLOSE && Objects.equals(e.metadata, until),
                e -> by.stream().flatMap(ContextStream::getStream)
        ));
        return this;
    }

    public ContextStreamEditor<StreamType> replace(ContextMetadata from, ContextMetadata until,
                                                   ContextStream<?>... by) {
        return replace(from, until, Arrays.asList(by));
    }

    public ContextStreamEditor<StreamType> remove( ContextMetadata from, ContextMetadata until) {
        return replace(from, until);
    }

    public ContextStreamEditor<StreamType> remove(final ContextMetadata elt) {
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

    public ContextStreamEditor<StreamType> inject(final InjectionPosition pos, final ContextMetadata metadata,
                                  final List<? extends ContextStream<?>> contextStreams) {
        actions.add(Action.singleEvent(
                        e -> e.type == pos.injectionEventType && Objects.equals(e.metadata, metadata),
                        e -> {
                            List<Stream<ContextEvent>> streams = new ArrayList<>();
                            for(ContextStream<?> cs: contextStreams) {
                                streams.add(cs.getStream());
                            }
                            streams.add(pos.injectBeforeEvent ? streams.size() : 0, Stream.of(e));

                            return streams.stream().flatMap(s-> s);
                        }
                )
        );
        return this;
    }

    public ContextStreamEditor<StreamType> inject(InjectionPosition pos,
                                  ContextMetadata metadata, ContextStream<?>... contextStreams) {
        return inject(pos, metadata, Arrays.asList(contextStreams));
    }

    public static class VersificationUpdater {
        private Function<ContextMetadata, BibleBook> bookUpdater = null;
        private ToIntFunction<ContextMetadata> chapterNbUpdater = null;
        private Function<ContextMetadata, String> chapterValueUpdater = null;
        private ToIntFunction<ContextMetadata> verseNbUpdater = null;
        private Function<ContextMetadata, String> verseValueUpdater = null;

        public VersificationUpdater book(Function<ContextMetadata, BibleBook> bookUpdater) {
            this.bookUpdater = bookUpdater;
            return this;
        }

        public VersificationUpdater chapterNb(ToIntFunction<ContextMetadata> chapterNbUpdater) {
            this.chapterNbUpdater = chapterNbUpdater;
            return this;
        }

        public VersificationUpdater chapterValue(Function<ContextMetadata, String> chapterValueUpdater) {
            this.chapterValueUpdater = chapterValueUpdater;
            return this;
        }

        public VersificationUpdater verseNb(ToIntFunction<ContextMetadata> verseNbUpdater) {
            this.verseNbUpdater = verseNbUpdater;
            return this;
        }

        public VersificationUpdater verseValue(Function<ContextMetadata, String> verseValueUpdater) {
            this.verseValueUpdater = verseValueUpdater;
            return this;
        }

        public ContextEvent apply(ContextEvent e) {
            BibleBook newBook = bookUpdater != null && e.metadata.book != null
                    ? bookUpdater.apply(e.metadata)
                    : e.metadata.book;
            int newChapterNb = chapterNbUpdater != null && e.metadata.chapter > 0
                    ? chapterNbUpdater.applyAsInt(e.metadata)
                    : e.metadata.chapter;
            int newVerseNb = verseNbUpdater != null && e.metadata.verse > 0
                    ? verseNbUpdater.applyAsInt(e.metadata)
                    : e.metadata.verse;

            String newValue = e.value;
            if(chapterValueUpdater != null && e.metadata.type == ContextType.CHAPTER) {
                newValue = chapterValueUpdater.apply(e.metadata);
            }
            else if(verseValueUpdater != null && e.metadata.type == ContextType.VERSE) {
                newValue = verseValueUpdater.apply(e.metadata);
            }

            return new ContextEvent(e.type,
                    new ContextMetadata(e.metadata.type, newBook, newChapterNb, newVerseNb),
                    newValue
            );
        }
    }

    public ContextStreamEditor<StreamType> updateVersification(final ContextMetadata from, final ContextMetadata until,
                                               final VersificationUpdater updater) {
        actions.add(new Action(
                e -> e.type == ContextEvent.Type.OPEN && Objects.equals(e.metadata, from),
                true,
                e -> e.type == ContextEvent.Type.CLOSE && Objects.equals(e.metadata, until),
                true,
                e -> Stream.of(updater.apply(e)))
        );
        return this;
    }

    public ContextStreamEditor<StreamType> updateVersificationUntilTheEnd(final VersificationUpdater updater) {
        actions.add(new Action(
                e -> true,
                true,
                e -> false,
                true,
                e -> Stream.of(updater.apply(e))
        ));
        return this;
    }

    private Stream<ContextEvent> applyToStream() {
        final int[] currentActionIndex = { -1 };
        final boolean[] currentActionComplete = { true };

        return stream.flatMap(e -> {
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
