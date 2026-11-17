package com.sync2pc.app.media

import java.io.IOException

/**
 * Auflösung und Dauer von Bildern und Videos beider Seiten für den Konflikt-Dialog (SYNC-20261115-367, DEC-248).
 * Eine Lesefunktion für beide Seiten: Sie liest nur wenige Blöcke über eine [RangeSource] (Handy-Datei oder SMB).
 * Dieser Teil ist rein (JVM-getestet, MediaProbeTest); die Android-Teile (Video, HEIC, WebP) stehen in [AndroidMediaProbe].
 */

/** Quelle mit wahlfreiem Lesen; [readAt] liefert am Dateiende weniger Bytes als verlangt */
interface RangeSource {
    val size: Long
    fun readAt(offset: Long, length: Int): ByteArray
}

/** Das Lesebudget oder das Zeitlimit ist aufgebraucht: das Ergebnis ist «unbekannt» */
class MediaBudgetException(message: String) : IOException(message)

/** Masse nach Drehung ([width] × [height]), bei Video zusätzlich die Dauer. [durationMs] null = keine Dauer. */
data class MediaDims(val width: Int, val height: Int, val durationMs: Long? = null) {
    val pixels: Long get() = width.toLong() * height
}

/**
 * Begrenzt das Lesen einer Quelle: Blöcke zu [block] Bytes mit kleinem Zwischenspeicher, Gesamtbudget [budgetBytes] je Datei
 * (danach [MediaBudgetException]) und Zeitlimit [deadlineMs] (Standard 5 s).
 */
class BudgetedSource(
    private val inner: RangeSource,
    private val budgetBytes: Long = 1L shl 20,
    private val deadlineMs: Long = 5_000,
    private val block: Int = 64 * 1024,
    private val now: () -> Long = System::currentTimeMillis,
) : RangeSource {
    override val size: Long get() = inner.size
    private val start = now()
    private var used = 0L
    private val cache = LinkedHashMap<Long, ByteArray>()

    /** Bisher gelesene Bytes (für Tests) */
    val bytesRead: Long get() = used

    private fun blockAt(index: Long): ByteArray {
        cache[index]?.let { return it }
        if (now() - start > deadlineMs) throw MediaBudgetException("Zeitlimit")
        val off = index * block
        val len = minOf(block.toLong(), (size - off).coerceAtLeast(0)).toInt()
        if (len <= 0) return ByteArray(0)
        if (used + len > budgetBytes) throw MediaBudgetException("Lesebudget")
        val data = inner.readAt(off, len)
        used += data.size
        cache[index] = data
        if (cache.size > 4) cache.remove(cache.keys.first())
        return data
    }

    override fun readAt(offset: Long, length: Int): ByteArray {
        if (offset < 0 || length <= 0 || offset >= size) return ByteArray(0)
        val end = minOf(offset + length, size)
        val out = java.io.ByteArrayOutputStream((end - offset).toInt())
        var pos = offset
        while (pos < end) {
            val b = blockAt(pos / block)
            val inBlock = (pos - (pos / block) * block).toInt()
            if (inBlock >= b.size) break
            val take = minOf(b.size - inBlock, (end - pos).toInt())
            out.write(b, inBlock, take)
            pos += take
        }
        return out.toByteArray()
    }
}

object MediaProbe {
    private val JPEG = setOf("jpg", "jpeg")
    private val PNG = setOf("png")
    private val WEBP = setOf("webp")
    private val HEIC = setOf("heic", "heif")
    private val VIDEO = setOf("mp4", "mov", "3gp", "m4v", "webm")

    enum class Kind { JPEG, PNG, WEBP, HEIC, VIDEO, NONE }

    /** Nur diese Endungen werden gelesen (Bilder: JPEG, PNG, WebP, HEIC/HEIF; Videos: MP4, MOV, 3GP, M4V, WebM) */
    fun kindOf(name: String): Kind {
        val ext = name.substringAfterLast('/').substringAfterLast('\\').let { n ->
            if (n.lastIndexOf('.') <= 0) "" else n.substringAfterLast('.').lowercase()
        }
        return when (ext) {
            in JPEG -> Kind.JPEG
            in PNG -> Kind.PNG
            in WEBP -> Kind.WEBP
            in HEIC -> Kind.HEIC
            in VIDEO -> Kind.VIDEO
            else -> Kind.NONE
        }
    }

    fun isMedia(name: String): Boolean = kindOf(name) != Kind.NONE

    /** Masse nach Drehung: bei 90°/270° (bzw. EXIF-Orientierung 5 bis 8) vertauscht, Hochformat hat Breite < Höhe */
    fun normalize(width: Int, height: Int, rotationDegrees: Int): Pair<Int, Int> =
        if (((rotationDegrees % 360) + 360) % 360 in setOf(90, 270)) height to width else width to height

