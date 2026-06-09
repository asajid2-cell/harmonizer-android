# Contributing

## Prerequisites

- Android Studio Hedgehog or newer
- Android SDK 34, minSdk 26
- JDK 17+

## Build

```bash
./gradlew assembleDebug
```

Install on a connected device or emulator:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Architecture

MVVM + Hilt + Jetpack Compose. Key packages:

- `player/` — `HarmonizerPlaybackService` (MediaSessionService) + playback engines
- `ui/` — screens and Compose components
- `api/` — Retrofit interface + data models
- `navigation/` — NavGraph

All ExoPlayer calls must happen on the main thread. The beat loop runs on `Dispatchers.Default`; use `withContext(Dispatchers.Main)` before any player call.

## Pull Requests

- One logical change per PR
- Keep Kotlin idiomatic — no Java interop cruft
- Do not commit `local.properties`, `*.jks`, or build output

## Backend

The app routes to `https://harmonizerlabs.cc`. The `BASE_URL` is set in `app/build.gradle.kts` as a `BuildConfig` field.
