package com.github.unaszole.bible.scraping.generic.index;

import java.util.Iterator;

public class NotifyingIterator<T> implements Iterator<T> {

    private final Iterator<T> wrapped;
    private final Runnable startCallback;
    private boolean startNotified = false;
    private final Runnable endCallback;
    private boolean endNotified = false;

    public NotifyingIterator(Iterator<T> wrapped, Runnable startCallback, Runnable endCallback) {
        this.wrapped = wrapped;
        this.startCallback = startCallback;
        this.endCallback = endCallback;
    }

    @Override
    public boolean hasNext() {
        if (!startNotified) {
            startNotified = true;
            startCallback.run();
        }
        boolean res = wrapped.hasNext();
        if (!res && !endNotified) {
            endNotified = true;
            endCallback.run();
        }
        return res;
    }

    @Override
    public T next() {
        return wrapped.next();
    }
}
