package com.sync2pc.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/** Nächster Schritt der Import-Checkliste (SYNC-20261115-353) */
class ImportChecklistTest {
    @Test fun lostFolderComesFirst() {
        assertEquals(ChecklistStep.FOLDER, nextChecklistStep(folderAccessLost = true, pcKnown = false))
        assertEquals(ChecklistStep.FOLDER, nextChecklistStep(folderAccessLost = true, pcKnown = true))
    }

    @Test fun unknownPcNeedsConnectionTest() {
        assertEquals(ChecklistStep.TEST, nextChecklistStep(folderAccessLost = false, pcKnown = false))
    }

    @Test fun otherwiseStartFirstSync() {
        assertEquals(ChecklistStep.SYNC, nextChecklistStep(folderAccessLost = false, pcKnown = true))
    }
}