package com.github.unaszole.bible.datamodel;

import com.github.unaszole.bible.scraping.ContextStreamEditor;
import com.github.unaszole.bible.scraping.Parser;

import java.util.*;
import java.util.stream.Stream;

public class ContextStream {

    public static ContextStream fromContents(Context rootContext, List<ContextStream> contextStreams) {
        List<Stream<ContextEvent>> streams = new ArrayList<>();

        streams.add(Stream.of(new ContextEvent(ContextEvent.Type.OPEN, rootContext)));
        for (final ContextStream contextStream : contextStreams) {
            streams.add(contextStream.stream);
        }
        streams.add(Stream.of(new ContextEvent(ContextEvent.Type.CLOSE, rootContext)));

        return new ContextStream(rootContext.metadata, streams.stream().flatMap(s -> s));
    }

    public static Stream<ContextEvent> extract(Stream<ContextEvent> stream, ContextMetadata wantedContext) {
        final boolean[] closed = new boolean[] {false};
        return stream
                .dropWhile(e -> !(e.type == ContextEvent.Type.OPEN && Objects.equals(e.metadata, wantedContext)))
                .takeWhile(e -> {
                    if(closed[0]) {
                        return false;
                    }
                    closed[0] = e.type == ContextEvent.Type.CLOSE && Objects.equals(e.metadata, wantedContext);
                    return true;
                })
                //.peek(e -> System.out.println("Extracting context " + wantedContext + " : found event " + e))
                ;
    }

    public final ContextMetadata rootContext;
    private final Stream<ContextEvent> stream;

    /**
     * @param rootContext The root context.
     * @param stream      The events contained within that context. (INCLUDING the open/close of this context itself !)
     */
    public ContextStream(ContextMetadata rootContext, Stream<ContextEvent> stream) {
        this.rootContext = rootContext;
        this.stream = stream;
    }

    /**
     * Intermediate operation, to edit a stream on the fly.
     * @return An editor on that stream. Call {@link ContextStreamEditor#process()} to get the resulting stream
     * dynamically edited.
     */
    public ContextStreamEditor edit() {
        return new ContextStreamEditor(this);
    }

    /**
     * @return The stream of events for this context, including the OPEN and CLOSE events for this context.
     */
    public Stream<ContextEvent> getStream() {
        return stream;
    }

    /**
     * Terminal operation, to extract a populated context (or even the root context) from this stream.
     * @param wantedMetadata The wanted context, possibly the root itself.
     * @return The extracted context.
     */
    public Context extractContext(ContextMetadata wantedMetadata) {

        // Easy implementation : just use a parser on the event stream... after removing the open and close events.
        Iterator<ContextEvent> it = stream
                .dropWhile(e -> Objects.equals(e.metadata, wantedMetadata))
                .takeWhile(e -> !(Objects.equals(e.metadata, wantedMetadata)))
                .iterator();

        Context extractedContext = new Context(wantedMetadata);
        new Parser.TerminalParser<ContextEvent>(it, extractedContext) {
            @Override
            protected Context readContext(Deque<ContextMetadata> ancestorStack, ContextType type, ContextEvent event) {
                if(event.type == ContextEvent.Type.OPEN && event.metadata.type == type) {
                    return new Context(event.metadata, event.value);
                }
                return null;
            }
        }.fill();

        return extractedContext;
    }

    /**
     * Terminal operation, to extract the root context from this stream.
     * @return The root context.
     */
    public Context extractContext() {
        return extractContext(rootContext);
    }

    /**
     * Partially-terminal operation.
     * If wantedMetadata is not the same as the root context's, this consumes the current stream until reaching the
     * wanted element.
     * @param wantedMetadata The metadata of the element we wish to extract from this stream.
     * @return A new context stream for the wanted child context.
     */
    public ContextStream extractStream(ContextMetadata wantedMetadata) {
        // Initial check : if the wanted context is this stream's context, we're good, nothing to process.
        if(Objects.equals(wantedMetadata, rootContext)) {
            return this;
        }

        return new ContextStream(wantedMetadata, extract(getStream(), wantedMetadata));
    }
}
