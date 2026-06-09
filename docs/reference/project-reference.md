# Project Reference

## Build Commands

| Command | Purpose | Expected result |
|---|---|---|
| `./gradlew assembleDebug` | Debug APK | `BUILD SUCCESSFUL`, APK ~20 MB |
| `./gradlew assembleRelease` | Release APK | Requires signing config in `build.gradle.kts` |
| `./gradlew processDebugResources` | Resources + manifest | Fast check without full Kotlin compile |
| `./gradlew kaptDebugKotlin` | Hilt annotation processing | Generates DI factories |

## Feature / Status / Proof / Demo / Docs Matrix

| Feature | Status | Proof | Demo/Capture | Docs |
|---|---|---|---|---|
| Canon mode | Working | Build succeeds, dual ExoPlayer | Pending screenshot | README |
| Jukebox mode | Working | Build succeeds, beat loop | Pending screenshot | README |
| Eternal mode | Working | Build succeeds | Pending screenshot | README |
| Background play | Working | MediaSessionService | Manual device test | README |
| Beat visualizer | Working | Canvas composable | Pending screenshot | README |
| Background render | Working | API polling + DownloadManager | Manual test | README |
| Unit tests | Missing | — | — | — |

## Configuration

| Key | Location | Default | Notes |
|---|---|---|---|
| `BASE_URL` | `app/build.gradle.kts` `buildConfigField` | `https://harmonizerlabs.cc/` | Change to point at a local server |
| `sdk.dir` | `local.properties` | set by Android Studio | Never commit |

## API Endpoints Used

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/upload` | POST multipart | Upload audio file |
| `/api/process` | POST form | Process from URL source |
| `/api/status/{jobId}` | GET | Poll job status |
| `/data/{trackId}.json` | GET | Fetch analysis JSON |
| `/api/background-render` | POST | Start background render |
| `/api/render-status/{jobId}` | GET | Poll render status |
