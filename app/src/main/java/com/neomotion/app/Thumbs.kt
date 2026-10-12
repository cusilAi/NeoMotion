package com.neomotion.app

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/** A row of small preview frames taken from one source video, one every [intervalMs]. */
class ThumbStrip(val intervalMs: Long, count: Int) {
    val frames: SnapshotStateList<ImageBitmap?> =
        mutableStateListOf<ImageBitmap?>().apply { repeat(count) { add(null) } }
    var done: Boolean = false
}

/** Decodes the frames of [strip] one by one in the background. Can be restarted safely. */
suspend fun fillThumbs(context: Context, uri: Uri, strip: ThumbStrip) {
    withContext(Dispatchers.IO) {
        val r = MediaMetadataRetriever()
        try {
            r.setDataSource(context, uri)
            for (i in 0 until strip.frames.size) {
                if (!isActive) return@withContext
                if (strip.frames[i] != null) continue
                val raw = r.getFrameAtTime(
                    i * strip.intervalMs * 1000L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                if (raw != null) {
                    val targetH = 96
                    val scale = targetH.toFloat() / raw.height
                    val w = maxOf(1, (raw.width * scale).toInt())
                    val small = Bitmap.createScaledBitmap(raw, w, targetH, true)
                    if (small !== raw) raw.recycle()
                    val img = small.asImageBitmap()
                    withContext(Dispatchers.Main) { strip.frames[i] = img }
                }
            }
            strip.done = true
        } catch (e: Exception) {
            strip.done = true
        } finally {
            try { r.release() } catch (e: Exception) { }
        }
    }
}
