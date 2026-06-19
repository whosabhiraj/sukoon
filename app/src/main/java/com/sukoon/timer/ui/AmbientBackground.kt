package com.sukoon.timer.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A calm, slowly drifting backdrop: a soft vertical wash with three large translucent
 * "blobs" that breathe and wander, like light through frosted glass. Pure Canvas, cheap to draw.
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
        infiniteRepeatable(tween(19_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "a",
    )
    val b by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(27_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "b",
    )

    Canvas(modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(listOf(top, bottom)))

        val w = size.width
        val h = size.height

        fun blob(cx: Float, cy: Float, radius: Float, color: Color) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0f)),
                    center = Offset(cx, cy),
                    radius = radius,
                ),
                radius = radius,
                center = Offset(cx, cy),
            )
        }

        val c0 = blobs[0]
        val c1 = blobs[(1).coerceAtMost(blobs.lastIndex)]
        val c2 = blobs[(2).coerceAtMost(blobs.lastIndex)]

        blob(w * (0.22f + 0.16f * a), h * (0.20f + 0.10f * b), w * 0.62f, c0)
        blob(w * (0.85f - 0.20f * b), h * (0.34f + 0.12f * a), w * 0.52f, c1)
        blob(w * (0.50f + 0.12f * b), h * (0.86f - 0.10f * a), w * 0.58f, c2)
    }
}
