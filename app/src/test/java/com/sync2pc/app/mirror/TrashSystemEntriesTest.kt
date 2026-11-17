package com.sync2pc.app.mirror

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrashSystemEntriesTest {
    @Test fun thumbnailsFolderIsHidden() {
        assertTrue(TrashSystemEntries.hidden("DCIM/.thumbnails/.nomedia"))
        assertTrue(TrashSystemEntries.hidden(".thumbnails/a.jpg"))
        assertTrue(TrashSystemEntries.hidden("a\\.Thumbnails\\b.jpg"))
    }

    @Test fun normalFilesAreShown() {
        assertFalse(TrashSystemEntries.hidden("DCIM/Camera/a.jpg"))
        assertFalse(TrashSystemEntries.hidden("DCIM/thumbnails/a.jpg"))
        assertFalse(TrashSystemEntries.hidden("a/x.thumbnails"))
    }
}