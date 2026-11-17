package com.sync2pc.app.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Auflösung und Dauer für den Konflikt-Dialog (SYNC-20261115-367) */
class MediaProbeTest {
    private class Mem(private val data: ByteArray) : RangeSource {
        var reads = 0
        override val size: Long get() = data.size.toLong()
        override fun readAt(offset: Long, length: Int): ByteArray {
            reads++
            if (offset >= data.size) return ByteArray(0)
            val end = minOf(data.size.toLong(), offset + length).toInt()
            return data.copyOfRange(offset.toInt(), end)
        }
    }

    private fun be16(v: Int) = byteArrayOf((v shr 8).toByte(), v.toByte())

    private fun pngOf(w: Int, h: Int): ByteArray {
        val sig = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        val ihdr = byteArrayOf(0, 0, 0, 13, 'I'.code.toByte(), 'H'.code.toByte(), 'D'.code.toByte(), 'R'.code.toByte()) +
            byteArrayOf((w shr 24).toByte(), (w shr 16).toByte(), (w shr 8).toByte(), w.toByte()) +
            byteArrayOf((h shr 24).toByte(), (h shr 16).toByte(), (h shr 8).toByte(), h.toByte())
        return sig + ihdr + ByteArray(20)
    }

    /** JPEG mit optionalem EXIF-APP1 (Orientierung) vor dem SOF0-Block; [pad] Füllbytes in einem APP0-Block */
    private fun jpegOf(w: Int, h: Int, orientation: Int? = null, pad: Int = 0, little: Boolean = true): ByteArray {
        var out = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
        if (pad > 0) out += byteArrayOf(0xFF.toByte(), 0xE0.toByte()) + be16(pad + 2) + ByteArray(pad)
        if (orientation != null) {
            fun u16(v: Int) = if (little) byteArrayOf(v.toByte(), (v shr 8).toByte()) else be16(v)
            fun u32(v: Int) = if (little) byteArrayOf(v.toByte(), (v shr 8).toByte(), (v shr 16).toByte(), (v shr 24).toByte())
                else byteArrayOf((v shr 24).toByte(), (v shr 16).toByte(), (v shr 8).toByte(), v.toByte())
            val tiff = (if (little) "II".toByteArray() else "MM".toByteArray()) + u16(42) + u32(8) + u16(1) +
                u16(0x0112) + u16(3) + u32(1) + u16(orientation) + u16(0) + u32(0)
            val seg = "Exif".toByteArray() + byteArrayOf(0, 0) + tiff
            out += byteArrayOf(0xFF.toByte(), 0xE1.toByte()) + be16(seg.size + 2) + seg
        }
        out += byteArrayOf(0xFF.toByte(), 0xC0.toByte()) + be16(17) + byteArrayOf(8) + be16(h) + be16(w) + ByteArray(10)
        out += byteArrayOf(0xFF.toByte(), 0xDA.toByte(), 0, 2)
        return out
    }

    @Test fun pngDimensions() {
        assertEquals(MediaDims(4000, 3000), MediaProbe.png(Mem(pngOf(4000, 3000))))
        assertNull(MediaProbe.png(Mem(ByteArray(40))))
    }

    @Test fun jpegDimensionsWithoutExif() {
        assertEquals(MediaDims(4000, 3000), MediaProbe.jpeg(Mem(jpegOf(4000, 3000))))
    }

    @Test fun jpegPortraitOrientationSwapsDimensions() {
        // Orientierung 6 = 90°: das Sensorbild 4000×3000 zeigt sich als Hochformat 3000×4000 (Breite < Höhe)
        assertEquals(MediaDims(3000, 4000), MediaProbe.jpeg(Mem(jpegOf(4000, 3000, orientation = 6))))
        assertEquals(MediaDims(3000, 4000), MediaProbe.jpeg(Mem(jpegOf(4000, 3000, orientation = 8, little = false))))
        assertEquals(MediaDims(4000, 3000), MediaProbe.jpeg(Mem(jpegOf(4000, 3000, orientation = 1))))
        assertEquals(MediaDims(4000, 3000), MediaProbe.jpeg(Mem(jpegOf(4000, 3000, orientation = 3))))
    }

