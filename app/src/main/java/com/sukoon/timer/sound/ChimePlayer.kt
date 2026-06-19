package com.sukoon.timer.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.sukoon.timer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * Plays the pre-rendered chime samples in `res/raw` via [SoundPool].
 *
 * SoundPool is built for short, overlapping cues and is far more reliable than spinning up a
 * fresh AudioTrack each time (which intermittently dropped sounds). Samples are decoded once at
 * start-up; [play] waits briefly if a cue somehow fires before its sample has finished loading.
 *
 * Every cue is a soft, warm chime — they differ by pitch and character (low and grounding for
 * ready, bright and lifted for a round, mellow and descending for rest, a gentle cascade for the
 * finish), not by counting beeps.
 */
class ChimePlayer(context: Context) {

    enum class Cue { Ready, WorkStart, RestStart, Tick, Complete }

    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        .build()

    private val ready = Collections.synchronizedSet(mutableSetOf<Int>())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val ids: Map<Cue, Int>

    init {
        val app = context.applicationContext
        pool.setOnLoadCompleteListener { sp, sampleId, status ->
            if (status == 0) {
                ready.add(sampleId)
                // Silent warm-up: primes the audio pipeline so the first audible cue isn't
                // swallowed (the main cause of "no sound" the first time a session starts).
                sp.play(sampleId, 0f, 0f, 0, 0, 1f)
            }
        }
        ids = mapOf(
            Cue.Ready to pool.load(app, R.raw.chime_ready, 1),
            Cue.WorkStart to pool.load(app, R.raw.chime_work, 1),
            Cue.RestStart to pool.load(app, R.raw.chime_rest, 1),
            Cue.Tick to pool.load(app, R.raw.chime_tick, 1),
            Cue.Complete to pool.load(app, R.raw.chime_done, 1),
        )
    }

    fun play(cue: Cue) {
        val id = ids[cue] ?: return
        scope.launch {
            var waited = 0
            while (id !in ready && waited < 1500) {
                delay(20)
                waited += 20
            }
            pool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        pool.release()
    }
}
