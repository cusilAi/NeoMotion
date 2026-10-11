package com.neomotion.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Feature(val title: String, val desc: String, val status: Int)

// status: 0 = ready, 1 = next up, 2 = coming soon
private val features = listOf(
    Feature("Timeline editing", "Multi-clip timeline, drag to reorder, split, trim, undo / redo", 0),
    Feature("Layers & text", "Move, scale and rotate layers with your fingers", 1),
    Feature("Keyframes & graph editor", "Animate anything with bezier speed curves", 2),
    Feature("GPU effects", "Smooth shader effects that stay lag-free", 2),
    Feature("Time remapping", "Speed ramps and smooth slow motion", 2),
    Feature("Mirror", "One-tap mirror and flip tools", 2),
    Feature("Remove background", "Cut out people from video", 2),
    Feature("Face tracking", "Make text and effects follow a face", 2),
    Feature("Video enhancement", "Sharper, cleaner, better looking video", 2),
    Feature("3D text & fonts", "Animated 3D titles with a big font library", 2),
    Feature("Node video", "Node-based video effects", 2),
    Feature("1080p 60 fps export", "Fast hardware export", 2)
)

@Composable
fun HomeScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("NeoMotion", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("The free, no-lag mobile video editor", color = NeoColors.Muted, fontSize = 14.sp)

        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(NeoColors.Accent, Color(0xFF2B1B6B))))
                .padding(20.dp)
        ) {
            Text("Edit without limits", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Keyframes, effects, tracking and more. No premium, every feature is free.",
                color = Color(0xFFE6DFFF),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF2B1B6B)
                )
            ) { Text("Start new project") }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("1080p", "Max export", Modifier.weight(1f))
            StatCard("60 fps", "Smooth", Modifier.weight(1f))
            StatCard("Free", "No premium", Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        Text("Features", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        features.forEach {
            FeatureRow(it)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text("NeoMotion v0.4.0", color = NeoColors.Muted, fontSize = 12.sp)
    }
}

@Composable
private fun StatCard(value: String, caption: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NeoColors.Card)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(caption, color = NeoColors.Muted, fontSize = 12.sp)
    }
}

@Composable
private fun FeatureRow(f: Feature) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NeoColors.Card)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(f.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(f.desc, color = NeoColors.Muted, fontSize = 12.sp)
        }
        Badge(f.status)
    }
}

@Composable
private fun Badge(status: Int) {
    val text = when (status) {
        0 -> "Ready"
        1 -> "Next"
        else -> "Soon"
    }
    val color = when (status) {
        0 -> Color(0xFF3DDC84)
        1 -> NeoColors.Accent
        else -> NeoColors.Muted
    }
    Text(
        text,
        color = color,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun ProjectsScreen(onNew: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Projects", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No projects yet", color = Color.White, fontSize = 18.sp)
                Spacer(Modifier.height(6.dp))
                Text("Saving projects is coming soon.", color = NeoColors.Muted, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNew) { Text("New project") }
            }
        }
    }
}

@Composable
fun TemplatesScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Templates", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Templates are coming soon", color = Color.White, fontSize = 18.sp)
                Spacer(Modifier.height(6.dp))
                Text("Ready-made edits you can use in one tap.", color = NeoColors.Muted, fontSize = 13.sp)
            }
        }
    }
}
