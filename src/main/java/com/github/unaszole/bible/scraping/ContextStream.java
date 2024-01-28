package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class ContextStream {

    public static ContextStream fromSequence(Context rootContext, List<ContextStream> contextStreams) {
        List<Stream<ContextEvent>> streams = new ArrayList<>();

        for (final ContextStream contextStream : contextStreams) {
            if (contextStream.rootContext == rootContext) {
                // Stream contributes to the same root context directly.
                streams.add(contextStream.stream);
            } else {
                // Stream contributes to a child of the root.
                // We need to make sure to append the context to the root when we start streaming.
                // (hence why we add manually, and NOT with the getStream() public method).
                streams.add(Stream.of(new ContextEvent(ContextEvent.Type.OPEN, contextStream.rootContext)).peek(e -> {
                    rootContext.addChild(contextStream.rootContext);
                }));
                streams.add(contextStream.stream);
                streams.add(Stream.of(new ContextEvent(ContextEvent.Type.CLOSE, contextStream.rootContext)));
            }
        }

        return new ContextStream(rootContext, streams.stream().flatMap(s -> s));
    }

    public static Stream<ContextEvent> extract(Stream<ContextEvent> stream, ContextMetadata wantedContext) {
        final boolean[] closed = new boolean[] {false};
        return stream
                .dropWhile(e -> !(e.type == ContextEvent.Type.OPEN && Objects.equals(e.context.metadata, wantedContext)))
                .takeWhile(e -> {
                    if(closed[0]) {
                        return false;
                    }
                    closed[0] = e.type == ContextEvent.Type.CLOSE && Objects.equals(e.context.metadata, wantedContext);
                    return true;
                })
                //.peek(e -> System.out.println("Extracting context " + wantedContext + " : found event " + e))
                ;
    }

    public final Context rootContext;
    private final Stream<ContextEvent> stream;

    /**
     * @param rootContext The root context.
     * @param stream      The events contained within that context. (EXCLUDING the open/close of this context itself !)
     *                    Those will be added automatically.
     */
    public ContextStream(Context rootContext, Stream<ContextEvent> stream) {
        this.rootContext = rootContext;
        this.stream = stream;
    }

    /**
     * Intermediate operation, to edit a stream on the fly.
     * @param editorConstructor Constructor for a stream editor.
     * @return The stream dynamically modified by an editor.
     */
    public ContextStream edited(Function<ContextStream, ContextStreamEditor> editorConstructor) {
        return editorConstructor.apply(this).process();
    }

    /**
     * @return The stream of events for this context, including the OPEN and CLOSE events for this context.
     */
    public Stream<ContextEvent> getStream() {
        return Stream.of(
                Stream.of(new ContextEvent(ContextEvent.Type.OPEN, rootContext)),
                stream,
                Stream.of(new ContextEvent(ContextEvent.Type.CLOSE, rootContext))
        ).flatMap(s -> s);
    }

    /**
     * Terminal operation, to extract a populated context (or even the root context) from this stream.
     * @param wantedMetadata The wanted context, possibly the root itself.
     * @return The extracted context.
     */
    public Context extractContext(ContextMetadata wantedMetadata) {
        return getStream()
                .filter(e -> e.type == ContextEvent.Type.CLOSE && Objects.equals(e.context.metadata, wantedMetadata))
                .findAny()
                .map(e -> e.context)
                .orElseThrow(() -> new IllegalArgumentException("The requested context " + wantedMetadata + " wasn't found in the event stream for " + rootContext));
    }

    /**
     * Terminal operation, to extract the root context from this stream.
     * @return The root context.
     */
    public Context extractContext() {
        return extractContext(rootContext.metadata);
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
        if(Objects.equals(wantedMetadata, rootContext.metadata)) {
            return this;
        }

        // We need to consume the beginning of the stream until we reach the opening event for the wanted context.
        Iterator<ContextEvent> it = this.getStream().iterator();
        Context extractedContext = null;
        while(it.hasNext() && extractedContext == null) {
            ContextEvent e = it.next();
            if(e.type == ContextEvent.Type.OPEN && Objects.equals(e.context.metadata, wantedMetadata)) {
                extractedContext = e.context;
            }
        }

        // Now we need to rebuild a stream from what's left of the iterator, until the close event for the wanted context.
        Stream<ContextEvent> extractedStream = ParsingUtils.toStream(it).takeWhile(
                e -> !(e.type == ContextEvent.Type.CLOSE && Objects.equals(e.context.metadata, wantedMetadata))
        );

        return new ContextStream(extractedContext, extractedStream);
    }
}
