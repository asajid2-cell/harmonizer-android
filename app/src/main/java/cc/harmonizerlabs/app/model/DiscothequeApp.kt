package cc.harmonizerlabs.app.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import cc.harmonizerlabs.app.R

/**
 * One launcher tile on the Internet Discotheque home — a faithful port of the desktop icon grid
 * in `index.html`. `iconRes` is the real mascot PNG (bundled from the web assets); `glyph` is a
 * fallback unicode glyph for the apps that used the pixelart-icons font on the web.
 *
 * `primary`/`secondary` are the per-app `--icon-primary`/`--icon-secondary` CSS accent colours.
 * Harmonizer is the only NATIVE destination; everything else opens its web page.
 */
data class DiscothequeApp(
    val id: String,
    val label: String,
    @DrawableRes val iconRes: Int? = null,
    val glyph: String? = null,
    val primary: Color,
    val secondary: Color,
    val url: String? = null,        // null => native (Harmonizer)
    val isHarmonizer: Boolean = false,
)

// Order + accents lifted verbatim from index.html `.icon-frame--*` variables.
val DiscothequeApps: List<DiscothequeApp> = listOf(
    DiscothequeApp("home", "Home", glyph = "⌂",
        primary = Color(0xFF6DFFD2), secondary = Color(0xFF4F7BFF),
        url = "https://harmonizerlabs.cc/"),
    DiscothequeApp("showcase", "Showcase", glyph = "▦",
        primary = Color(0xFF8AB4FF), secondary = Color(0xFFFFB347),
        url = "https://harmonizerlabs.cc/projects"),
    DiscothequeApp("squeezebox", "Squeezebox Cloud", iconRes = R.drawable.ic_app_squeezebox,
        primary = Color(0xFFFF5F83), secondary = Color(0xFFFFFFFF),
        url = "https://harmonizerlabs.cc/cloud-squeeze/"),
    DiscothequeApp("cameraroom", "CameraRoom", iconRes = R.drawable.ic_app_cameraroom,
        primary = Color(0xFFFFD0E0), secondary = Color(0xFFFF5C8A),
        url = "https://harmonizerlabs.cc/CameraRoom/"),
    DiscothequeApp("nightlibrary", "Night Library", iconRes = R.drawable.ic_app_nightlibrary,
        primary = Color(0xFFFF9FD2), secondary = Color(0xFF7AFFFF),
        url = "https://harmonizerlabs.cc/night-library.html"),
    DiscothequeApp("eldrichify", "Eldrichify", iconRes = R.drawable.ic_app_eldrichify,
        primary = Color(0xFFFFD86B), secondary = Color(0xFF7BD7FF),
        url = "https://harmonizerlabs.cc/eldrichify.html"),
    DiscothequeApp("talk", "Talk to Disco-teque", iconRes = R.drawable.ic_app_talk,
        primary = Color(0xFF8FE7FF), secondary = Color(0xFFFF7AB8),
        url = "https://harmonizerlabs.cc/talk-to-disco-teque.html"),
    DiscothequeApp("codesniff", "CodeSniff", iconRes = R.drawable.ic_app_codesniff,
        primary = Color(0xFF8FFF5A), secondary = Color(0xFF5AF0FF),
        url = "https://harmonizerlabs.cc/codesniff.html"),
    DiscothequeApp("radio", "Radio", glyph = "⦿",
        primary = Color(0xFFFF9F5A), secondary = Color(0xFFFFD65A),
        url = "https://plaza.one/"),
    DiscothequeApp("ourspace", "OurSpace", iconRes = R.drawable.ic_app_ourspace,
        primary = Color(0xFF9A7BFF), secondary = Color(0xFF53FFD6),
        url = "https://harmonizerlabs.cc/ourspace.html"),
    DiscothequeApp("sand", "Sand Sim", iconRes = R.drawable.ic_app_sand,
        primary = Color(0xFFF8D27A), secondary = Color(0xFFFF7A7A),
        url = "https://harmonizerlabs.cc/sand.html"),
    DiscothequeApp("venpod", "VENPOD", iconRes = R.drawable.ic_app_venpod,
        primary = Color(0xFFFF6B6B), secondary = Color(0xFF4ECDC4),
        url = "https://harmonizerlabs.cc/venpod.html"),
    DiscothequeApp("notebook", "Notebook", glyph = "▤",
        primary = Color(0xFFA5FFA5), secondary = Color(0xFF7AC7FF),
        url = "https://harmonizerlabs.cc/notebook.html"),
    DiscothequeApp("quickref", "Quick Reference", glyph = "☰",
        primary = Color(0xFFFFC3FF), secondary = Color(0xFF7AFFFF),
        url = "https://harmonizerlabs.cc/cheatsheets/"),
)

// The featured native tile.
val HarmonizerApp = DiscothequeApp(
    "harmonizer", "Harmonizer", iconRes = R.drawable.ic_app_harmonizer,
    primary = Color(0xFF5CFF8A), secondary = Color(0xFFFF7AC8),
    url = null, isHarmonizer = true,
)