    /** EXIF-Orientierung (1 bis 8) in Grad im Uhrzeigersinn (nur die Drehung zählt für die Masse) */
    fun orientationToDegrees(orientation: Int): Int = when (orientation) {
        5, 8 -> 270
        6, 7 -> 90
        3, 4 -> 180
        else -> 0
    }

    /** PNG: Breite und Höhe stehen im IHDR-Block (Offset 16 und 20) */
    fun png(src: RangeSource): MediaDims? {
        val h = src.readAt(0, 24)
        if (h.size < 24) return null
        val sig = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        for (i in sig.indices) if (h[i] != sig[i]) return null
        val w = be32(h, 16); val ht = be32(h, 20)
        return if (w > 0 && ht > 0) MediaDims(w, ht) else null
    }

    /** JPEG: Abmessungen aus dem ersten SOF-Block, Drehung aus der EXIF-Orientierung (APP1) */
    fun jpeg(src: RangeSource): MediaDims? {
        val start = src.readAt(0, 2)
        if (start.size < 2 || start[0] != 0xFF.toByte() || start[1] != 0xD8.toByte()) return null
        var off = 2L
        var orientation = 1
        var w = 0; var h = 0
        var guard = 0
        while (off + 4 <= src.size && guard++ < 512) {
            val head = src.readAt(off, 4)
            if (head.size < 4 || head[0] != 0xFF.toByte()) return null
            val marker = head[1].toInt() and 0xFF
            if (marker == 0xFF) { off++; continue }
            if (marker == 0xD8 || marker == 0x01 || marker in 0xD0..0xD7) { off += 2; continue }
            if (marker == 0xD9 || marker == 0xDA) break
            val len = ((head[2].toInt() and 0xFF) shl 8) or (head[3].toInt() and 0xFF)
            if (len < 2) return null
            if (marker == 0xE1 && len > 8) orientation = exifOrientation(src.readAt(off + 4, minOf(len - 2, 4096))) ?: orientation
            if (marker in 0xC0..0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC) {
                val d = src.readAt(off + 4, 5)
                if (d.size < 5) return null
                h = ((d[1].toInt() and 0xFF) shl 8) or (d[2].toInt() and 0xFF)
                w = ((d[3].toInt() and 0xFF) shl 8) or (d[4].toInt() and 0xFF)
                break
            }
            off += 2 + len
        }
        if (w <= 0 || h <= 0) return null
        val (nw, nh) = normalize(w, h, orientationToDegrees(orientation))
        return MediaDims(nw, nh)
    }

    /** Orientierung (Tag 0x0112) aus einem EXIF-APP1-Segment ohne den Segmentkopf; null = nicht gefunden */
    fun exifOrientation(seg: ByteArray): Int? {
        // "Exif\0\0", danach der TIFF-Kopf
        if (seg.size < 14 || seg[0] != 'E'.code.toByte() || seg[1] != 'x'.code.toByte() || seg[2] != 'i'.code.toByte() || seg[3] != 'f'.code.toByte()) return null
        val t = 6
        val little = seg[t] == 'I'.code.toByte() && seg[t + 1] == 'I'.code.toByte()
        val big = seg[t] == 'M'.code.toByte() && seg[t + 1] == 'M'.code.toByte()
        if (!little && !big) return null
        fun u16(p: Int) = if (p + 2 > seg.size) -1 else if (little) (seg[p].toInt() and 0xFF) or ((seg[p + 1].toInt() and 0xFF) shl 8)
            else ((seg[p].toInt() and 0xFF) shl 8) or (seg[p + 1].toInt() and 0xFF)
        fun u32(p: Int) = if (p + 4 > seg.size) -1 else if (little) (seg[p].toInt() and 0xFF) or ((seg[p + 1].toInt() and 0xFF) shl 8) or
            ((seg[p + 2].toInt() and 0xFF) shl 16) or ((seg[p + 3].toInt() and 0xFF) shl 24)
            else ((seg[p].toInt() and 0xFF) shl 24) or ((seg[p + 1].toInt() and 0xFF) shl 16) or ((seg[p + 2].toInt() and 0xFF) shl 8) or (seg[p + 3].toInt() and 0xFF)
        val ifd = t + u32(t + 4)
        val count = u16(ifd)
        if (ifd < t || count <= 0) return null
        for (i in 0 until count.coerceAtMost(64)) {
            val e = ifd + 2 + i * 12
            if (e + 12 > seg.size) return null
            if (u16(e) == 0x0112) {
                val v = u16(e + 8)
                return if (v in 1..8) v else null
            }
        }
        return null
    }

    private fun be32(b: ByteArray, p: Int) =
        ((b[p].toInt() and 0xFF) shl 24) or ((b[p + 1].toInt() and 0xFF) shl 16) or ((b[p + 2].toInt() and 0xFF) shl 8) or (b[p + 3].toInt() and 0xFF)

    /** Dauer als «0:42» bzw. «1:02:03» */
    fun formatDuration(ms: Long): String {
        val total = (ms / 1000).coerceAtLeast(0)
        val h = total / 3600; val m = (total % 3600) / 60; val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }
}
