package com.github.unaszole.bible.scraping.generic.parsing.sliding;

import java.nio.CharBuffer;
import java.util.Iterator;

public class SlidingView implements Iterator<SlidingView> {

    private final CharBuffer buffer;
    private boolean hasConsumed = true;

    SlidingView(String str) {
        this.buffer = CharBuffer.wrap(str);
    }

    public CharSequence getRemaining() {
        return buffer.slice();
    }

    public void consume(int nbChars) {
        assert nbChars > 0 : "Can only consume a positive amount.";
        buffer.position(buffer.position() + nbChars);
        hasConsumed = true;
    }

    @Override
    public boolean hasNext() {
        return buffer.hasRemaining();
    }

    @Override
    public SlidingView next() {
        if (!hasConsumed) {
            // If we have not been advanced explicitly since the last item, advance by 1.
            consume(1);
        }

        // Reset the advance marker before returning at the new position.
        hasConsumed = false;
        return this;
    }
}
