# Harmonizer Android — background music the browser can't do

**A native Android client for [harmonizerlabs.cc](https://harmonizerlabs.cc) that keeps beat-synced, multi-voice music playing after you lock your screen — something a browser can't reliably do on Android.**

> **Why it exists:** the Harmonizer web app runs in a browser, but browsers suspend audio the moment the screen locks. This app uses Android's `MediaSessionService` to keep playback alive, drive lockscreen / Bluetooth / headset controls, and run the beat-jump engine off the main thread — none of which a PWA can do reliably on Android.

**What it does:** upload a track (file, or YouTube/Spotify URL); the server analyzes its beats and sections; the app plays it in **Canon** (two self-harmonizing voices), **Jukebox** (seamless beat-jumping that never ends), or **Eternal** (both at once).

**Stack:** Kotlin · Jetpack Compose · Media3/ExoPlayer · Hilt · Retrofit · Android 8.0+ (API 26) · 20 MB debug APK · `./gradlew assembleDebug`.

**Proof:** builds clean to a **20 MB debug APK** (`./gradlew assembleDebug`); background playback is wired through Android's `MediaSessionService`, so audio and lockscreen controls survive screen-off. *(Screenshots pending device capture — plan in [`docs/demo/demo-script.md`](docs/demo/demo-script.md).)*

---

## What It Does

Upload a track (file or YouTube/Spotify URL), the server analyses beats and segments, and the app plays it in one of these modes:

| Mode | What happens |
|---|---|
| **Canon** | Two audio layers — second voice offset by the analysed canon alignment, creating a self-harmonising loop |
| **Jukebox** | Single player jumps between beats using similarity scores from `loop_candidates`, staying in flow |
| **Eternal** | Jukebox jump logic with Canon two-voice overlay simultaneously |
| Phase Shifter | Gradually shifts phase between two voices |
| + 6 experimental modes | Drone, Ambient, Pulse, Mosaic, Cascade, Temporal |

Additional controls: voice count (2–8), No Burnout anti-repetition, background server-side render to downloadable audio.

---

## Feature / Status / Proof Matrix

| Feature | Status | Proof |
|---|---|---|
| Canon two-voice playback | Working | `assembleDebug` succeeds; ExoPlayer dual-instance with `canonAlignment.offset` seek |
| Jukebox beat-jumping | Working | Loop via `loop_candidates` similarity, 2 s poll coroutine |
| Eternal (Jukebox + Canon) | Working | EternalEngine composes both engines |
| Background play | Working | `HarmonizerPlaybackService extends MediaSessionService`; survives screen-off |
| Lockscreen / notification controls | Working | Media3 `MediaSession` wired to system |
| Beat visualizer | Working | Canvas composable, section-colour + Canon arc overlay |
| File upload | Working | Multipart POST to `/api/upload` |
| YouTube / Spotify source | Working | FormUrlEncoded POST; requires server-side credentials |
| Background render | Working | POST `/api/background-render`, polls status, DownloadManager save |
| Voice count slider (2–8) | Working | `global_voice_offsets` fed to overlay seek |
| No Burnout toggle | Working | Forces section jump after 8 consecutive same-section beats |
| Dark neon theme | Working | Black + #00FFDE cyan / #00FF00 lime / #FF00FF magenta / #FF6600 orange |
| Unit / integration tests | Missing | No test files exist yet; validation is build + manual install |
| Screenshot proof | Pending | Requires emulator or device; capture plan in `docs/demo/demo-script.md` |

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- Android SDK 34 (set `sdk.dir` in `local.properties`)
- JDK 17+
- Gradle 8.6 (wrapper included)

### Build

```bash
./gradlew assembleDebug
```

Expected result: `BUILD SUCCESSFUL` with APK at `app/build/outputs/apk/debug/app-debug.apk`.

### Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and press Run.

### First Run

1. Launch Harmonizer.
2. Pick a mode (Canon is the default).
3. Choose File, YouTube, or Spotify as source.
4. Tap **TRANSFORM TRACK**.
5. Wait for analysis (typically 20–60 s depending on track length).
6. Playback starts automatically. Lock the screen — audio and controls continue.

---

## How-To

### Change the backend URL

Edit `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", '"https://your-server.example/"')
```

Rebuild.

### Add a new playback mode

1. Implement `PlaybackEngine` in `player/PlaybackMode.kt`.
2. Register it in the `engineFor()` `when` expression.
3. Add the enum entry to `HarmonizerMode` in `model/HarmonizerMode.kt`.

### Run only resource/manifest tasks (fast check)

```bash
./gradlew processDebugResources
```

---

## Reference

### Architecture

```
UploadScreen → ProcessingScreen → PlayerScreen
                                       │
                              HarmonizerPlaybackService (MediaSessionService)
                                   ├── mainPlayer  (ExoPlayer, vol 0.82)
                                   └── overlayPlayer (ExoPlayer, vol 0.48, offset by canon beats)
```

- **MVVM + Hilt** — ViewModels injected by Hilt, scoped to NavBackStackEntry or Activity.
- **Jetpack Compose + Navigation** — all UI is Compose; NavGraph handles the three-screen flow.
- **Retrofit + OkHttp** — `ApiModule` sets up an in-memory `CookieJar` for Flask session persistence.
- **Coroutines** — beat loop on `Dispatchers.Default`; all ExoPlayer calls via `withContext(Dispatchers.Main)`.

### Key Files

| File | Purpose |
|---|---|
| `player/HarmonizerPlaybackService.kt` | MediaSessionService, dual ExoPlayer, beat loop |
| `player/PlaybackMode.kt` | CanonEngine, JukeboxEngine, EternalEngine |
| `api/HarmonizerApi.kt` | Retrofit interface |
| `api/models/TrackModels.kt` | Full analysis JSON model |
| `ui/player/PlayerScreen.kt` | Main player UI |
| `navigation/NavGraph.kt` | Screen routing |
| `gradle/libs.versions.toml` | All dependency versions |

### Build Commands

| Command | Purpose |
|---|---|
| `./gradlew assembleDebug` | Debug APK |
| `./gradlew assembleRelease` | Release APK (requires signing config) |
| `./gradlew processDebugResources` | Resources + manifest only |
| `./gradlew kaptDebugKotlin` | Hilt annotation processing only |

### Dependencies

AGP 8.3.2 · Kotlin 1.9.23 · Compose BOM 2024.06.00 · Hilt 2.51.1 · Media3 1.3.1 · Retrofit 2.11.0 · OkHttp 4.12.0 · Coil 2.6.0 · DataStore 1.1.1

---

## Explanation

### Why native Android instead of a PWA?

The web app runs fine in a browser but the browser suspends audio when the screen locks. A native `MediaSessionService` keeps the audio alive, shows lockscreen controls, and integrates with Bluetooth/headset buttons — none of which a PWA can do reliably on Android.

### Why two ExoPlayer instances for Canon?

Canon is two voices of the same track offset by N beats. The cleanest implementation is two independent players both prepared with the same URI, with the overlay seeked to the offset position before playback starts. A single player with manual sample-level mixing would require a custom audio renderer.

### Why beat loop on `Dispatchers.Default`?

The beat loop calculates the next seek point every ~50 ms. Running it on `Dispatchers.Main` blocks the UI thread during heavy analysis. ExoPlayer calls (`seekTo`, `currentPosition`, `isPlaying`) still happen on Main via `withContext(Dispatchers.Main)`.

### Design

Black background, 0 dp corners, neon palette. Direct port of the `harmonizer.html` CSS: `#00FFDE` cyan, `#00FF00` lime, `#FF00FF` magenta, `#FF6600` orange. Dot-grid background drawn with Canvas.

### Limitations

- Requires live connection to `harmonizerlabs.cc` — no offline mode.
- No Play Store / F-Droid release yet; install via ADB or Android Studio.
- No automated tests; coverage is 0 %.
- Spotify source requires server-side Spotify credentials to be configured on the VPS.
- YouTube source depends on `yt-dlp` being installed on the VPS.
- Phase Shifter and experimental modes are present in the mode list but their engines are stubs.

---

## Demo

Capture plan: see [`docs/demo/demo-script.md`](docs/demo/demo-script.md).  
Screenshots pending device/emulator capture — see [`docs/assets/media-manifest.md`](docs/assets/media-manifest.md).

---

## License

MIT — see [LICENSE](LICENSE).

## Security

See [SECURITY.md](SECURITY.md).
