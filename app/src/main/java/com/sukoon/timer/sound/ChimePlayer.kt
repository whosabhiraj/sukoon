package com.sukoon.timer.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

/**
 * Synthesises soft, bell-like chimes on the fly with [AudioTrack] — no audio files needed,
 * so the app stays tiny and every cue is precisely tuned.
 *
 * Each cue has its own deliberately distinct shape, so the listener can learn them by ear
 * (like a car fob: one honk to lock, two to unlock):
 *
 *  - [Cue.Ready]      one calm low note  — "get ready"
 *  - [Cue.WorkStart]  two rising notes   — "begin"
 *  - [Cue.RestStart]  one warm mid note  — "ease off, reposition"
 *  - [Cue.Tick]       a soft high pip     — final-3-seconds countdown
 *  - [Cue.Complete]   a rising arpeggio   — "all done, well done"
 */
class ChimePlayer {

    enum class Cue { Ready, WorkStart, RestStart, Tick, Complete }

    private val sampleRate = 44_100
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun play(cue: Cue) {
        scope.launch {
            val pcm = render(cue)
            playPcm(pcm)
        }
    }

    private fun render(cue: Cue): ShortArray = when (cue) {
        Cue.Ready -> tones(
            listOf(Note(392.00, 0.00, 0.55)),            // G4
            masterGain = 0.7,
        )
        Cue.WorkStart -> tones(
            listOf(
                Note(587.33, 0.00, 0.26),                 // D5
                Note(880.00, 0.17, 0.42),                 // A5  (rising)
            ),
            masterGain = 0.85,
        )
        Cue.RestStart -> tones(
            listOf(Note(440.00, 0.00, 0.55)),             // A4 (warm, single)
            masterGain = 0.8,
        )
        Cue.Tick -> tones(
            listOf(Note(1318.51, 0.00, 0.10)),            // E6 short pip
            masterGain = 0.32,
        )
        Cue.Complete -> tones(
            listOf(
                Note(523.25, 0.00, 0.40),                 // C5
                Note(659.25, 0.15, 0.40),                 // E5
                Note(783.99, 0.30, 0.40),                 // G5
                Note(1046.50, 0.45, 0.75),                // C6 (resolve)
            ),
            masterGain = 0.85,
        )
    }

    private data class Note(
        val freq: Double,
        val start: Double, // seconds from the beginning of the cue
        val dur: Double,   // seconds
        val gain: Double = 1.0,
    )

    /** Mixes the notes into one normalised 16-bit PCM buffer with a soft bell envelope. */
    private fun tones(notes: List<Note>, masterGain: Double): ShortArray {
        val totalSec = (notes.maxOf { it.start + it.dur }) + 0.06
        val total = (totalSec * sampleRate).toInt()
        val buf = DoubleArray(total)
        val attackSamples = 0.008 * sampleRate

        for (note in notes) {
            val startIdx = (note.start * sampleRate).toInt()
            val len = (note.dur * sampleRate).toInt()
            val decayRate = 4.6 / note.dur // ~full fade across the note's length
            for (i in 0 until len) {
                val idx = startIdx + i
                if (idx >= total) break
                val t = i.toDouble() / sampleRate
                val attack = if (i < attackSamples) i / attackSamples else 1.0
                val env = attack * exp(-t * decayRate)
                // fundamental plus two soft harmonics → a warm, marimba/bell timbre
                val w = sin(2 * PI * note.freq * t) +
                    0.5 * sin(2 * PI * note.freq * 2 * t) +
                    0.22 * sin(2 * PI * note.freq * 3 * t)
                buf[idx] += w * env * note.gain
            }
        }

        var peak = 0.0
        for (v in buf) {
            val a = abs(v)
            if (a > peak) peak = a
        }
        val norm = if (peak > 0) (0.92 * masterGain) / peak else 0.0

        val out = ShortArray(total)
        for (i in 0 until total) {
            val s = buf[i] * norm * Short.MAX_VALUE
            out[i] = s.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return out
    }

    private suspend fun playPcm(data: ShortArray) {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val track = AudioTrack(
            attrs,
            format,
            data.size * 2, // bytes (16-bit)
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
        runCatching {
            track.write(data, 0, data.size)
            track.play()
            // Hold long enough for playback to finish, then release.
            val durMs = data.size.toLong() * 1000 / sampleRate + 180
            delay(durMs)
        }
        runCatching {
            track.stop()
            track.release()
        }
    }
}
