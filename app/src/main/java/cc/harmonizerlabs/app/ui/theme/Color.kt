package cc.harmonizerlabs.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary neon palette — direct port from harmonizer CSS variables
val NeonCyan    = Color(0xFF00FFDE)
val NeonLime    = Color(0xFF00FF00)
val NeonMagenta = Color(0xFFFF00FF)
val NeonOrange  = Color(0xFFFF6600)
val NeonBlue    = Color(0xFF0000FF)

// Background / surfaces
val Black       = Color(0xFF000000)
val SurfaceDark = Color(0xFF03060F)
val SurfaceMid  = Color(0xFF0D0D1A)

// Text
val TextPrimary = Color(0xFFFFFFFF)
val TextMuted   = Color(0x8CFFFFFF)  // 55% white

// Glow-ready semi-transparent versions
val CyanGlow10  = Color(0x1A00FFDE)
val CyanGlow25  = Color(0x4000FFDE)
val CyanGlow45  = Color(0x7300FFDE)
val MagentaGlow = Color(0xCCFF00FF)  // 80%
val LimeGlow    = Color(0xCC00FF00)  // 80%

// Mode accent colors (one per mode for beat visualizer)
val BeatVerse   = Color(0xFF1A2A4A)
val BeatChorus  = Color(0xFF2A1A3A)
val BeatBridge  = Color(0xFF1A3A2A)
val BeatActive  = NeonCyan
val BeatDefault = Color(0xFF0A0F1A)

// Experimental mode overrides
val ExperimentalRed  = Color(0xFFFF2F2F)
val ExperimentalGold = Color(0xFFE6C35C)
