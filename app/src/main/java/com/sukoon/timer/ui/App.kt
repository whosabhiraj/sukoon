package com.sukoon.timer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sukoon.timer.timer.Screen
import com.sukoon.timer.timer.TimerEngine

@Composable
fun SukoonApp(vm: TimerEngine) {
    // Device back button: from any sub-screen, return home.
    BackHandler(enabled = vm.screen != Screen.Home) { vm.goHome() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = vm.screen,
            transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(350)) },
            label = "screen",
        ) { screen ->
            when (screen) {
                is Screen.Home -> HomeScreen(vm)
                is Screen.Edit -> EditScreen(vm, screen.profile)
                is Screen.Run -> RunScreen(vm)
            }
        }
    }
}
