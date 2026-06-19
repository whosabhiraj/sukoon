<h1 align="center">Sukoon</h1>

<p align="center">A gentle, beautiful interval timer for Android.</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-9DBE9C">
  <img alt="Language" src="https://img.shields.io/badge/Kotlin-2.0-B9A7E0">
  <img alt="UI" src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-F0B6C6">
  <img alt="minSdk" src="https://img.shields.io/badge/minSdk-26-E7A98C">
</p>

---

*Sukoon* (سکون / सुकून) means calm, tranquillity. It's a soft, distraction-free timer for any
**repeating, timed routine** — set how many rounds, how long each one lasts, and the rest
interval between them, then let distinct chimes keep time so you never need to watch the screen.

Good for stretching, mobility and physiotherapy sets, breathwork and meditation, HIIT and
strength intervals, study/focus sprints, or anything that repeats on a clock. Designed to be
elegant and effortless, in a palette of lavender, sage, baby pink and a soft clay peach.

## Features

- **Configurable routines** — number of rounds, work duration, rest interval, and an optional
  *get-ready* lead-in. Use the large +/− steppers, or tap a value to type an exact number
  (e.g. 3 seconds).
- **Saved profiles** — store, edit and reuse named configurations. They persist on-device, and
  a couple of gentle starters are seeded on first launch.
- **Soft, distinct chimes** for each moment — warm wooden "dun" mallet tones (marimba/kalimba-like)
  that differ by pitch and shape, not by counting beeps:
  | Cue | Character |
  |-----|-----------|
  | Get ready | two even low notes |
  | Begin a round | a gently rising "dun dun dun" |
  | Rest / reposition | two soft descending notes |
  | Final 3 seconds | a single soft tick *(optional, per routine)* |
  | All done | an ascending run that resolves |

  Samples are rendered offline by a small synthesiser (`tools/generate_chimes.js` — warm harmonic
  partials, heavy low-pass, a touch of room) into `res/raw`, then played with `SoundPool` (the WAVs
  are kept uncompressed and warmed up on load) for reliable, overlap-friendly playback.
- **Soothing motion** — screens slide between one another; a drifting pastel backdrop; a ring that
  eases as the round counts down (with a glowing tip); a "breathing" pulse during rest; and a
  rippling bloom on completion. On each phase change the new colour **grows outward from the centre**
  of the screen, which stays awake for the whole session.
- **Type** — Inter throughout (a clean, light, San-Francisco-like sans), with a light weight for
  the big timer.

## Tech stack

Native **Kotlin + Jetpack Compose + Material 3**, single-Activity, no third-party UI libraries.

Native was chosen deliberately: the app's one job is to chime at *exactly* the right second, even
when the phone is set down and the screen would normally dim. Native handles that reliably
(precise wall-clock timing plus keeping the screen awake) where a web app's timers get throttled
the moment the screen sleeps. Compose also delivers smooth, soothing animations, and everything
is drawn in-app so the build stays lightweight.

| | |
|---|---|
| Min / target SDK | 26 / 34 |
| Kotlin | 2.0.20 |
| Compose BOM | 2024.09.03 |
| Android Gradle Plugin | 8.6.1 |
| Gradle | 8.9 |
| Persistence | SharedPreferences + `kotlinx.serialization` |
| Type | Inter (bundled variable font) |
| Audio | offline-rendered WAV samples · `SoundPool` |

## Getting started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (bundles JDK 17, the Android SDK and
  Gradle). That single install is all you need.

### Build & run

```bash
git clone https://github.com/whosabhiraj/sukoon sukoon
cd sukoon
```

1. **Open** the project folder in Android Studio and trust it.
2. Let the first **Gradle sync** finish — it downloads Gradle 8.9 and the dependencies, and
   generates `local.properties` and the Gradle wrapper.
3. Press **▶ Run** on either an emulator (Device Manager → add a Pixel) or a connected device
   with *Developer options → USB debugging* enabled.

Prefer the command line? Once Android Studio has generated the wrapper:

```bash
./gradlew assembleDebug      # build a debug APK
./gradlew installDebug       # build and install on a connected device
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk` — you can share that file
to sideload it on any device (allow "install from this source" once). The app then runs fully
offline.

## Customisation

| What | Where |
|------|-------|
| App name | `app/src/main/res/values/strings.xml` (`app_name`) |
| Palette & per-phase colours | `app/src/main/java/com/sukoon/timer/ui/theme/Theme.kt` |
| Fonts | `app/src/main/res/font/` + `app/.../ui/theme/Fonts.kt` |
| Chime tones | edit `tools/generate_chimes.js`, then run `node tools/generate_chimes.js` |
| Stepper ranges & defaults | `app/src/main/java/com/sukoon/timer/ui/EditScreen.kt` |

## Project structure

```
app/src/main/java/com/sukoon/timer/
├── MainActivity.kt            entry point
├── model/                     TimerProfile, Phase, time formatting
├── data/ProfileRepository.kt  saves/loads routines (SharedPreferences + JSON)
├── sound/ChimePlayer.kt       plays the chime samples (SoundPool)
├── timer/TimerEngine.kt       ViewModel: navigation + the live countdown
└── ui/                        Compose screens, theme, components, animated background
```

## Possible feature additions

- **Foreground service** so a session keeps perfect time and chimes even with the screen
      locked or the app backgrounded (today the screen is kept awake while the timer is open).
- Gentle **haptic** feedback alongside the chimes.
- Optional spoken cues.
- Reorder and duplicate saved profiles.
