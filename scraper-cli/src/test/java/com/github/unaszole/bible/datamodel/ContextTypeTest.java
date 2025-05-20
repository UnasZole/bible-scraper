package com.github.unaszole.bible.datamodel;

import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.datamodel.contexttypes.StructureMarkers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextTypeTest {

    @Test
    public void testAllowedChildren() {
        assertFalse(
                BibleContainers.CHAPTER.childrenSpec().getAllowedTypesForNextChild(
                        List.of(StructureMarkers.STRUCTURED_TEXT)
                ).contains(BibleContainers.CHAPTER_TITLE),
                "Chapter title can't be contained after structured text."
        );
    }
}