package com.neomotion.app

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem

const val MIN_CLIP_MS = 100L

/** One piece of video on the timeline. inMs/outMs = the part of the source file that is used. */
data class Clip(
    val id: Long,
    val uri: Uri,
    val sourceDurationMs: Long,
    val inMs: Long,
    val outMs: Long
) {
    val durationMs: Long get() = outMs - inMs

    fun toMediaItem(): MediaItem {
        val end = if (outMs >= sourceDurationMs) C.TIME_END_OF_SOURCE else outMs
        return MediaItem.Builder()
            .setUri(uri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(inMs)
                    .setEndPositionMs(end)
                    .build()
            )
            .build()
    }
}

/** Reads the duration of a video and makes a full-length clip. Returns null if unreadable. */
fun readClip(context: Context, uri: Uri): Clip? {
    val r = MediaMetadataRetriever()
    return try {
        r.setDataSource(context, uri)
        val d = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        if (d == null || d <= 0L) null else Clip(TimelineOps.newId(), uri, d, 0L, d)
    } catch (e: Exception) {
        null
    } finally {
        try { r.release() } catch (e: Exception) { }
    }
}

/** Pure editing functions. Every edit returns a NEW list (or null when nothing can be done). */
object TimelineOps {
    private var nextId = 1L
    fun newId(): Long = nextId++

    fun totalDuration(clips: List<Clip>): Long = clips.sumOf { it.durationMs }

    fun clipStart(clips: List<Clip>, index: Int): Long {
        var acc = 0L
        for (i in 0 until index) acc += clips[i].durationMs
        return acc
    }

    /** Which clip is at this timeline position, and how far into that clip. */
    fun locate(clips: List<Clip>, posMs: Long): Pair<Int, Long>? {
        if (clips.isEmpty()) return null
        var acc = 0L
        for ((i, c) in clips.withIndex()) {
            if (posMs < acc + c.durationMs) return Pair(i, (posMs - acc).coerceAtLeast(0L))
            acc += c.durationMs
        }
        return Pair(clips.size - 1, clips.last().durationMs)
    }

    fun split(clips: List<Clip>, posMs: Long): List<Clip>? {
        val (i, off) = locate(clips, posMs) ?: return null
        val c = clips[i]
        if (off < MIN_CLIP_MS || off > c.durationMs - MIN_CLIP_MS) return null
        val a = c.copy(outMs = c.inMs + off)
        val b = c.copy(id = newId(), inMs = c.inMs + off)
        return clips.toMutableList().apply {
            removeAt(i)
            add(i, a)
            add(i + 1, b)
        }
    }

    /** Removes everything before the playhead from the selected clip. */
    fun trimStart(clips: List<Clip>, posMs: Long): List<Clip>? {
        val (i, off) = locate(clips, posMs) ?: return null
        val c = clips[i]
        if (off < MIN_CLIP_MS || off > c.durationMs - MIN_CLIP_MS) return null
        return clips.toMutableList().apply { set(i, c.copy(inMs = c.inMs + off)) }
    }

    /** Removes everything after the playhead from the selected clip. */
    fun trimEnd(clips: List<Clip>, posMs: Long): List<Clip>? {
        val (i, off) = locate(clips, posMs) ?: return null
        val c = clips[i]
        if (off < MIN_CLIP_MS || off > c.durationMs - MIN_CLIP_MS) return null
        return clips.toMutableList().apply { set(i, c.copy(outMs = c.inMs + off)) }
    }

    fun delete(clips: List<Clip>, posMs: Long): List<Clip>? {
        val (i, _) = locate(clips, posMs) ?: return null
        return clips.toMutableList().apply { removeAt(i) }
    }
}
