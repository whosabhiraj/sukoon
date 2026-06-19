package com.sukoon.timer.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.timer.model.Phase
import com.sukoon.timer.model.fullClock
import com.sukoon.timer.timer.TimerEngine
import com.sukoon.timer.ui.theme.Fraunces
import com.sukoon.timer.ui.theme.Ink
import com.sukoon.timer.ui.theme.PinkDeep
import com.sukoon.timer.ui.theme.accentDeep
import com.sukoon.timer.ui.theme.label

private val White70 = Color.White.copy(alpha = 0.70f)
private val WhiteTrack = Color.White.copy(alpha = 0.34f)

@Composable
fun RunScreen(vm: TimerEngine) {
    // Keep the screen awake for the whole session so a chime is never missed.
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    Box(Modifier.fillMaxSize()) {
        PhaseBackground(phase = vm.phase)
        if (vm.phase == Phase.Done) DoneContent(vm) else RunningContent(vm)
    }
}

@Composable
private fun RunningContent(vm: TimerEngine) {
    val phase = vm.phase
    val deep = phase.accentDeep()
    val fraction = if (vm.phaseTotal > 0) vm.remaining.toFloat() / vm.phaseTotal else 0f

    // A slow "breathing" pulse during rest, to encourage easing off.
    val breathe = rememberInfiniteTransition(label = "breathe")
    val breathScale by breathe.animateFloat(
        0.97f, 1.03f,
        infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse),
        label = "breathScale",
    )
    val centreScale = if (phase == Phase.Rest && !vm.paused) breathScale else 1f

    Column(
        Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(18.dp))
        Text(
            text = if (phase == Phase.GetReady) "Getting ready" else "Round ${vm.round} of ${vm.totalRounds}",
            style = MaterialTheme.typography.titleLarge,
            color = deep,
        )

        Spacer(Modifier.weight(1f))

        TimerRing(
            fraction = fraction,
            color = Color.White,
            trackColor = WhiteTrack,
            modifier = Modifier.fillMaxWidth(0.84f),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(centreScale),
            ) {
                AnimatedContent(
                    targetState = fullClock(vm.remaining),
                    transitionSpec = {
                        (slideInVertically { it / 3 } + fadeIn(tween(280))) togetherWith
                            (slideOutVertically { -it / 3 } + fadeOut(tween(220)))
                    },
                    label = "time",
                ) { shown ->
                    Text(
                        shown,
                        fontFamily = Fraunces,
                        fontWeight = FontWeight(600),
                        // Leave comfortable space inside the ring, including for 1:00:00.
                        fontSize = 64.sp,
                        color = Ink,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                Text(
                    text = if (vm.paused) "Paused" else phase.label(),
                    style = MaterialTheme.typography.titleMedium,
                    color = deep,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(
                icon = Icons.Rounded.Close, contentDescription = "Stop",
                background = White70, tint = deep, onClick = { vm.goHome() }, size = 60.dp,
            )
            RoundIconButton(
                icon = if (vm.paused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                contentDescription = if (vm.paused) "Resume" else "Pause",
                background = Color.White, tint = deep, onClick = { vm.togglePause() }, size = 84.dp,
            )
            RoundIconButton(
                icon = Icons.Rounded.SkipNext, contentDescription = "Skip",
                background = White70, tint = deep, onClick = { vm.skip() }, size = 60.dp,
            )
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun DoneContent(vm: TimerEngine) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val bloom by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "bloom",
    )

    Column(
        Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
            Ripples()
            Text(
                "✿",
                fontFamily = Fraunces,
                fontSize = 128.sp,
                color = Color.White,
                // Font baseline metrics put the visible flower slightly above the
                // centre of its text box; offset it to the ripple's visual centre.
                modifier = Modifier.offset(x = -2.dp, y = -8.dp).scale(bloom),
            )
        }
        Spacer(Modifier.height(34.dp))
        Text("Well done!", style = MaterialTheme.typography.displayLarge, color = Ink)
        Spacer(Modifier.height(10.dp))
        Text(
            "You completed ${vm.totalRounds} rounds.\nTake a moment to breathe.",
            style = MaterialTheme.typography.bodyLarge,
            color = PinkDeep,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        SoftButton(
            text = "Do it again",
            background = Color.White,
            textColor = PinkDeep,
            leading = Icons.Rounded.Replay,
            onClick = { vm.repeat() },
        )
        Spacer(Modifier.height(12.dp))
        SoftButton(
            text = "Back home",
            background = Color.White.copy(alpha = 0.55f),
            textColor = Ink,
            onClick = { vm.goHome() },
        )
    }
}

/** Soft concentric ripples that bloom outward — a gentle, looping celebration. */
@Composable
private fun Ripples() {
    val t = rememberInfiniteTransition(label = "ripples")
    val p by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "p",
    )
    Canvas(Modifier.fillMaxSize()) {
        val maxR = size.minDimension / 2f
        for (i in 0 until 3) {
            val f = (p + i / 3f) % 1f
            drawCircle(
                color = Color.White.copy(alpha = (1f - f) * 0.5f),
                radius = f * maxR,
                center = Offset(size.width / 2f, size.height / 2f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}
