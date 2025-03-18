package com.github.unaszole.bible.datamodel;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextTypeTest {

    @Test
    public void testAllowedChildren() {
        assertFalse(
                ContextType.CHAPTER.getAllowedTypesForNextChild(
                        List.of(ContextType.STRUCTURED_TEXT)
                ).contains(ContextType.CHAPTER_TITLE),
                "Chapter title can't be contained after structured text."
        );
    }
}