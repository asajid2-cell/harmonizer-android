# Getting Started

## Prerequisites

- Android Studio Hedgehog or newer
- Android SDK 34 (`sdk.dir` in `local.properties`)
- JDK 17+

## Steps

```bash
git clone https://github.com/asajid2-cell/harmonizer-android.git
cd harmonizer-android
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Expected Result

`BUILD SUCCESSFUL` — APK installs on any Android 8+ device or emulator.

Launch the app, pick Canon mode, upload a track, tap **TRANSFORM TRACK**, and playback begins with background audio and lockscreen controls working.

## Validation

```bash
./gradlew assembleDebug
# expect: BUILD SUCCESSFUL
# expect: app/build/outputs/apk/debug/app-debug.apk ~20 MB
```
