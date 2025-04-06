package com.github.unaszole.bible.stream;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtils {
    public static <T> Iterator<T> toFlatIterator(Iterator<List<T>> listIt) {
        final Deque<T> buffer = new LinkedList<>();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                while(buffer.isEmpty() && listIt.hasNext()) {
                    buffer.addAll(listIt.next());
                }
                return !buffer.isEmpty();
            }

            @Override
            public T next() {
                return buffer.removeFirst();
            }
        };
    }

    public static <T> Stream<T> toStream(final Iterator<T> it) {
        // Else return a stream that iterates on it.
        return Stream.iterate((T)null, p -> {
            if(it.hasNext()) {
                return it.next();
            }
            return null;
        }).skip(1).takeWhile(Objects::nonNull);
    }

    private static class ChainedIterator<T> implements Iterator<T> {
        private final Queue<Iterator<T>> iterators;

        private ChainedIterator(List<Iterator<T>> iterators) {
            this.iterators = new LinkedList<>(iterators);
        }


        @Override
        public boolean hasNext() {
            while(!iterators.isEmpty()) {
                if(iterators.peek().hasNext()) {
                    return true;
                }
                iterators.remove();
            }
            return false;
        }

        @Override
        public T next() {
            assert !iterators.isEmpty();
            assert hasNext();
            return iterators.peek().next();
        }
    }

    public static <T> Iterator<T> concatIterators(List<Iterator<T>> iterators) {
        return new ChainedIterator<>(iterators);
    }

    public static <T> Stream<T> concatStreams(List<Stream<T>> streams) {
        return toStream(concatIterators(streams.stream()
                .map(BaseStream::iterator)
                .collect(Collectors.toList())
        ));
    }

    @SafeVarargs
    public static <T> Stream<T> concatStreams(Stream<T>... streams) {
        return concatStreams(Arrays.asList(streams));
    }

    public static <E> Iterator<E> deferredIterator(final Supplier<Iterator<E>> iteratorSupplier) {
        return new Iterator<>() {
            private Iterator<E> iterator = null;

            @Override
            public boolean hasNext() {
                if (iterator == null) {
                    iterator = iteratorSupplier.get();
                }
                return iterator.hasNext();
            }

            @Override
            public E next() {
                assert iterator != null;
                return iterator.next();
            }
        };
    }

    public static <E> Stream<E> deferredStream(final Supplier<Stream<E>> streamSupplier) {
        return toStream(deferredIterator(() -> streamSupplier.get().iterator()));
    }
}
