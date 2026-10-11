package com.neomotion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NeoMotionApp() }
    }
}

private val clipColors = listOf(
    Color(0xFF6650A4), Color(0xFF3F6FB5), Color(0xFF2E8B7A), Color(0xFFB5683F)
)

private fun fmt(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d.%d".format(s / 60, s % 60, (ms % 1000) / 100)
}

@Composable
private fun RowScope.ToolButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) { Text(label, fontSize = 13.sp, maxLines = 1) }
}

@Composable
fun NeoMotionApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var clips by remember { mutableStateOf(emptyList<Clip>()) }
    var playheadMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    val undoStack = remember { mutableStateListOf<List<Clip>>() }
    val redoStack = remember { mutableStateListOf<List<Clip>>() }

    val exo = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(exo) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    playheadMs = TimelineOps.totalDuration(clips)
                }
            }
        }
        exo.addListener(listener)
        onDispose {
            exo.removeListener(listener)
            exo.release()
        }
    }

    // While playing, move the playhead along with the video
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val idx = exo.currentMediaItemIndex
            if (idx in clips.indices) {
                playheadMs = TimelineOps.clipStart(clips, idx) + exo.currentPosition.coerceAtLeast(0L)
            }
            delay(16)
        }
    }

    // ---------- helper functions ----------

    fun applyToPlayer(list: List<Clip>, posMs: Long) {
        exo.pause()
        if (list.isEmpty()) {
            exo.clearMediaItems()
            return
        }
        val loc = TimelineOps.locate(list, posMs) ?: return
        val off = minOf(loc.second, maxOf(0L, list[loc.first].durationMs - 1))
        exo.setMediaItems(list.map { it.toMediaItem() }, loc.first, off)
        exo.prepare()
    }

    fun seek(posMs: Long) {
        val t = TimelineOps.totalDuration(clips)
        val p = posMs.coerceIn(0L, t)
        playheadMs = p
        val loc = TimelineOps.locate(clips, p) ?: return
        val off = minOf(loc.second, maxOf(0L, clips[loc.first].durationMs - 1))
        exo.seekTo(loc.first, off)
    }

    fun seekFromX(x: Float, widthPx: Int) {
        val t = TimelineOps.totalDuration(clips)
        if (t <= 0L || widthPx <= 0) return
        seek((x.coerceIn(0f, widthPx.toFloat()) / widthPx * t).toLong())
    }

    fun commit(newClips: List<Clip>, newPos: Long) {
        undoStack.add(clips)
        redoStack.clear()
        clips = newClips
        val pos = newPos.coerceIn(0L, TimelineOps.totalDuration(newClips))
        playheadMs = pos
        applyToPlayer(newClips, pos)
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(clips)
        val prev = undoStack.removeAt(undoStack.lastIndex)
        clips = prev
        val pos = playheadMs.coerceIn(0L, TimelineOps.totalDuration(prev))
        playheadMs = pos
        applyToPlayer(prev, pos)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.add(clips)
        val next = redoStack.removeAt(redoStack.lastIndex)
        clips = next
        val pos = playheadMs.coerceIn(0L, TimelineOps.totalDuration(next))
        playheadMs = pos
        applyToPlayer(next, pos)
    }

    fun togglePlay() {
        val t = TimelineOps.totalDuration(clips)
        if (t <= 0L) return
        if (exo.isPlaying) {
            exo.pause()
        } else {
            if (playheadMs >= t - 50) seek(0L)
            exo.play()
        }
    }

    fun doSplit() {
        val updated = TimelineOps.split(clips, playheadMs) ?: return
        commit(updated, playheadMs)
    }

    fun doTrimStart() {
        val loc = TimelineOps.locate(clips, playheadMs) ?: return
        val updated = TimelineOps.trimStart(clips, playheadMs) ?: return
        commit(updated, TimelineOps.clipStart(updated, loc.first))
    }

    fun doTrimEnd() {
        val updated = TimelineOps.trimEnd(clips, playheadMs) ?: return
        commit(updated, playheadMs - 1)
    }

    fun doDelete() {
        val updated = TimelineOps.delete(clips, playheadMs) ?: return
        commit(updated, playheadMs)
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val added = withContext(Dispatchers.IO) {
                    uris.mapNotNull { readClip(context, it) }
                }
                if (added.isNotEmpty()) commit(clips + added, playheadMs)
            }
        }
    }

    // ---------- UI ----------

    val total = TimelineOps.totalDuration(clips)
    val selectedIdx = TimelineOps.locate(clips, playheadMs)?.first

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D12))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("NeoMotion", color = Color.White, fontSize = 24.sp)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { undo() }, enabled = undoStack.isNotEmpty()) { Text("Undo") }
            TextButton(onClick = { redo() }, enabled = redoStack.isNotEmpty()) { Text("Redo") }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = exo
                        useController = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            if (clips.isEmpty()) {
                Text("Import videos to start", color = Color.Gray)
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { togglePlay() }, enabled = clips.isNotEmpty()) {
                Text(if (isPlaying) "Pause" else "Play")
            }
            Spacer(Modifier.width(12.dp))
            Text(fmt(playheadMs) + " / " + fmt(total), color = Color.White, fontSize = 15.sp)
        }

        Spacer(Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .background(Color(0xFF17171F))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { off ->
                        exo.pause()
                        seekFromX(off.x, size.width)
                    })
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { off ->
                            exo.pause()
                            exo.setSeekParameters(SeekParameters.CLOSEST_SYNC)
                            seekFromX(off.x, size.width)
                        },
                        onDragEnd = {
                            exo.setSeekParameters(SeekParameters.EXACT)
                            seek(playheadMs)
                        },
                        onDragCancel = {
                            exo.setSeekParameters(SeekParameters.EXACT)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            seekFromX(change.position.x, size.width)
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            if (total > 0L) {
                var x = 0f
                clips.forEachIndexed { i, c ->
                    val cw = c.durationMs.toFloat() / total * w
                    val selected = i == selectedIdx
                    val col = clipColors[i % clipColors.size]
                    val topLeft = Offset(x + 2f, 8f)
                    val boxSize = Size(maxOf(cw - 4f, 1f), h - 16f)
                    drawRoundRect(
                        color = col.copy(alpha = if (selected) 1f else 0.55f),
                        topLeft = topLeft,
                        size = boxSize,
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    if (selected) {
                        drawRoundRect(
                            color = Color.White,
                            topLeft = topLeft,
                            size = boxSize,
                            cornerRadius = CornerRadius(12f, 12f),
                            style = Stroke(width = 3f)
                        )
                    }
                    x += cw
                }
                val px = playheadMs.toFloat() / total * w
                drawLine(Color.White, Offset(px, 0f), Offset(px, h), strokeWidth = 4f)
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolButton("Import") {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            }
            ToolButton("Split", clips.isNotEmpty()) { doSplit() }
            ToolButton("Delete", clips.isNotEmpty()) { doDelete() }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolButton("Trim start", clips.isNotEmpty()) { doTrimStart() }
            ToolButton("Trim end", clips.isNotEmpty()) { doTrimEnd() }
        }
    }
}
