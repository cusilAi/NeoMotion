package com.neomotion.app

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

private fun DrawScope.st(): Stroke =
    Stroke(width = size.width * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)

fun DrawScope.backIcon(c: Color) {
    val w = size.width
    val h = size.height
    val p = Path().apply {
        moveTo(w * 0.65f, h * 0.15f)
        lineTo(w * 0.3f, h * 0.5f)
        lineTo(w * 0.65f, h * 0.85f)
    }
    drawPath(p, c, style = st())
}

fun DrawScope.playIcon(c: Color) {
    val w = size.width
    val h = size.height
    val p = Path().apply {
        moveTo(w * 0.28f, h * 0.12f)
        lineTo(w * 0.86f, h * 0.5f)
        lineTo(w * 0.28f, h * 0.88f)
        close()
    }
    drawPath(p, c)
}

fun DrawScope.pauseIcon(c: Color) {
    val w = size.width
    val h = size.height
    drawRoundRect(c, Offset(w * 0.2f, h * 0.15f), Size(w * 0.22f, h * 0.7f), CornerRadius(w * 0.05f))
    drawRoundRect(c, Offset(w * 0.58f, h * 0.15f), Size(w * 0.22f, h * 0.7f), CornerRadius(w * 0.05f))
}

fun DrawScope.undoIcon(c: Color) {
    val w = size.width
    val h = size.height
    val head = Path().apply {
        moveTo(w * 0.3f, h * 0.2f)
        lineTo(w * 0.12f, h * 0.4f)
        lineTo(w * 0.3f, h * 0.6f)
    }
    val body = Path().apply {
        moveTo(w * 0.12f, h * 0.4f)
        lineTo(w * 0.55f, h * 0.4f)
        cubicTo(w * 0.9f, h * 0.4f, w * 0.9f, h * 0.85f, w * 0.55f, h * 0.85f)
        lineTo(w * 0.3f, h * 0.85f)
    }
    drawPath(head, c, style = st())
    drawPath(body, c, style = st())
}

fun DrawScope.redoIcon(c: Color) {
    val w = size.width
    val h = size.height
    val head = Path().apply {
        moveTo(w * 0.7f, h * 0.2f)
        lineTo(w * 0.88f, h * 0.4f)
        lineTo(w * 0.7f, h * 0.6f)
    }
    val body = Path().apply {
        moveTo(w * 0.88f, h * 0.4f)
        lineTo(w * 0.45f, h * 0.4f)
        cubicTo(w * 0.1f, h * 0.4f, w * 0.1f, h * 0.85f, w * 0.45f, h * 0.85f)
        lineTo(w * 0.7f, h * 0.85f)
    }
    drawPath(head, c, style = st())
    drawPath(body, c, style = st())
}

fun DrawScope.splitIcon(c: Color) {
    val w = size.width
    val h = size.height
    val s = st()
    drawLine(c, Offset(w * 0.5f, h * 0.08f), Offset(w * 0.5f, h * 0.92f), strokeWidth = s.width, cap = StrokeCap.Round)
    drawRoundRect(c, Offset(w * 0.08f, h * 0.3f), Size(w * 0.3f, h * 0.4f), CornerRadius(w * 0.06f), style = s)
    drawRoundRect(c, Offset(w * 0.62f, h * 0.3f), Size(w * 0.3f, h * 0.4f), CornerRadius(w * 0.06f), style = s)
}

fun DrawScope.trimStartIcon(c: Color) {
    val w = size.width
    val h = size.height
    val s = st()
    val br = Path().apply {
        moveTo(w * 0.42f, h * 0.18f)
        lineTo(w * 0.18f, h * 0.18f)
        lineTo(w * 0.18f, h * 0.82f)
        lineTo(w * 0.42f, h * 0.82f)
    }
    drawPath(br, c, style = s)
    drawRoundRect(c, Offset(w * 0.5f, h * 0.3f), Size(w * 0.4f, h * 0.4f), CornerRadius(w * 0.06f), style = s)
}

fun DrawScope.trimEndIcon(c: Color) {
    val w = size.width
    val h = size.height
    val s = st()
    val br = Path().apply {
        moveTo(w * 0.58f, h * 0.18f)
        lineTo(w * 0.82f, h * 0.18f)
        lineTo(w * 0.82f, h * 0.82f)
        lineTo(w * 0.58f, h * 0.82f)
    }
    drawPath(br, c, style = s)
    drawRoundRect(c, Offset(w * 0.1f, h * 0.3f), Size(w * 0.4f, h * 0.4f), CornerRadius(w * 0.06f), style = s)
}

fun DrawScope.trashIcon(c: Color) {
    val w = size.width
    val h = size.height
    val s = st()
    drawLine(c, Offset(w * 0.15f, h * 0.27f), Offset(w * 0.85f, h * 0.27f), strokeWidth = s.width, cap = StrokeCap.Round)
    val handle = Path().apply {
        moveTo(w * 0.4f, h * 0.27f)
        lineTo(w * 0.4f, h * 0.14f)
        lineTo(w * 0.6f, h * 0.14f)
        lineTo(w * 0.6f, h * 0.27f)
    }
    val body = Path().apply {
        moveTo(w * 0.24f, h * 0.33f)
        lineTo(w * 0.3f, h * 0.88f)
        lineTo(w * 0.7f, h * 0.88f)
        lineTo(w * 0.76f, h * 0.33f)
    }
    drawPath(handle, c, style = s)
    drawPath(body, c, style = s)
}

fun DrawScope.plusIcon(c: Color) {
    val w = size.width
    val h = size.height
    val s = st()
    drawLine(c, Offset(w * 0.15f, h * 0.5f), Offset(w * 0.85f, h * 0.5f), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(w * 0.5f, h * 0.15f), Offset(w * 0.5f, h * 0.85f), strokeWidth = s.width, cap = StrokeCap.Round)
}

fun DrawScope.diamondIcon(c: Color) {
    val w = size.width
    val h = size.height
    val p = Path().apply {
        moveTo(w * 0.5f, h * 0.1f)
        lineTo(w * 0.9f, h * 0.5f)
        lineTo(w * 0.5f, h * 0.9f)
        lineTo(w * 0.1f, h * 0.5f)
        close()
    }
    drawPath(p, c, style = st())
}

fun DrawScope.mirrorIcon(c: Color) {
    val w = size.width
    val h = size.height
    val left = Path().apply {
        moveTo(w * 0.42f, h * 0.2f)
        lineTo(w * 0.42f, h * 0.8f)
        lineTo(w * 0.08f, h * 0.8f)
        close()
    }
    val right = Path().apply {
        moveTo(w * 0.58f, h * 0.2f)
        lineTo(w * 0.58f, h * 0.8f)
        lineTo(w * 0.92f, h * 0.8f)
        close()
    }
    drawPath(left, c, style = st())
    drawPath(right, c, style = st())
}
