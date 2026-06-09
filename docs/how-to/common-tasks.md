# Common Tasks

## Change the backend URL

Edit `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", '"https://your-server.example/"')
```

Rebuild with `./gradlew assembleDebug`.

## Add a new playback mode

1. Add an entry to `HarmonizerMode` enum in `model/HarmonizerMode.kt`.
2. Implement `PlaybackEngine` in `player/PlaybackMode.kt`.
3. Register it in `engineFor()`.

## Run background render

In the player screen, tap **BACKGROUND**, select duration and quality, tap **RENDER**. The app polls `/api/background-render` status and shows a download button when done.

## Verify background play

1. Start a track in any mode.
2. Lock the screen.
3. Audio should continue; lockscreen shows track title + play/pause button.
4. Check `HarmonizerPlaybackService` is listed under Running Services in Android developer settings.
