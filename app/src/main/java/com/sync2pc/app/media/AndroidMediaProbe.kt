package com.sync2pc.app.media

import android.graphics.BitmapFactory
import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import java.io.InputStream
import kotlin.coroutines.cancellation.CancellationException

/**
 * Android-Teil von [MediaProbe] (SYNC-20261115-367): Video und HEIC über [MediaMetadataRetriever] mit einer begrenzten
 * [MediaDataSource], WebP über [BitmapFactory] (nur Abmessungen). JPEG und PNG liest [MediaProbe] ohne Android-Klassen.
 * Jede Ausnahme ergibt «unbekannt» (null); ein Abbruch des Laufs ([CancellationException]) wird weitergereicht.
 */
object AndroidMediaProbe {
    /** Auflösung (und bei Video die Dauer) der Datei [name] über [src]; null = nicht lesbar oder keine Medien-Endung */
    fun read(name: String, src: RangeSource): MediaDims? = try {
        when (MediaProbe.kindOf(name)) {
            MediaProbe.Kind.NONE -> null
            MediaProbe.Kind.PNG -> MediaProbe.png(src)
            MediaProbe.Kind.JPEG -> MediaProbe.jpeg(src)
            MediaProbe.Kind.WEBP -> webp(src)
            MediaProbe.Kind.HEIC -> retriever(src, video = false)
            MediaProbe.Kind.VIDEO -> retriever(src, video = true)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private fun webp(src: RangeSource): MediaDims? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(asStream(src), null, opts)
        return if (opts.outWidth > 0 && opts.outHeight > 0) MediaDims(opts.outWidth, opts.outHeight) else null
    }

    private fun retriever(src: RangeSource, video: Boolean): MediaDims? {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(object : MediaDataSource() {
                override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
                    if (position >= src.size) return -1
                    val data = src.readAt(position, size)
                    if (data.isEmpty()) return -1
                    System.arraycopy(data, 0, buffer, offset, data.size)
                    return data.size
                }
                override fun getSize(): Long = src.size
                override fun close() {}
            })
            fun key(k: Int) = mmr.extractMetadata(k)?.toIntOrNull()
            val w = key(if (video) MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH else MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH)
            val h = key(if (video) MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT else MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT)
            val rot = key(if (video) MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION else MediaMetadataRetriever.METADATA_KEY_IMAGE_ROTATION) ?: 0
            if (w == null || h == null || w <= 0 || h <= 0) null
            else {
                val (nw, nh) = MediaProbe.normalize(w, h, rot)
                val dur = if (video) mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() else null
                MediaDims(nw, nh, dur)
            }
        } finally {
            try { mmr.release() } catch (ignored: Exception) {}
        }
    }

    /** Stream über die begrenzte Quelle (liest sequenziell ab Position 0) */
    private fun asStream(src: RangeSource): InputStream = object : InputStream() {
        private var pos = 0L
        override fun read(): Int {
            val b = src.readAt(pos, 1)
            if (b.isEmpty()) return -1
            pos++
            return b[0].toInt() and 0xFF
        }
        override fun read(buf: ByteArray, off: Int, len: Int): Int {
            val b = src.readAt(pos, len)
            if (b.isEmpty()) return -1
            System.arraycopy(b, 0, buf, off, b.size)
            pos += b.size
            return b.size
        }
    }
}
