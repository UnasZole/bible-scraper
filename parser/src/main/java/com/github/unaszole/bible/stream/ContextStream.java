package com.github.unaszole.bible.stream;

import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.parsing.Parser;

import java.util.*;
import java.util.stream.Stream;

import static com.github.unaszole.bible.parsing.ContextReaderListBuilder.context;

public abstract class ContextStream<StreamType extends ContextStream<StreamType>> {

    public static ContextStream.Single fromContents(Context rootContext, List<? extends ContextStream<?>> contextStreams) {
        List<Stream<ContextEvent>> streams = new ArrayList<>();

        streams.add(Stream.of(new ContextEvent(ContextEvent.Type.OPEN, rootContext)));
        for (final ContextStream<?> contextStream : contextStreams) {
            streams.add(contextStream.stream);
        }
        streams.add(Stream.of(new ContextEvent(ContextEvent.Type.CLOSE, rootContext)));

        return new ContextStream.Single(rootContext.metadata, StreamUtils.concatStreams(streams));
    }

    public final ContextMetadata firstRoot;
    public final ContextMetadata lastRoot;
    private final Stream<ContextEvent> stream;

    /**
     * @param firstRoot The first root context included within this stream.
     * @param lastRoot The last root context. Must be either the first context, or a following sibling.
     * @param stream The events contained within that context. (INCLUDING the open/close of all root contexts !)
     */
    public ContextStream(ContextMetadata firstRoot, ContextMetadata lastRoot, Stream<ContextEvent> stream) {
        this.firstRoot = firstRoot;
        this.lastRoot = lastRoot;
        this.stream = stream;
    }

    protected abstract StreamType getThis();
    protected abstract StreamType build(ContextMetadata firstRoot, ContextMetadata lastRoot, Stream<ContextEvent> stream);
    public ContextStreamEditor<StreamType> edit() {
        return new ContextStreamEditor<>(getThis());
    }

    /**
     * @return The stream of events for this context, including the OPEN and CLOSE events for this context.
     */
    public Stream<ContextEvent> getStream() {
        return stream;
    }

    /**
     * Terminal operation, to extract one single context (possibly one of the roots) from this stream.
     * @param wantedMetadata The wanted context, possibly one of the root.
     * @return The extracted context.
     */
    public Context extractContext(ContextMetadata wantedMetadata) {

        // Easy implementation : just use a parser on the event stream... after removing the open and close events.
        Iterator<ContextEvent> it = stream
                .dropWhile(e -> Objects.equals(e.metadata, wantedMetadata))
                .takeWhile(e -> !(Objects.equals(e.metadata, wantedMetadata)))
                .iterator();

        Context extractedContext = new Context(wantedMetadata);
        new Parser.TerminalParser<>(new PositionBufferedParserCore<>() {
            @Override
            protected List<ContextReader> readContexts(List<Context> ancestorStack, ContextType type,
                                                       ContextMetadata previousOfType, ContextEvent event) {
                if (event.type == ContextEvent.Type.OPEN && event.metadata.type == type) {
                    return context(event.metadata, event.value).build();
                }
                return List.of();
            }
        }, it, extractedContext).fill();

        return extractedContext;
    }

    protected Stream<ContextEvent> internalExtract(ContextMetadata firstWanted, ContextMetadata lastWanted) {
        // Initial check : if the wanted contexts are this stream's root contexts, we're good, return the same stream.
        if(Objects.equals(firstWanted, firstRoot) && Objects.equals(lastWanted, lastRoot)) {
            return this.stream;
        }

        // Else return a stream built from an interval within the current stream.
        final boolean[] closed = new boolean[] {false};
        return stream
                // Drop everything until the OPEN of the first wanted context.
                .dropWhile(e -> !(e.type == ContextEvent.Type.OPEN && Objects.equals(e.metadata, firstWanted)))
                // Drop everything after the CLOSE of the last wanted context
                .takeWhile(e -> {
                    if(closed[0]) {
                        return false;
                    }
                    closed[0] = e.type == ContextEvent.Type.CLOSE
                            && Objects.equals(e.metadata, lastWanted);
                    return true;
                })
                //.peek(e -> System.out.println("Extracting context " + wantedContext + " : found event " + e))
        ;
    }

    /**
     * Partially-terminal operation.
     * If wantedMetadata is not the same as the root context's, this consumes the current stream until reaching the
     * wanted element.
     * @param firstWanted The metadata of first element we wish to extract from this stream.
     * @param lastWanted The metadata of the last element to extract. Must be either identical to first, or a following sibling.
     * @return A new context stream for the wanted child context.
     */
    public ContextStream.Sequence extractStream(ContextMetadata firstWanted, ContextMetadata lastWanted) {
        return new Sequence(firstWanted, lastWanted, internalExtract(firstWanted, lastWanted));
    }

    public ContextStream.Single extractStream(ContextMetadata wanted) {
        return new Single(wanted, internalExtract(wanted, wanted));
    }

    public static class Single extends ContextStream<Single> {

        /**
         * @param rootContext The root context of that stream.
         * @param stream       The events contained within that context. (INCLUDING the open/close of this context itself !)
         */
        public Single(ContextMetadata rootContext, Stream<ContextEvent> stream) {
            super(rootContext, rootContext, stream);
        }

        @Override
        protected Single getThis() {
            return this;
        }

        @Override
        protected Single build(ContextMetadata firstRoot, ContextMetadata lastRoot, Stream<ContextEvent> stream) {
            return new Single(firstRoot, stream);
        }

        /**
         * Terminal operation, to extract the root context from this stream.
         * @return The root context.
         */
        public Context extractContext() {
            return extractContext(firstRoot);
        }
    }

    public static class Sequence extends ContextStream<Sequence> {

        /**
         * @param firstRoot The first root context included within this stream.
         * @param lastRoot  The last root context. Must be either the first context, or a following sibling.
         * @param stream    The events contained within that context. (INCLUDING the open/close of all root contexts !)
         */
        public Sequence(ContextMetadata firstRoot, ContextMetadata lastRoot, Stream<ContextEvent> stream) {
            super(firstRoot, lastRoot, stream);
        }

        @Override
        protected Sequence getThis() {
            return this;
        }

        @Override
        protected Sequence build(ContextMetadata firstRoot, ContextMetadata lastRoot, Stream<ContextEvent> stream) {
            return new Sequence(firstRoot, lastRoot, stream);
        }
    }
}
