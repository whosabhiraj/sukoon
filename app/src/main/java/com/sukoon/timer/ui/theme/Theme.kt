package com.sukoon.timer.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.timer.model.Phase

// ---- The palette: lavender, sage, baby pink, and a lighter Claude clay -----------------
val Lavender = Color(0xFFB9A7E0)
val LavenderSoft = Color(0xFFEDE7FA)
val Sage = Color(0xFF8FB58C)
val SageSoft = Color(0xFFE3EEE0)
val BabyPink = Color(0xFFF0B6C6)
val PinkSoft = Color(0xFFFBE4EA)
val ClaudePeach = Color(0xFFE7A98C) // a softer, lighter take on the Claude clay tone
val PeachSoft = Color(0xFFF8E2D6)

val Ink = Color(0xFF4A4458)         // soft deep plum, gentle on the eyes
val InkSoft = Color(0xFF837B92)
val Cream = Color(0xFFFCF9FF)

private val SukoonColors = lightColorScheme(
    primary = Lavender,
    onPrimary = Color.White,
    primaryContainer = LavenderSoft,
    onPrimaryContainer = Ink,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = SageSoft,
    onSecondaryContainer = Ink,
    tertiary = ClaudePeach,
    onTertiary = Color.White,
    tertiaryContainer = PeachSoft,
    onTertiaryContainer = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = LavenderSoft,
    onSurfaceVariant = InkSoft,
    outline = Color(0xFFD9D0E8),
)

private val SukoonShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

private val SukoonType = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 52.sp,
        letterSpacing = (-1).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
    ),
)

@Composable
fun SukoonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SukoonColors,
        shapes = SukoonShapes,
        typography = SukoonType,
        content = content,
    )
}

// ---- Per-phase colour language ---------------------------------------------------------
/** The vivid accent used for the ring and controls in a given phase. */
fun Phase.accent(): Color = when (this) {
    Phase.GetReady -> ClaudePeach
    Phase.Work -> Sage
    Phase.Rest -> Lavender
    Phase.Done -> BabyPink
    Phase.Idle -> Lavender
}

/** The two soft tints used for that phase's drifting background. */
fun Phase.ambient(): Pair<Color, Color> = when (this) {
    Phase.GetReady -> PeachSoft to PinkSoft
    Phase.Work -> SageSoft to LavenderSoft
    Phase.Rest -> LavenderSoft to PinkSoft
    Phase.Done -> PinkSoft to LavenderSoft
    Phase.Idle -> LavenderSoft to PinkSoft
}

/** A human, encouraging label for each phase. */
fun Phase.label(): String = when (this) {
    Phase.GetReady -> "Get ready"
    Phase.Work -> "Work"
    Phase.Rest -> "Rest"
    Phase.Done -> "All done"
    Phase.Idle -> ""
}
