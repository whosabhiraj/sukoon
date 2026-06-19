package com.sukoon.timer.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.timer.model.Phase

// ---- Palette: lavender, sage, baby pink, a lighter Claude clay — but bolder ----------
val Lavender = Color(0xFF9B86D9)
val LavenderSoft = Color(0xFFE7DEFA)
val Sage = Color(0xFF6FB387)
val SageSoft = Color(0xFFDCEFE0)
val BabyPink = Color(0xFFF48FB1)
val PinkSoft = Color(0xFFFBDCE7)
val ClaudePeach = Color(0xFFEC9A74)
val PeachSoft = Color(0xFFF9DECB)

val Ink = Color(0xFF3A3350)        // deep plum text
val InkSoft = Color(0xFF8A82A0)
val Cream = Color(0xFFFBF7FF)
val Sunflower = Color(0xFFF2C14E)  // soft, light sunflower yellow

// Deep accents — used for text/icons sitting on a light, colourful phase background.
val PeachDeep = Color(0xFFC2602F)
val SageDeep = Color(0xFF36864F)
val LavenderDeep = Color(0xFF5C45AD)
val PinkDeep = Color(0xFFBF4775)

private val SukoonColors = lightColorScheme(
    primary = Lavender,
    onPrimary = Color.White,
    primaryContainer = LavenderSoft,
    onPrimaryContainer = LavenderDeep,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = SageSoft,
    onSecondaryContainer = SageDeep,
    tertiary = ClaudePeach,
    onTertiary = Color.White,
    tertiaryContainer = PeachSoft,
    onTertiaryContainer = PeachDeep,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = LavenderSoft,
    onSurfaceVariant = InkSoft,
    outline = Color(0xFFCDC2E6),
)

private val SukoonShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

private val SukoonType = Typography(
    displayLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight(600),
        fontSize = 60.sp, letterSpacing = (-1).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight(600), fontSize = 32.sp,
        letterSpacing = (-0.5).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight(600), fontSize = 23.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight(500), fontSize = 19.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight(400), fontSize = 18.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight(400), fontSize = 16.sp,
        letterSpacing = 0.1.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight(600), fontSize = 17.sp,
        letterSpacing = 0.2.sp,
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

/** The mid accent for a phase (cards, ring on a light surface). */
fun Phase.accent(): Color = when (this) {
    Phase.GetReady -> ClaudePeach
    Phase.Work -> Sage
    Phase.Rest -> Lavender
    Phase.Done -> BabyPink
    Phase.Idle -> Lavender
}

/** A deep, high-contrast accent for text/icons on the phase's light background. */
fun Phase.accentDeep(): Color = when (this) {
    Phase.GetReady -> PeachDeep
    Phase.Work -> SageDeep
    Phase.Rest -> LavenderDeep
    Phase.Done -> PinkDeep
    Phase.Idle -> LavenderDeep
}

/** The prominent base colour the running background fills with for this phase. */
fun Phase.bgBase(): Color = when (this) {
    Phase.GetReady -> Color(0xFFF7D7C1)
    Phase.Work -> Color(0xFFB4E1C1)
    Phase.Rest -> Color(0xFFD9C9F4)
    Phase.Done -> Color(0xFFF7C8DC)
    Phase.Idle -> Color(0xFFE7DEFA)
}

/** A richer drifting-blob tint layered over [bgBase]. */
fun Phase.bgBlob(): Color = when (this) {
    Phase.GetReady -> ClaudePeach
    Phase.Work -> Sage
    Phase.Rest -> Lavender
    Phase.Done -> BabyPink
    Phase.Idle -> Lavender
}

/** A human, encouraging label for each phase. */
fun Phase.label(): String = when (this) {
    Phase.GetReady -> "Get ready"
    Phase.Work -> "Work"
    Phase.Rest -> "Rest"
    Phase.Done -> "All done"
    Phase.Idle -> ""
}
