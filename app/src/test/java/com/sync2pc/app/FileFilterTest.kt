package com.sync2pc.app

import com.sync2pc.app.mirror.FileFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileFilterTest {
    @Test fun emptyAcceptsAllButTemp() {
        val f = FileFilter("", "")
        assertTrue(f.accepts("a/b/c.pdf"))
        assertFalse(f.accepts("a/b/c.pdf.s2ptmp"))
        assertFalse(f.accepts("a/b/c.pdf.s2pold")) // alte Version beim Ersetzen (0.4.1)
        assertFalse(f.accepts(".LeoSync-Bin/2026-11-01/x.txt")) // 0.8.0 bis 0.16.x (Altname, wird von der Papierkorb-Übernahme umbenannt)
        assertFalse(f.accepts(".LeoSync-Papierkorb/2026-11-01/x.txt")) // 0.0.12 bis 0.7.x
        assertFalse(f.accepts(".Sync2PC-Papierkorb/2026-11-01/x.txt")) // alter Name bis 0.0.11
        assertFalse(f.accepts("@LeoSync-Bin/2026-11-01/x.txt")) // aktueller Papierkorb auf Handy und PC (SYNC-20261111-244)
    }

    // SYNC-20261115-419: SAF meldet für .trashed-<ts>-<name> den Anzeigenamen ohne Präfix; der echte Name steht in der Dokument-ID
    @Test fun trashedFileDetectedViaDocumentId() {
        val real = com.sync2pc.app.mirror.LocalTree.realName("primary:Download/QA400/.trashed-1700000000-b.jpg", "b.jpg")
        assertEquals(".trashed-1700000000-b.jpg", real)
        assertFalse(FileFilter("", "").accepts("sub/$real"))
        assertEquals("b.jpg", com.sync2pc.app.mirror.LocalTree.realName("primary:Download/QA400/b.jpg", "b.jpg"))
        assertEquals("Foto.jpg", com.sync2pc.app.mirror.LocalTree.realName("1234", "Foto.jpg")) // undurchsichtige ID
        assertEquals("x.jpg", com.sync2pc.app.mirror.LocalTree.realName(null, "x.jpg"))
    }

    // SYNC-20261115-399: Android-Systemordner und -dateien werden nie synchronisiert
    @Test fun thumbnailsFolderExcludedAtAnyDepth() {
        val f = FileFilter("", "")
        assertFalse(f.accepts(".thumbnails/x.jpg"))
        assertFalse(f.accepts("DCIM/.thumbnails/x.jpg"))
        assertFalse(f.accepts("DCIM/.Thumbnails/.nomedia"))
        assertFalse(f.accepts("a/.thumbnails/b/c.jpg"))
        assertTrue(f.accepts("my.thumbnails/x.jpg"))
        assertTrue(f.accepts("DCIM/.thumbnails")) // Datei mit diesem Namen, nichts darunter
        assertTrue(f.accepts(".nomedia"))
        assertTrue(f.accepts("DCIM/Camera/.nomedia"))
    }

    @Test fun whitelistCannotRevokeSystemExclusion() {
        val f = FileFilter("*", "")
        assertFalse(f.accepts("DCIM/.thumbnails/x.jpg"))
        assertFalse(f.accepts(".pending-1700000000-IMG_1.jpg"))
    }

    @Test fun pendingAndTrashedNamesExcludedAtAnyDepth() {
        val f = FileFilter("", "")
        assertFalse(f.accepts(".pending-1700000000-IMG_1.jpg"))
        assertFalse(f.accepts(".trashed-1700000000-IMG_1.jpg"))
        assertFalse(f.accepts("DCIM/Camera/.pending-1700000000-IMG_1.jpg"))
        assertFalse(f.accepts("DCIM/Camera/.TRASHED-1700000000-IMG_1.jpg"))
        assertTrue(f.accepts("IMG.pending-1.jpg"))
        assertTrue(f.accepts("my.trashed-x"))
        assertTrue(f.accepts("DCIM/Camera/IMG_1.jpg"))
    }

    @Test fun whitelist() {
        val f = FileFilter("*.pdf, *.DOCX", "")
        assertTrue(f.accepts("Ordner/Bericht.PDF"))
        assertTrue(f.accepts("brief.docx"))
        assertFalse(f.accepts("bild.jpg"))
    }

    @Test fun blacklistWins() {
        val f = FileFilter("*.pdf", "Temp/**\nentwurf*.pdf")
        assertTrue(f.accepts("Arbeit/bericht.pdf"))
        assertFalse(f.accepts("Temp/bericht.pdf"))
        assertFalse(f.accepts("Arbeit/Entwurf-1.pdf"))
    }

    @Test fun unbalancedBracketsAreReported() {
        assertEquals("[abc", FileFilter.unbalancedPattern("*.pdf, [abc"))
        assertEquals("x]", FileFilter.unbalancedPattern("x]"))
        assertEquals("{a", FileFilter.unbalancedPattern("{a"))
        assertNull(FileFilter.unbalancedPattern("foto[1].jpg, *.{a}"))
        assertNull(FileFilter.unbalancedPattern(""))
        assertNull(FileFilter.unbalancedPattern("*.pdf\nThumbs.db"))
    }

    @Test fun nameOnlyPatternMatchesInSubfolders() {
        val f = FileFilter("", "Thumbs.db")
        assertFalse(f.accepts("a/b/Thumbs.db"))
        assertTrue(f.accepts("a/b/thumbs.dbx"))
    }
}
