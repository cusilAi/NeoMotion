package com.neomotion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NeoMotionRoot() }
    }
}

@Composable
fun NeoMotionRoot() {
    var tab by remember { mutableStateOf(NavTab.Home) }
    var inEditor by remember { mutableStateOf(false) }

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
}
