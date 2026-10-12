package com.neomotion.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

private val clipColors = listOf(
    Color(0xFF6650A4), Color(0xFF3F6FB5), Color(0xFF2E8B7A), Color(0xFFB5683F)
)

private fun fmt(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d.%d".format(s / 60, s % 60, (ms % 1000) / 100)
}

private fun fmtTick(ms: Long): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

private fun DrawScope.drawClipBlock(
    tm: TextMeasurer,
    label: String,
    x0: Float,
    cw: Float,
    top: Float,
    hgt: Float,
    color: Color,
    selected: Boolean,
    lifted: Boolean
) {
    val tl = Offset(x0 + 1f, top)
    val sz = Size(maxOf(cw - 2f, 1f), hgt)
    val r = CornerRadius(8.dp.toPx())
    val alpha = if (lifted) 0.92f else if (selected) 1f else 0.7f
    drawRoundRect(color = color.copy(alpha = alpha), topLeft = tl, size = sz, cornerRadius = r)
    if (selected || lifted) {
        drawRoundRect(
            color = if (lifted) NeoColors.Accent else Color.White,
            topLeft = tl,
            size = sz,
            cornerRadius = r,
            style = Stroke(width = 2.dp.toPx())
        )
    }
    if (cw > 70.dp.toPx()) {
        drawText(
            tm,
            label,
            Offset(x0 + 8.dp.toPx(), top + 6.dp.toPx()),
            style = TextStyle(color = Color.White, fontSize = 11.sp),
            overflow = TextOverflow.Clip,
            maxLines = 1,
            size = Size(minOf(cw - 16.dp.toPx(), size.width), 16.dp.toPx())
        )
    }
}

@Composable
private fun IconBtn(enabled: Boolean = true, onClick: () -> Unit, icon: DrawScope.(Color) -> Unit) {
    val tint = if (enabled) Color.White else Color(0xFF4A4A58)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(22.dp)) { this.icon(tint) }
    }
}

