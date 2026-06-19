package com.sukoon.timer.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The centrepiece on the running screen: a ring that depletes as the phase counts down,
 * with a soft glow behind it and a smooth, eased sweep.
 */
@Composable
fun TimerRing(
    fraction: Float,       // 1f at the start of a phase, 0f when it ends
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable () -> Unit,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "ring",
    )
    val ringColor by animateColorAsState(color, tween(600), label = "ringColor")

    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val stroke = size.minDimension * 0.05f
            val pad = stroke * 1.8f
            val arcSize = Size(size.minDimension - pad * 2, size.minDimension - pad * 2)
            val topLeft = Offset(
                (size.width - arcSize.width) / 2f,
                (size.height - arcSize.height) / 2f,
            )
            val radius = arcSize.width / 2f
            val centre = Offset(size.width / 2f, size.height / 2f)
            // soft glow
            drawArc(
                color = ringColor.copy(alpha = 0.22f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = stroke * 2.6f, cap = StrokeCap.Round),
            )
            // full track
            drawArc(
                color = trackColor,
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            // remaining time
            val sweep = 360f * animated
            drawArc(
                color = ringColor,
                startAngle = -90f, sweepAngle = sweep, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            // glowing dot at the leading tip
            if (animated > 0.001f) {
                val angle = Math.toRadians((-90f + sweep).toDouble())
                val tip = Offset(
                    centre.x + radius * kotlin.math.cos(angle).toFloat(),
                    centre.y + radius * kotlin.math.sin(angle).toFloat(),
                )
                drawCircle(ringColor.copy(alpha = 0.30f), radius = stroke * 1.5f, center = tip)
                drawCircle(ringColor, radius = stroke * 0.62f, center = tip)
            }
        }
        content()
    }
}

/** A large, friendly +/- stepper for durations and counts — big, easy-to-hit targets. */
@Composable
fun Stepper(
    label: String,
    valueText: String,
    accent: Color,
    valueColor: Color,
    container: Color,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
    onValueClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = container,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .then(if (onValueClick != null) Modifier.clickable(onClick = onValueClick) else Modifier),
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = valueColor.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        valueText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = valueColor,
                    )
                    if (onValueClick != null) {
                        Spacer(Modifier.size(8.dp))
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Type a value for $label",
                            tint = valueColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }
            RoundIconButton(Icons.Rounded.Remove, "Decrease $label", Color.White.copy(alpha = 0.6f), accent, onMinus)
            Spacer(Modifier.size(12.dp))
            RoundIconButton(Icons.Rounded.Add, "Increase $label", accent, Color.White, onPlus)
        }
    }
}

/** A circular icon button with a gentle press animation. */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    background: Color,
    tint: Color,
    onClick: () -> Unit,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f, tween(120), label = "press")
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .background(background, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint)
    }
}

/** A wide, soft pill button used for the primary actions. */
@Composable
fun SoftButton(
    text: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(120), label = "press")
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = RoundedCornerShape(29.dp),
        color = background,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                Icon(leading, contentDescription = null, tint = textColor)
                Spacer(Modifier.size(8.dp))
            }
            Text(
                text,
                color = textColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
