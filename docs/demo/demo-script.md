# Demo Script

## Story

A viewer should understand: this is a native Android app that takes any song, analyses its beat structure, and plays it back as a self-harmonising loop — even with the screen off.

## 30-Second Flow

1. Open app → neon dark UI loads (Upload screen)
2. Select **Canon** mode
3. Paste a YouTube URL → tap **TRANSFORM TRACK**
4. Progress screen shows analysis (15–45 s)
5. Player opens → beat visualizer shows coloured beat squares
6. Lock screen → audio continues, lockscreen card shows play/pause
7. Unlock → return to player, tap **BACKGROUND** → select 5 min → **RENDER** → download button appears

## Capture Plan

| Asset | Destination | Workflow / State | Method | Privacy Checks | Validation | README Placement | Caption |
|---|---|---|---|---|---|---|---|
| `android-upload-screen.png` | `docs/assets/` | Upload screen, Canon selected | Android emulator screenshot | No personal data | Matches `UploadScreen.kt` | README Demo | Upload screen with neon dark theme |
| `android-player-screen.png` | `docs/assets/` | Player active, beat visualizer lit | Android emulator screenshot | No personal data | Matches `PlayerScreen.kt` | README Demo | Beat visualizer during Canon playback |
| `android-lockscreen.png` | `docs/assets/` | Screen locked, media notification visible | Android emulator screenshot | No personal data, no private apps in recents | Validates background play | README feature matrix | Lockscreen media controls |
| `demo-workflow.webm` | `docs/assets/` | Full upload → playback → lock screen flow | Android emulator screen record | No personal URLs or emails visible | Audio continues after lock | README Demo | 30-second demo of Canon mode |
