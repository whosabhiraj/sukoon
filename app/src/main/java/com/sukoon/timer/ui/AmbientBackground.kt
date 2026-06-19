package com.sukoon.timer.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.sukoon.timer.model.Phase
import com.sukoon.timer.ui.theme.bgBase
import com.sukoon.timer.ui.theme.bgBlob
import kotlin.math.hypot

private fun DrawScope.blob(cx: Float, cy: Float, radius: Float, color: Color, alpha: Float) {
    if (alpha <= 0f) return
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
            center = Offset(cx, cy),
            radius = radius,
        ),
        radius = radius,
        center = Offset(cx, cy),
    )
}

/**
 * The calm, drifting backdrop for the home and edit screens: a vertical wash plus a few large,
 * slowly wandering colour blobs.
 */
@Composable
fun AmbientBackground(
    top: Color,
    bottom: Color,
    blobs: List<Color>,
    modifier: Modifier = Modifier,
) {
    val t = rememberInfiniteTransition(label = "ambient")
    val a by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(19_000, easing = LinearEasing), RepeatMode.Reverse), label = "a",
    )
    val b by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(27_000, easing = LinearEasing), RepeatMode.Reverse), label = "b",
    )
    Canvas(modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(listOf(top, bottom)))
        val w = size.width
        val h = size.height
        val c0 = blobs[0]
        val c1 = blobs[1.coerceAtMost(blobs.lastIndex)]
        val c2 = blobs[2.coerceAtMost(blobs.lastIndex)]
        blob(w * (0.20f + 0.18f * a), h * (0.18f + 0.10f * b), w * 0.70f, c0, 0.60f)
        blob(w * (0.88f - 0.22f * b), h * (0.34f + 0.12f * a), w * 0.58f, c1, 0.55f)
        blob(w * (0.50f + 0.14f * b), h * (0.88f - 0.10f * a), w * 0.66f, c2, 0.50f)
    }
}

/**
 * The running-screen backdrop. On every phase change the new phase's colour **grows outward from
 * the centre**, over the previous colour, so the screen visibly blooms into the next phase.
 */
@Composable
fun PhaseBackground(phase: Phase, modifier: Modifier = Modifier) {
    var prev by remember { mutableStateOf(phase) }
    val reveal = remember { Animatable(1f) }
    LaunchedEffect(phase) {
        if (phase != prev) {
            reveal.snapTo(0f)
            reveal.animateTo(1f, tween(950, easing = FastOutSlowInEasing))
            prev = phase
        }
    }

    val newBase = phase.bgBase()
    val oldBase = prev.bgBase()
    val newBlob = phase.bgBlob()
    val oldBlob = prev.bgBlob()

    val t = rememberInfiniteTransition(label = "phaseAmbient")
    val d1 by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(15_000, easing = LinearEasing), RepeatMode.Reverse), label = "d1",
    )
    val d2 by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(21_000, easing = LinearEasing), RepeatMode.Reverse), label = "d2",
    )

    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val origin = Offset(w * 0.5f, h * 0.46f)
        val maxR = hypot(w.toDouble(), h.toDouble()).toFloat()
        val r = reveal.value

        // previous phase underneath, fading as the new colour takes over
        drawRect(oldBase)
        blob(w * (0.22f + 0.16f * d1), h * (0.22f + 0.10f * d2), w * 0.7f, oldBlob, (1f - r) * 0.5f)
        blob(w * (0.84f - 0.18f * d2), h * (0.7f + 0.1f * d1), w * 0.62f, oldBlob, (1f - r) * 0.42f)

        // new phase blooming outward from the centre
        drawCircle(color = newBase, radius = r * maxR, center = origin)
        blob(w * (0.26f + 0.16f * d2), h * (0.24f + 0.12f * d1), w * 0.72f, newBlob, r * 0.55f)
        blob(w * (0.82f - 0.2f * d1), h * (0.72f + 0.08f * d2), w * 0.6f, newBlob, r * 0.45f)
    }
}
