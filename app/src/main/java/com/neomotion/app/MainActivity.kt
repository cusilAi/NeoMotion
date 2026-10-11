package com.neomotion.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NeoMotionRoot() }
    }
}

@Composable
fun NeoMotionRoot() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var tab by remember { mutableStateOf(NavTab.Home) }
    var inEditor by remember { mutableStateOf(false) }
    var crash by remember { mutableStateOf(CrashLogger.read(context)) }

    if (inEditor) {
        EditorScreen(onExit = { inEditor = false })
    } else {
        Column(modifier = Modifier.fillMaxSize().background(NeoColors.Bg)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    NavTab.Home -> HomeScreen(onStart = { inEditor = true })
                    NavTab.Projects -> ProjectsScreen(onNew = { inEditor = true })
                    NavTab.Templates -> TemplatesScreen()
                }
            }
            BottomBar(
                selected = tab,
                onTab = { tab = it },
                onPlus = { inEditor = true }
            )
        }
    }

    val report = crash
    if (report != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("NeoMotion crashed last time") },
            text = {
                Text(
                    report.take(4000),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(report))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }) { Text("Copy") }
            },
            dismissButton = {
                TextButton(onClick = {
                    CrashLogger.clear(context)
                    crash = null
                }) { Text("Close") }
            }
        )
    }
}
