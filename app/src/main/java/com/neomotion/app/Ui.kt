package com.neomotion.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object NeoColors {
    val Bg = Color(0xFF0D0D12)
    val Card = Color(0xFF17171F)
    val Bar = Color(0xFF12121A)
    val Accent = Color(0xFF7C5CFF)
    val Muted = Color(0xFF8A8A9A)
}

enum class NavTab { Projects, Templates, Home }

@Composable
fun BottomBar(selected: NavTab, onTab: (NavTab) -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeoColors.Bar)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BarItem(
            "Projects", selected == NavTab.Projects,
            { onTab(NavTab.Projects) }, Modifier.weight(1f)
        ) { folderIcon(it) }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(NeoColors.Accent, Color(0xFF4A8CFF))))
                    .clickable(onClick = onPlus),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(22.dp)) {
                    val w = size.width
                    val h = size.height
                    drawLine(Color.White, Offset(3f, h / 2), Offset(w - 3f, h / 2), strokeWidth = 7f, cap = StrokeCap.Round)
                    drawLine(Color.White, Offset(w / 2, 3f), Offset(w / 2, h - 3f), strokeWidth = 7f, cap = StrokeCap.Round)
                }
            }
        }

        BarItem(
            "Templates", selected == NavTab.Templates,
            { onTab(NavTab.Templates) }, Modifier.weight(1f)
        ) { templatesIcon(it) }

        BarItem(
            "Home", selected == NavTab.Home,
            { onTab(NavTab.Home) }, Modifier.weight(1f)
        ) { homeIcon(it) }
    }
}

@Composable
private fun BarItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    icon: DrawScope.(Color) -> Unit
) {
    val c = if (selected) NeoColors.Accent else NeoColors.Muted
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(Modifier.size(24.dp)) { this.icon(c) }
        Spacer(Modifier.height(2.dp))
        Text(label, color = c, fontSize = 11.sp)
    }
}

private fun DrawScope.homeIcon(c: Color) {
    val w = size.width
    val h = size.height
    val s = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val roof = Path().apply {
        moveTo(w * 0.08f, h * 0.5f)
        lineTo(w * 0.5f, h * 0.12f)
        lineTo(w * 0.92f, h * 0.5f)
    }
    val body = Path().apply {
        moveTo(w * 0.2f, h * 0.44f)
        lineTo(w * 0.2f, h * 0.88f)
        lineTo(w * 0.8f, h * 0.88f)
        lineTo(w * 0.8f, h * 0.44f)
    }
    drawPath(roof, c, style = s)
    drawPath(body, c, style = s)
}

private fun DrawScope.folderIcon(c: Color) {
    val w = size.width
    val h = size.height
    val s = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawRoundRect(
        c,
        topLeft = Offset(w * 0.08f, h * 0.3f),
        size = Size(w * 0.84f, h * 0.58f),
        cornerRadius = CornerRadius(w * 0.1f),
        style = s
    )
    val tab = Path().apply {
        moveTo(w * 0.08f, h * 0.3f)
        lineTo(w * 0.08f, h * 0.16f)
        lineTo(w * 0.4f, h * 0.16f)
        lineTo(w * 0.5f, h * 0.3f)
    }
    drawPath(tab, c, style = s)
}

private fun DrawScope.templatesIcon(c: Color) {
    val w = size.width
    val h = size.height
    val s = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val g = w * 0.38f
    for (r in 0..1) {
        for (col in 0..1) {
            drawRoundRect(
                c,
                topLeft = Offset(w * 0.1f + col * (g + w * 0.04f), h * 0.1f + r * (g + h * 0.04f)),
                size = Size(g, g),
                cornerRadius = CornerRadius(w * 0.07f),
                style = s
            )
        }
    }
}
