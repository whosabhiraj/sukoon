package com.sukoon.timer.model

import kotlinx.serialization.Serializable

/**
 * A saved routine: a reusable configuration of timed rounds.
 *
 * @param rounds        how many work rounds to repeat (5..15 is the typical range)
 * @param workSeconds   length of each work round
 * @param restSeconds   interval between rounds, to reposition (0 = no rest)
 * @param leadInSeconds a short "get ready" countdown before the first round (0 = skip)
 * @param countdownCues play soft pips for the final 3 seconds of each phase
 */
@Serializable
data class TimerProfile(
    val id: String,
    val name: String,
    val rounds: Int,
    val workSeconds: Int,
    val restSeconds: Int,
    val leadInSeconds: Int = 5,
    val countdownCues: Boolean = true,
) {
    /** A friendly one-line summary, e.g. "10 rounds · 2:00 work · 30s rest". */
    val summary: String
        get() = buildString {
            append(rounds)
            append(if (rounds == 1) " round" else " rounds")
            append("  ·  ")
            append(shortClock(workSeconds))
            append(" work")
            if (restSeconds > 0) {
                append("  ·  ")
                append(shortClock(restSeconds))
                append(" rest")
            }
        }
}

/** mm:ss for the running screen (always shows minutes), e.g. 0:30, 2:05. */
fun fullClock(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}

/** Compact form for summaries: "30s", "2:00", "1:30". */
fun shortClock(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    return if (s < 60) "${s}s" else "%d:%02d".format(s / 60, s % 60)
}
