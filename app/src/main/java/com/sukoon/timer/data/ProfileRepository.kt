package com.sukoon.timer.data

import android.content.Context
import com.sukoon.timer.model.TimerProfile
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Persists routines as a JSON array in SharedPreferences.
 * Tiny payload, so plain synchronous reads/writes are fine.
 */
class ProfileRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(TimerProfile.serializer())

    fun load(): List<TimerProfile> {
        val raw = prefs.getString(KEY, null)
        if (raw == null) {
            val seeded = starters()
            save(seeded)
            return seeded
        }
        return runCatching { json.decodeFromString(serializer, raw) }
            .getOrElse { starters() }
    }

    fun save(profiles: List<TimerProfile>) {
        prefs.edit()
            .putString(KEY, json.encodeToString(serializer, profiles))
            .apply()
    }

    /** A couple of gentle starter routines so the app is useful on first launch. */
    private fun starters(): List<TimerProfile> = listOf(
        TimerProfile(
            id = "night",
            name = "Night stretch",
            rounds = 10,
            workSeconds = 120,
            restSeconds = 2,
            leadInSeconds = 5,
            countdownCues = true,
        ),
    )

    private companion object {
        const val PREFS = "sukoon_store"
        const val KEY = "profiles_json"
    }
}
