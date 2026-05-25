package com.github.unaszole.bible.scraping.generic.index;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.downloading.CachedDownloader;
import com.github.unaszole.bible.monitor.ExecutionMonitor;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.scraping.generic.ValueMapper;
import com.github.unaszole.bible.scraping.generic.data.PageData;
import com.github.unaszole.bible.scraping.generic.parsing.TextParser;
import com.github.unaszole.bible.stream.ContextStream;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @param page If this node is a leaf, this is the page to parse. Null otherwise.
 * @param childNodes If this node is internal, this is the list of child nodes. Null otherwise.
 * @param wrappingContext An optional context within which to wrap the contents of this node.
 */
public record IndexNode(@Nullable PageData page,
                        @Nullable List<IndexNode> childNodes,
                        @Nullable Context wrappingContext
) {
    public IndexNode {
        assert (page != null) ^ (childNodes != null) : "Index node must be either leaf or internal : " + this;
    }

    public IndexNode(@Nonnull PageData page, @Nullable Context wrappingContext) {
        this(page, null, wrappingContext);
    }

    public IndexNode(@Nonnull PageData page) {
        this(page, null);
    }

    public IndexNode(@Nonnull List<IndexNode> childNodes, @Nullable Context wrappingContext) {
        this(null, childNodes, wrappingContext);
    }

    public IndexNode(@Nonnull List<IndexNode> childNodes) {
        this(childNodes, null);
    }

    public @Nonnull Optional<IndexNode> find(@Nonnull ContextMetadata searchedMetadata) {
        // If this node provides exactly the searched context, return it.
        if(wrappingContext != null && wrappingContext.metadata.equals(searchedMetadata)) {
            return Optional.of(this);
        }

        // If this node has children, could any of them be what we are searching for ?
        if(childNodes != null) {
            boolean isAncestor = this.wrappingContext != null && this.wrappingContext.metadata.isAncestorOf(searchedMetadata);

            // If this has a context which is not the ancestor, stop the search here.
            if(this.wrappingContext != null && !isAncestor) {
                return Optional.empty();
            }

            // Else search descendants recursively.
            Optional<IndexNode> found = childNodes.stream()
                        .flatMap(n -> n.find(searchedMetadata).stream())
                        .findAny();

            if(found.isPresent()) {
                // Return the found result among descendants.
                return found;
            }
            else if(isAncestor) {
                // We couldn't find any result in its children : return it as the closest ancestor.
                return Optional.of(this);
            }
            return Optional.empty();
        }

        // No children : nothing else to search.
        return Optional.empty();
    }

    static final Map<IndexNode, ExecutionMonitor.Item> MONITOR_ITEMS = new ConcurrentHashMap<>();

    public Iterator<IndexNode> childrenIterator() {
        assert childNodes != null : "Iterator only available on nodes with children, not on " + this;

        if(wrappingContext != null) {
            final ExecutionMonitor.Item statusItem = MONITOR_ITEMS.computeIfAbsent(this, in -> ExecutionMonitor.INSTANCE.register(in.wrappingContext.metadata.toString()));
            return new NotifyingIterator<>(childNodes.iterator(), statusItem::start, statusItem::complete);
        }
        else {
            return childNodes.iterator();
        }
    }

    private void registerDescendants() {
        if(wrappingContext != null) {
            MONITOR_ITEMS.computeIfAbsent(this, in -> ExecutionMonitor.INSTANCE.register(in.wrappingContext.metadata.toString()));
        }
        if(childNodes != null) {
            for(IndexNode n: childNodes) {
                n.registerDescendants();
            }
        }
    }

    public ContextStream.Single stream(Map<String, TextParser> namedParsers, CachedDownloader cachedDownloader, ValueMapper mapper) {
        assert wrappingContext != null && childNodes != null : "Single context stream can only be created from container node with defined context, got " + this;

        // Register all descendant contexts on the execution monitor before parsing.
        registerDescendants();

        // Instantiate a terminal parser that traverses the index while notifying when it starts other descendant contexts.
        return new Parser.TerminalParser<>(
                new IndexedContentParser(namedParsers, cachedDownloader, mapper),
                childrenIterator(),
                wrappingContext
        ).asContextStream();
    }
}