    @Test fun jpegWithLargeApp0StillFound() {
        assertEquals(MediaDims(640, 480), MediaProbe.jpeg(Mem(jpegOf(640, 480, pad = 30_000))))
    }

    @Test fun notAJpegIsUnknown() {
        assertNull(MediaProbe.jpeg(Mem(ByteArray(100))))
        assertNull(MediaProbe.jpeg(Mem(byteArrayOf(0xFF.toByte(), 0xD8.toByte()))))
    }

    @Test fun normalizeRotation() {
        assertEquals(1080 to 1920, MediaProbe.normalize(1920, 1080, 90))
        assertEquals(1080 to 1920, MediaProbe.normalize(1920, 1080, 270))
        assertEquals(1920 to 1080, MediaProbe.normalize(1920, 1080, 180))
        assertEquals(1920 to 1080, MediaProbe.normalize(1920, 1080, 0))
        assertEquals(1080 to 1920, MediaProbe.normalize(1920, 1080, -90))
    }

    @Test fun kindsByExtension() {
        assertEquals(MediaProbe.Kind.JPEG, MediaProbe.kindOf("a/b/IMG_1.JPG"))
        assertEquals(MediaProbe.Kind.HEIC, MediaProbe.kindOf("x.heic"))
        assertEquals(MediaProbe.Kind.VIDEO, MediaProbe.kindOf("clip.MOV"))
        assertEquals(MediaProbe.Kind.NONE, MediaProbe.kindOf("doc.pdf"))
        assertEquals(MediaProbe.Kind.NONE, MediaProbe.kindOf(".jpg"))
        assertFalse(MediaProbe.isMedia("readme"))
    }

    @Test fun budgetStopsAfterOneMiB() {
        val big = Mem(ByteArray(3 shl 20))
        val src = BudgetedSource(big, budgetBytes = 1L shl 20, block = 64 * 1024)
        var failed = false
        try {
            var off = 0L
            while (off < big.size) { src.readAt(off, 65536); off += 65536 }
        } catch (e: MediaBudgetException) { failed = true }
        assertTrue(failed)
        assertTrue(src.bytesRead <= 1L shl 20)
    }

    @Test fun deadlineStopsReading() {
        var t = 0L
        val src = BudgetedSource(Mem(ByteArray(1 shl 20)), deadlineMs = 5_000, now = { t })
        src.readAt(0, 10)
        t = 6_000
        var failed = false
        try { src.readAt(200_000, 10) } catch (e: MediaBudgetException) { failed = true }
        assertTrue(failed)
    }

    @Test fun cacheAvoidsRepeatedReads() {
        val mem = Mem(ByteArray(300_000))
        val src = BudgetedSource(mem)
        src.readAt(0, 100); src.readAt(50, 100); src.readAt(1000, 10)
        assertEquals(1, mem.reads)
    }

    @Test fun readAcrossBlockBoundaryAndEnd() {
        val data = ByteArray(100_000) { (it % 251).toByte() }
        val src = BudgetedSource(Mem(data), block = 4096)
        assertTrue(src.readAt(4090, 20).contentEquals(data.copyOfRange(4090, 4110)))
        assertEquals(10, src.readAt(99_990, 50).size)
        assertEquals(0, src.readAt(100_000, 10).size)
    }

    @Test fun mp4WithMoovAtEndIsReadableByJvmPartAsUnknown() {
        // Videos liest AndroidMediaProbe (MediaMetadataRetriever); der reine Teil kennt sie nur als VIDEO
        assertEquals(MediaProbe.Kind.VIDEO, MediaProbe.kindOf("a.mp4"))
    }

    @Test fun durationFormat() {
        assertEquals("0:42", MediaProbe.formatDuration(42_000))
        assertEquals("1:02:03", MediaProbe.formatDuration(3_723_000))
        assertEquals("0:00", MediaProbe.formatDuration(-5))
    }
}