package com.sukoon.timer.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.timer.model.Phase
import com.sukoon.timer.model.fullClock
import com.sukoon.timer.timer.TimerEngine
import com.sukoon.timer.ui.theme.BabyPink
import com.sukoon.timer.ui.theme.Ink
import com.sukoon.timer.ui.theme.InkSoft
import com.sukoon.timer.ui.theme.Lavender
import com.sukoon.timer.ui.theme.accent
import com.sukoon.timer.ui.theme.ambient
import com.sukoon.timer.ui.theme.label

@Composable
fun RunScreen(vm: TimerEngine) {
    // Keep the screen awake for the whole session so a chime is never missed.
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    val phase = vm.phase
    val (ambTopTarget, ambBottomTarget) = phase.ambient()
    val ambTop by animateColorAsState(ambTopTarget, tween(700), label = "ambTop")
    val ambBottom by animateColorAsState(ambBottomTarget, tween(700), label = "ambBottom")

    Box(Modifier.fillMaxSize()) {
        AmbientBackground(
            top = ambTop,
            bottom = ambBottom,
            blobs = listOf(phase.accent().copy(alpha = 0.5f), Lavender, BabyPink),
        )

        if (phase == Phase.Done) {
            DoneContent(vm)
        } else {
            RunningContent(vm)
        }
    }
}

@Composable
private fun RunningContent(vm: TimerEngine) {
    val phase = vm.phase
    val accent = phase.accent()
    val fraction = if (vm.phaseTotal > 0) vm.remaining.toFloat() / vm.phaseTotal else 0f

    // A slow "breathing" pulse used during Rest to encourage easing off.
    val breathe = rememberInfiniteTransition(label = "breathe")
    val breathScale by breathe.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse),
        label = "breathScale",
    )
    val centreScale = if (phase == Phase.Rest && !vm.paused) breathScale else 1f

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = when (phase) {
                Phase.GetReady -> "Getting ready"
                else -> "Round ${vm.round} of ${vm.totalRounds}"
            },
            style = MaterialTheme.typography.titleLarge,
            color = InkSoft,
        )

        Spacer(Modifier.weight(1f))

        TimerRing(
            fraction = fraction,
            color = accent,
            modifier = Modifier.fillMaxWidth(0.82f),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(centreScale),
            ) {
                AnimatedContent(
                    targetState = fullClock(vm.remaining),
                    transitionSpec = {
                        (fadeIn(tween(220))) togetherWith (fadeOut(tween(220)))
                    },
                    label = "time",
                ) { shown ->
                    Text(
                        shown,
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Light,
                        color = Ink,
                    )
                }
                Text(
                    text = if (vm.paused) "Paused" else phase.label(),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Controls
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = "Stop",
                background = Color.White.copy(alpha = 0.7f),
                tint = InkSoft,
                onClick = { vm.goHome() },
                size = 58.dp,
            )
            RoundIconButton(
                icon = if (vm.paused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                contentDescription = if (vm.paused) "Resume" else "Pause",
                background = accent,
                tint = Color.White,
                onClick = { vm.togglePause() },
                size = 78.dp,
            )
            RoundIconButton(
                icon = Icons.Rounded.SkipNext,
                contentDescription = "Skip",
                background = Color.White.copy(alpha = 0.7f),
                tint = InkSoft,
                onClick = { vm.skip() },
                size = 58.dp,
            )
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun DoneContent(vm: TimerEngine) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val bloom by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(900, easing = LinearEasing),
        label = "bloom",
    )

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            TimerRing(
                fraction = bloom,
                color = BabyPink,
                modifier = Modifier.fillMaxWidth(0.7f),
            ) {
                Text(
                    "✿",
                    fontSize = 64.sp,
                    color = BabyPink,
                    modifier = Modifier.scale(bloom),
                )
            }
        }
        Spacer(Modifier.height(36.dp))
        Text(
            "Well done!",
            style = MaterialTheme.typography.displayLarge,
            color = Ink,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "You completed ${vm.totalRounds} rounds.\nTake a moment to breathe.",
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        SoftButton(
            text = "Do it again",
            background = Lavender,
            textColor = Color.White,
            leading = Icons.Rounded.Replay,
            onClick = { vm.repeat() },
        )
        Spacer(Modifier.height(12.dp))
        SoftButton(
            text = "Back home",
            background = Color.White,
            textColor = Ink,
            onClick = { vm.goHome() },
        )
    }
}
