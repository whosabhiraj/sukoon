package com.sukoon.timer.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.sukoon.timer.R

// Inter — a clean, neutral, San-Francisco-like sans. Used everywhere, with light weights for a
// calm, simple feel. It's a variable font, so each weight is derived via FontVariation (API 26+).

@OptIn(ExperimentalTextApi::class)
private fun inter(weight: Int) = Font(
    resId = R.font.inter,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Inter = FontFamily(
    inter(300), inter(400), inter(500), inter(600), inter(700),
)

// Fraunces — an elegant soft serif, kept only for the big timer number (and the bloom flower),
// which the user prefers over the plain sans there.
@OptIn(ExperimentalTextApi::class)
private fun fraunces(weight: Int) = Font(
    resId = R.font.fraunces,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Fraunces = FontFamily(
    fraunces(500), fraunces(600), fraunces(700),
)
