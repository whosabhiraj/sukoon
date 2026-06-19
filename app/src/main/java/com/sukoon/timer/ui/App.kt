package com.sukoon.timer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.sukoon.timer.timer.Screen
import com.sukoon.timer.timer.TimerEngine

private fun order(screen: Screen): Int = when (screen) {
    is Screen.Home -> 0
    is Screen.Edit -> 1
    is Screen.Run -> 2
}

@Composable
fun SukoonApp(vm: TimerEngine) {
    // Device back button: from any sub-screen, return home.
    BackHandler(enabled = vm.screen != Screen.Home) { vm.goHome() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = vm.screen,
            transitionSpec = {
                // Forward navigation slides left (new in from the right); back slides right.
                val forward = order(targetState) >= order(initialState)
                val dir = if (forward) SlideDirection.Left else SlideDirection.Right
                val spec = tween<IntOffset>(440, easing = FastOutSlowInEasing)
                (slideIntoContainer(dir, spec) + fadeIn(tween(360))) togetherWith
                    (slideOutOfContainer(dir, spec) + fadeOut(tween(300)))
            },
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
