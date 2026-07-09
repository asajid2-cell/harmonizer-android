@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package cc.harmonizerlabs.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import cc.harmonizerlabs.app.R

// Manrope — the web app's primary typeface (Google Fonts: Manrope 400/600/700/800).
// Bundled as a single variable font; each weight is realised via the wght axis.
// minSdk is 26, so FontVariation settings apply on every supported device.
val ManropeFamily = FontFamily(
    Font(
        R.font.manrope_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.manrope_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.manrope_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.manrope_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
    Font(
        R.font.manrope_variable,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800)),
    ),
    Font(
        R.font.manrope_variable,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(FontVariation.weight(800)),
    ),
)

// IBM Plex Mono — the web's monospace face, used for numeric/tabular readouts (the player
// timer + beat counts) and the ASCII wave dividers.
val PlexMonoFamily = FontFamily(
    Font(R.font.ibm_plex_mono_regular, weight = FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium,  weight = FontWeight.Medium),
)