@Composable
private fun SmallBtn(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(NeoColors.Card)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
private fun ToolItem(
    label: String,
    enabled: Boolean = true,
    soon: Boolean = false,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val tint = when {
        !enabled -> Color(0xFF4A4A58)
        soon -> NeoColors.Muted
        else -> Color.White
    }
    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NeoColors.Card),
            contentAlignment = Alignment.Center
        ) { icon(tint) }
        Spacer(Modifier.height(4.dp))
        Text(label, color = tint, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
fun EditorScreen(onExit: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()

    var clips by remember { mutableStateOf(emptyList<Clip>()) }
    var playheadMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var zoomDp by remember { mutableFloatStateOf(60f) } // dp per second
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragDx by remember { mutableFloatStateOf(0f) }
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

    fun ppmOf(density: Float): Float = zoomDp * density / 1000f

    fun soon(name: String) {
        Toast.makeText(context, name + " is coming soon", Toast.LENGTH_SHORT).show()
    }

    fun requestExit() {
        if (clips.isEmpty()) onExit() else showLeaveDialog = true
    }

    BackHandler { requestExit() }

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

    /** Index of the clip under screen x, or null. */
    fun clipIndexAtX(x: Float, widthPx: Int, ppm: Float): Int? {
        val centerX = widthPx / 2f
        for ((i, c) in clips.withIndex()) {
            val x0 = centerX + (TimelineOps.clipStart(clips, i) - playheadMs) * ppm
            val x1 = x0 + c.durationMs * ppm
            if (x >= x0 && x < x1) return i
        }
        return null
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

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave editor?") },
            text = { Text("Saving projects is coming soon. Your edits will be lost if you leave.") },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    onExit()
                }) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Stay") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoColors.Bg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // ----- top bar -----
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBtn(onClick = { requestExit() }) { backIcon(it) }
            Text(
                "Untitled project",
                color = Color.White,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Brush.linearGradient(listOf(NeoColors.Accent, Color(0xFF4A8CFF))))
                    .clickable { soon("Export (1080p 60fps)") }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Export", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // ----- preview -----
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black),
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

        // ----- transport -----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Text(fmt(playheadMs) + " / " + fmt(total), color = Color.White, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (clips.isNotEmpty()) Color.White else Color(0xFF3A3A48))
                    .clickable(enabled = clips.isNotEmpty()) { togglePlay() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(22.dp)) {
                    if (isPlaying) pauseIcon(Color.Black) else playIcon(Color.Black)
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End
            ) {
                IconBtn(enabled = undoStack.isNotEmpty(), onClick = { undo() }) { undoIcon(it) }
                IconBtn(enabled = redoStack.isNotEmpty(), onClick = { redo() }) { redoIcon(it) }
            }
        }

        // ----- timeline -----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NeoColors.Bar)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Drag to scrub. Hold a clip, then drag to reorder.", color = NeoColors.Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                SmallBtn("-") { zoomDp = (zoomDp / 1.4f).coerceAtLeast(12f) }
                Spacer(Modifier.width(6.dp))
                SmallBtn("+") { zoomDp = (zoomDp * 1.4f).coerceAtMost(400f) }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    // tap: add box or move playhead
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { off ->
                            val ppm = ppmOf(density)
                            val centerX = size.width / 2f
                            val endX = centerX + (TimelineOps.totalDuration(clips) - playheadMs) * ppm
                            val gap = 8.dp.toPx()
                            val btn = 48.dp.toPx()
                            if (off.x >= endX + gap && off.x <= endX + gap + btn) {
                                picker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            } else {
                                exo.pause()
                                seek((playheadMs + (off.x - centerX) / ppm).toLong())
                            }
                        })
                    }
                    // drag: scrub the timeline
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                exo.pause()
                                exo.setSeekParameters(SeekParameters.CLOSEST_SYNC)
                            },
                            onDragEnd = {
                                exo.setSeekParameters(SeekParameters.EXACT)
                                seek(playheadMs)
                            },
                            onDragCancel = {
                                exo.setSeekParameters(SeekParameters.EXACT)
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                seek((playheadMs - amount.x / ppmOf(density)).toLong())
                            }
                        )
                    }
                    // long press + drag: reorder clips
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { off ->
                                val idx = clipIndexAtX(off.x, size.width, ppmOf(density))
                                if (idx != null) {
                                    draggingId = clips[idx].id
                                    dragDx = 0f
                                    exo.pause()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            onDragEnd = {
                                val id = draggingId
                                if (id != null) {
                                    val from = clips.indexOfFirst { it.id == id }
                                    if (from >= 0) {
                                        val ppm = ppmOf(density)
                                        val startMs = TimelineOps.clipStart(clips, from)
                                        val dur = clips[from].durationMs
                                        val centerMs = startMs + dur / 2f + dragDx / ppm
                                        val to = TimelineOps.targetIndex(clips, from, centerMs)
                                        if (to != from) {
                                            val moved = TimelineOps.move(clips, from, to)
                                            commit(moved, TimelineOps.clipStart(moved, to))
                                        }
                                    }
                                }
                                draggingId = null
                                dragDx = 0f
                            },
                            onDragCancel = {
                                draggingId = null
                                dragDx = 0f
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                if (draggingId != null) dragDx += amount.x
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f
                val ppm = ppmOf(density)
                val rulerH = 22.dp.toPx()
                val trackTop = rulerH + 10.dp.toPx()
                val trackH = 48.dp.toPx()

                // --- time ruler ---
                val steps = listOf(1, 2, 5, 10, 30, 60, 120, 300)
                val minGapPx = 64.dp.toPx()
                val stepSec = steps.firstOrNull { it * 1000f * ppm >= minGapPx } ?: 300
                val stepMs = stepSec * 1000L
                val tLeft = playheadMs + (0f - centerX) / ppm
                val tRight = playheadMs + (w - centerX) / ppm
                var k = maxOf(0L, (tLeft / stepMs).toLong())
                while (k * stepMs <= tRight) {
                    val t = k * stepMs
                    val x = centerX + (t - playheadMs) * ppm
                    drawLine(
                        Color(0xFF3A3A4A),
                        Offset(x, rulerH - 8.dp.toPx()),
                        Offset(x, rulerH),
                        strokeWidth = 2f
                    )
                    drawText(
                        textMeasurer,
                        fmtTick(t),
                        Offset(x + 4.dp.toPx(), 2.dp.toPx()),
                        style = TextStyle(color = NeoColors.Muted, fontSize = 10.sp),
                        maxLines = 1,
                        size = Size(40.dp.toPx(), 14.dp.toPx())
                    )
                    k++
                }

                // --- clips ---
                val dId = draggingId
                clips.forEachIndexed { i, c ->
                    if (c.id != dId) {
                        val x0 = centerX + (TimelineOps.clipStart(clips, i) - playheadMs) * ppm
                        val cw = c.durationMs * ppm
                        if (x0 + cw >= 0f && x0 <= w) {
                            drawClipBlock(
                                textMeasurer,
                                "Clip " + (i + 1) + "  " + "%.1fs".format(c.durationMs / 1000f),
                                x0, cw, trackTop, trackH,
                                clipColors[i % clipColors.size],
                                i == selectedIdx, false
                            )
                        }
                    }
                }

                // --- "+" add box after the last clip ---
                val addX = centerX + (total - playheadMs) * ppm + 8.dp.toPx()
                if (addX < w && addX + 48.dp.toPx() > 0f) {
                    drawRoundRect(
                        color = Color(0xFF2A2A38),
                        topLeft = Offset(addX, trackTop),
                        size = Size(48.dp.toPx(), trackH),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                    val cx = addX + 24.dp.toPx()
                    val cy = trackTop + trackH / 2f
                    val arm = 8.dp.toPx()
                    drawLine(Color.White, Offset(cx - arm, cy), Offset(cx + arm, cy), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(Color.White, Offset(cx, cy - arm), Offset(cx, cy + arm), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                }

                // --- dragged clip on top ---
                if (dId != null) {
                    val i = clips.indexOfFirst { it.id == dId }
                    if (i >= 0) {
                        val c = clips[i]
                        val x0 = centerX + (TimelineOps.clipStart(clips, i) - playheadMs) * ppm + dragDx
                        val cw = c.durationMs * ppm
                        drawClipBlock(
                            textMeasurer,
                            "Clip " + (i + 1) + "  " + "%.1fs".format(c.durationMs / 1000f),
                            x0, cw, trackTop - 4.dp.toPx(), trackH,
                            clipColors[i % clipColors.size],
                            true, true
                        )
                    }
                }

                // --- playhead ---
                drawLine(Color.White, Offset(centerX, 4.dp.toPx()), Offset(centerX, h), strokeWidth = 2.dp.toPx())
                drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(centerX, 4.dp.toPx()))
            }
        }

        // ----- tool strip -----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToolItem(label = "Import", onClick = {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            }) { c -> Canvas(Modifier.size(24.dp)) { plusIcon(c) } }

            ToolItem(label = "Split", enabled = clips.isNotEmpty(), onClick = { doSplit() }) { c ->
                Canvas(Modifier.size(24.dp)) { splitIcon(c) }
            }
            ToolItem(label = "Trim start", enabled = clips.isNotEmpty(), onClick = { doTrimStart() }) { c ->
                Canvas(Modifier.size(24.dp)) { trimStartIcon(c) }
            }
            ToolItem(label = "Trim end", enabled = clips.isNotEmpty(), onClick = { doTrimEnd() }) { c ->
                Canvas(Modifier.size(24.dp)) { trimEndIcon(c) }
            }
            ToolItem(label = "Delete", enabled = clips.isNotEmpty(), onClick = { doDelete() }) { c ->
                Canvas(Modifier.size(24.dp)) { trashIcon(c) }
            }
            ToolItem(label = "Text", soon = true, onClick = { soon("Text") }) { c ->
                Text("T", color = c, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            ToolItem(label = "Effects", soon = true, onClick = { soon("Effects") }) { c ->
                Text("FX", color = c, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            ToolItem(label = "Keyframes", soon = true, onClick = { soon("Keyframes") }) { c ->
                Canvas(Modifier.size(24.dp)) { diamondIcon(c) }
            }
            ToolItem(label = "Speed", soon = true, onClick = { soon("Speed") }) { c ->
                Text("1x", color = c, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            ToolItem(label = "Mirror", soon = true, onClick = { soon("Mirror") }) { c ->
                Canvas(Modifier.size(24.dp)) { mirrorIcon(c) }
            }
        }
    }
}
