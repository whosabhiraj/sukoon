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
            id = "starter-stretch",
            name = "Morning Stretch",
            rounds = 8,
            workSeconds = 120,
            restSeconds = 30,
            leadInSeconds = 5,
        ),
        TimerProfile(
            id = "starter-gentle",
            name = "Gentle Routine",
            rounds = 5,
            workSeconds = 150,
            restSeconds = 45,
            leadInSeconds = 5,
        ),
    )

    private companion object {
        const val PREFS = "sukoon_store"
        const val KEY = "profiles_json"
    }
}
