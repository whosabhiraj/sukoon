package com.sukoon.timer.timer

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sukoon.timer.data.ProfileRepository
import com.sukoon.timer.model.Phase
import com.sukoon.timer.model.TimerProfile
import com.sukoon.timer.sound.ChimePlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.ceil

/** Which screen is showing. Kept here so transitions can be animated centrally. */
sealed interface Screen {
    data object Home : Screen
    data class Edit(val profile: TimerProfile?) : Screen
    data object Run : Screen
}

/**
 * Single source of truth for navigation, the saved routines, and the live countdown.
 *
 * The countdown is driven by wall-clock time (a target end timestamp), not by decrementing
 * a counter, so it stays accurate even if a frame is dropped or the app is briefly paused.
 */
class TimerEngine(app: Application) : AndroidViewModel(app) {

    private val repo = ProfileRepository(app)
    private val chimes = ChimePlayer(app)

    // ---- Saved routines & navigation -------------------------------------------------------
    var profiles by mutableStateOf(repo.load()); private set
    var screen by mutableStateOf<Screen>(Screen.Home); private set

    // ---- Live session state (observed by the running screen) -------------------------------
    var phase by mutableStateOf(Phase.Idle); private set
    var round by mutableStateOf(0); private set
    var totalRounds by mutableStateOf(0); private set
    var remaining by mutableStateOf(0); private set   // whole seconds left in the phase
    var phaseTotal by mutableStateOf(1); private set  // phase length in seconds (>=1)
    var paused by mutableStateOf(false); private set

    private var activeProfile: TimerProfile? = null
    private var job: Job? = null
    private var skipRequested = false

    fun newId(): String = UUID.randomUUID().toString()

    // ---- Navigation ------------------------------------------------------------------------
    fun goHome() {
        stop()
        screen = Screen.Home
    }

    fun newProfile() { screen = Screen.Edit(null) }
    fun editProfile(p: TimerProfile) { screen = Screen.Edit(p) }

    fun saveProfile(p: TimerProfile) {
        val list = profiles.toMutableList()
        val idx = list.indexOfFirst { it.id == p.id }
        if (idx >= 0) list[idx] = p else list.add(p)
        profiles = list
        repo.save(list)
        screen = Screen.Home
    }

    fun deleteProfile(p: TimerProfile) {
        val list = profiles.filterNot { it.id == p.id }
        profiles = list
        repo.save(list)
    }

    // ---- Running ---------------------------------------------------------------------------
    fun start(p: TimerProfile) {
        activeProfile = p
        totalRounds = p.rounds
        round = 0
        paused = false
        skipRequested = false
        screen = Screen.Run
        job?.cancel()
        job = viewModelScope.launch { runSequence(p) }
    }

    fun repeat() = activeProfile?.let { start(it) }

    fun togglePause() { paused = !paused }

    /** End the current phase immediately and move on. */
    fun skip() {
        skipRequested = true
        paused = false
    }

    fun stop() {
        job?.cancel()
        job = null
        paused = false
        skipRequested = false
        phase = Phase.Idle
    }

    override fun onCleared() {
        job?.cancel()
        chimes.release()
    }

    private suspend fun runSequence(p: TimerProfile) {
        if (p.leadInSeconds > 0) {
            chimes.play(ChimePlayer.Cue.Ready)
            runPhase(Phase.GetReady, p.leadInSeconds, p)
        }
        for (r in 1..p.rounds) {
            round = r
            chimes.play(ChimePlayer.Cue.WorkStart)
            runPhase(Phase.Work, p.workSeconds, p)
            if (r < p.rounds && p.restSeconds > 0) {
                chimes.play(ChimePlayer.Cue.RestStart)
                runPhase(Phase.Rest, p.restSeconds, p)
            }
        }
        phase = Phase.Done
        remaining = 0
        chimes.play(ChimePlayer.Cue.Complete)
    }

    private suspend fun runPhase(ph: Phase, seconds: Int, p: TimerProfile) {
        phase = ph
        phaseTotal = seconds.coerceAtLeast(1)
        remaining = seconds
        var endAt = System.currentTimeMillis() + seconds * 1000L
        var lastTick = -1

        while (true) {
            if (skipRequested) {
                skipRequested = false
                return
            }
            if (paused) {
                val pauseStart = System.currentTimeMillis()
                while (paused) {
                    if (skipRequested) break
                    delay(100)
                }
                // Push the finish line forward by however long we were paused.
                endAt += System.currentTimeMillis() - pauseStart
                continue
            }

            val msLeft = endAt - System.currentTimeMillis()
            val secLeft = ceil(msLeft / 1000.0).toInt().coerceAtLeast(0)
            remaining = secLeft

            if (p.countdownCues && secLeft in 1..3 && secLeft != lastTick) {
                lastTick = secLeft
                chimes.play(ChimePlayer.Cue.Tick)
            }

            if (msLeft <= 0) return
            delay(60)
        }
    }
}
