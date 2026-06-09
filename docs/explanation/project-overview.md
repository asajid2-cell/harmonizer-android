# Project Overview

## What It Is

Harmonizer Android is a native Kotlin client for the Harmonizer music analysis backend at harmonizerlabs.cc. The server analyses audio — extracting beats, sections, loop candidates, and canon alignment — and the app plays the result in real time using beat-aware looping and layering modes.

The web version of Harmonizer works, but cannot keep audio alive when the screen locks. This Android app solves that with a `MediaSessionService` that runs as a foreground service with a persistent notification, integrating with the system lockscreen and Bluetooth controls.

## Design Choices

**Two ExoPlayer instances for Canon.** Canon is the same audio played twice with a beat offset. Two independent `ExoPlayer` instances are both prepared with the same stream URL, with the overlay seeked forward by `canonAlignment.offset` beats before playback starts. This avoids custom audio mixing.

**Beat loop on `Dispatchers.Default`.** The loop calculates the current beat position and whether to seek every ~50 ms. Running this on the main thread would block Compose recomposition. All `ExoPlayer` calls are marshalled back to `Dispatchers.Main` via `withContext`.

**MVVM + Hilt.** Standard architecture for testability. ViewModels are scoped to `NavBackStackEntry` for per-screen state, except `SharedTrackViewModel` which is scoped to the Activity to survive navigation transitions.

**Retrofit + in-memory CookieJar.** The backend uses Flask sessions. A simple in-memory `CookieJar` in `ApiModule` stores the session cookie per host. Native HTTP clients don't send `Origin` headers, so the server's CORS policy doesn't apply.

## Limitations And Non-Goals

- Requires live connection to `harmonizerlabs.cc` — no offline mode.
- No Play Store / F-Droid release yet; install via ADB.
- Experimental modes (Phase Shifter, Drone, Ambient, Pulse, Mosaic, Cascade, Temporal) have UI entries but engine stubs.
- No automated tests exist yet.
- Spotify source requires `yt-dlp` / Spotify credentials on the server.
- YouTube source depends on `yt-dlp` being installed on the VPS.
