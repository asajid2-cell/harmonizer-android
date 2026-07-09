package cc.harmonizerlabs.app.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.RectangleShape
import androidx.hilt.navigation.compose.hiltViewModel
import cc.harmonizerlabs.app.model.*
import cc.harmonizerlabs.app.ui.components.*
import cc.harmonizerlabs.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onTrackReady: (trackId: String, mode: HarmonizerMode) -> Unit,
    onBack: () -> Unit = {},
    viewModel: UploadViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Navigate when a track completes
    LaunchedEffect(state.completedTrackId) {
        state.completedTrackId?.let { id ->
            onTrackReady(id, state.selectedMode)
            viewModel.clearCompletedTrack()
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onFilePicked(context, it) } }

    val file2Launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onAudio2Picked(context, it) } }

    var songSearch by remember { mutableStateOf("") }

    if (state.showSongList) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeSongList(); songSearch = "" },
            containerColor   = SurfaceDark,
            scrimColor       = Black.copy(alpha = 0.75f),
            shape            = RectangleShape,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    "SONG LIBRARY",
                    style    = MaterialTheme.typography.labelLarge,
                    color    = NeonCyan,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                NeonTextField(
                    value         = songSearch,
                    onValueChange = { songSearch = it },
                    placeholder   = "SEARCH SONGS...",
                )
                Spacer(Modifier.height(12.dp))
                when {
                    state.isSongsLoading -> {
                        Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            Text("LOADING...", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                        }
                    }
                    state.songsError != null -> {
                        Text(
                            text     = state.songsError ?: "",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = NeonOrange,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }
                    state.cachedSongs.isEmpty() -> {
                        Text(
                            "No uploaded songs found. Upload a track to get started.",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = TextMuted,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }
                    else -> {
                        val filtered = state.cachedSongs.filter { song ->
                            songSearch.isBlank() ||
                            (song.title ?: "").contains(songSearch, ignoreCase = true) ||
                            (song.artist ?: "").contains(songSearch, ignoreCase = true)
                        }
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                            items(filtered) { song ->
                                val durationStr = song.duration?.let { d ->
                                    val mins = (d / 60).toInt()
                                    val secs = (d % 60).toInt()
                                    "$mins:${secs.toString().padStart(2, '0')}"
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.closeSongList()
                                            songSearch = ""
                                            onTrackReady(song.trackId, state.selectedMode)
                                        }
                                        .padding(vertical = 12.dp),
                                ) {
                                    Text("♫ ", color = NeonCyan, style = MaterialTheme.typography.bodyMedium)
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            song.title ?: "Unknown Track",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = TextPrimary,
                                        )
                                        Text(
                                            buildString {
                                                append(song.artist ?: "Unknown Artist")
                                                if (durationStr != null) append(" • $durationStr")
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted,
                                        )
                                    }
                                    Text("►", color = NeonCyan, style = MaterialTheme.typography.bodyMedium)
                                }
                                HorizontalDivider(color = NeonLime.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            NeonBar()

            // ── Back to Internet Discotheque ───────────────────────────────────
            Text(
                text = "← RETURN TO INTERNET DISCOTHEQUE",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier
                    .padding(start = 16.dp, top = 12.dp)
                    .clickable { onBack() }
                    .padding(vertical = 6.dp, horizontal = 2.dp),
            )

            // ── Hero ──────────────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            LabHero()

            Spacer(Modifier.height(24.dp))

            // ── LOAD A TRACK heading ──────────────────────────────────────────
            Text(
                "LOAD A TRACK",
                style    = MaterialTheme.typography.titleLarge,
                color    = NeonLime,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Text(
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
                style     = MaterialTheme.typography.titleMedium,
                color     = NeonLime.copy(alpha = 0.5f),
                fontFamily = PlexMonoFamily,
                maxLines  = 1,
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(14.dp))

            // ── Mode panel ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(3.dp, NeonLime)
                    .background(Black)
                    .padding(16.dp),
            ) {
                Text(
                    "ENGINE",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(12.dp))

                // Non-lazy 2-col grid — avoids nested-scroll conflict with outer Column
                ModeGrid(
                    modes    = PrimaryModes,
                    selected = state.selectedMode,
                    onSelect = { viewModel.selectMode(it) },
                )

                if (state.showExperimentalModes) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonMagenta.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    ModeGrid(
                        modes    = ExperimentalModes,
                        selected = state.selectedMode,
                        onSelect = { viewModel.selectMode(it) },
                    )
                }

                Spacer(Modifier.height(8.dp))
                NeonButton(
                    label       = if (state.showExperimentalModes) "HIDE EXPERIMENTAL" else "+ EXPERIMENTAL MODES",
                    onClick     = { viewModel.toggleExperimental() },
                    borderColor = NeonMagenta.copy(alpha = 0.5f),
                    modifier    = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~",
                style     = MaterialTheme.typography.titleMedium,
                color     = NeonLime.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(16.dp))

            // ── Source selector ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(3.dp, NeonLime)
                    .background(Black)
                    .padding(16.dp),
            ) {
                Text(
                    "SOURCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonLime,
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    UploadSource.entries.forEach { src ->
                        NeonButton(
                            label       = src.name,
                            onClick     = { viewModel.selectSource(src) },
                            active      = state.source == src,
                            borderColor = NeonCyan,
                            modifier    = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                when (state.source) {
                    UploadSource.FILE -> {
                        // Primary audio file picker
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .border(
                                    width = 2.dp,
                                    color = NeonCyan.copy(alpha = if (state.selectedFileUri != null) 1f else 0.4f),
                                    shape = RoundedCornerShape(0.dp),
                                )
                                .background(SurfaceDark)
                                .clickable { fileLauncher.launch("audio/*") },
                        ) {
                            Text(
                                text  = state.selectedFileName ?: "TAP TO PICK AUDIO FILE",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (state.selectedFileUri != null) NeonCyan else TextMuted,
                                textAlign = TextAlign.Center,
                            )
                        }
                        // Second track picker — only for Autoharmonizer
                        if (state.selectedMode.key == "autoharmonizer") {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "SECOND TRACK (HARMONY SOURCE)",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonMagenta,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .border(
                                        width = 2.dp,
                                        color = NeonMagenta.copy(alpha = if (state.audio2FileUri != null) 1f else 0.4f),
                                        shape = RoundedCornerShape(0.dp),
                                    )
                                    .background(SurfaceDark)
                                    .clickable { file2Launcher.launch("audio/*") },
                            ) {
                                Text(
                                    text  = state.audio2FileName ?: "TAP TO PICK SECOND AUDIO FILE",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (state.audio2FileUri != null) NeonMagenta else TextMuted,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    UploadSource.YOUTUBE -> NeonTextField(
                        value         = state.urlInput,
                        onValueChange = { viewModel.setUrlInput(it) },
                        placeholder   = "YOUTUBE URL",
                    )
                    UploadSource.SPOTIFY -> NeonTextField(
                        value         = state.urlInput,
                        onValueChange = { viewModel.setUrlInput(it) },
                        placeholder   = "SPOTIFY TRACK URL",
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Metadata
                NeonTextField(
                    value         = state.title,
                    onValueChange = { viewModel.setTitle(it) },
                    placeholder   = "TITLE (OPTIONAL)",
                )
                Spacer(Modifier.height(8.dp))
                NeonTextField(
                    value         = state.artist,
                    onValueChange = { viewModel.setArtist(it) },
                    placeholder   = "ARTIST (OPTIONAL)",
                )

                Spacer(Modifier.height(16.dp))

                NeonCtaButton(
                    label   = if (state.isSubmitting) "PROCESSING..." else "TRANSFORM TRACK",
                    onClick = { viewModel.submit(context) },
                    enabled = !state.isSubmitting,
                )
                Spacer(Modifier.height(8.dp))
                NeonButton(
                    label       = "VIEW SONGS",
                    onClick     = { viewModel.openSongList() },
                    borderColor = NeonCyan,
                    modifier    = Modifier.fillMaxWidth(),
                )
            }

            // ── Error ─────────────────────────────────────────────────────────
            state.errorMessage?.let { err ->
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(2.dp, NeonOrange)
                        .background(Black)
                        .padding(12.dp)
                        .clickable { viewModel.clearError() },
                ) {
                    Text(err, style = MaterialTheme.typography.bodySmall, color = NeonOrange)
                }
            }

            // ── Recent tracks ─────────────────────────────────────────────────
            if (state.recentTracks.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .neonGlow(NeonLime, glowRadius = 8.dp, intensity = 0.4f)
                        .border(2.dp, NeonLime)
                        .background(Black)
                        .padding(16.dp),
                ) {
                    Text("RECENT", style = MaterialTheme.typography.labelMedium, color = NeonLime)
                    Spacer(Modifier.height(8.dp))
                    state.recentTracks.forEach { track ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTrackReady(track.trackId, track.mode) }
                                .padding(vertical = 8.dp),
                        ) {
                            Text("♫ ", color = NeonCyan, style = MaterialTheme.typography.bodyMedium)
                            Column(Modifier.weight(1f)) {
                                Text(track.title, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                                Text(track.artist, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Text(
                                track.mode.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonMagenta,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("►", color = NeonCyan, style = MaterialTheme.typography.bodyMedium)
                        }
                        HorizontalDivider(color = NeonLime.copy(alpha = 0.12f))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/* ── Harmonizer Lab hero — faithful port of harmonizer.html `.hero` ────────────── */

private data class Callout(val mode: String, val effect: String)

private val LabCallouts = listOf(
    Callout("CANON MODE", "MIRROR VOICES"),
    Callout("JUKEBOX MODE", "JUMP AND NEVER STOP"),
    Callout("ETERNAL MODE", "LAYER AND REPEAT"),
    Callout("AUTOHARMONIZER", "DUAL TRACK FUSION"),
    Callout("SECTION SCULPTOR", "ARRANGE CLIPS"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabHero() {
    // Pulsing cyan title glow — mirrors the CSS `title-pulse` keyframes (20px <-> 40px).
    val pulse = rememberInfiniteTransition(label = "title-pulse")
    val glow by pulse.animateFloat(
        initialValue = 18f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "glow",
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // eyebrow
        Text(
            "*** WELCOME TO THE HARMONIZER LAB ***",
            color = NeonLime,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
            style = TextStyle(shadow = Shadow(NeonLime, Offset.Zero, 12f)),
        )
        Spacer(Modifier.height(10.dp))

        // title
        Text(
            "HASHTAG\nINFINITE LOOPS",
            color = NeonCyan,
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            letterSpacing = 2.sp,
            style = TextStyle(shadow = Shadow(NeonCyan, Offset.Zero, glow)),
        )
        Spacer(Modifier.height(12.dp))

        // lede
        Text(
            "UPLOAD YOUR TRACK. WE SLICE IT INTO BEATS, FIND THE PERFECT " +
                "MATCHES, AND CREATE ENDLESS SEAMLESS LOOPS. YOUR MUSIC NEVER HAS TO END.",
            color = NeonOrange,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.5.sp,
        )
        Spacer(Modifier.height(16.dp))

        // callouts
        LabCallouts.forEach { c ->
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = NeonCyan)) { append("⟡  ") }
                    withStyle(SpanStyle(color = NeonMagenta, fontWeight = FontWeight.Bold,
                        shadow = Shadow(NeonMagenta, Offset.Zero, 8f))) { append(c.mode) }
                    withStyle(SpanStyle(color = NeonCyan)) { append("  →  ${c.effect}") }
                },
                fontFamily = PlexMonoFamily,
                fontSize = 13.sp,
                lineHeight = 22.sp,
            )
        }
        Spacer(Modifier.height(16.dp))

        // side dancers (web keeps these with the callouts)
        AsciiDancersRow()

        Spacer(Modifier.height(18.dp))

        // badges
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LabBadge("BUSINESS CASUAL PRESENTS", NeonMagenta)
            LabBadge("ALOE ISLAND POSSE", NeonOrange)
            LabBadge("THE INTERNET'S DISCONTENT", NeonCyan)
        }

        Spacer(Modifier.height(20.dp))

        // hero visual — orbit rings + ascii woman (web `.hero-visual`)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { HeroVisual() }
    }
}

private val DancerPink = androidx.compose.ui.graphics.Color(0xFFFF6BD6)

// `.ascii-woman` from harmonizer.html
private val AsciiWoman = """
       _)))
      /|||\
     ({O O})
      ( > )
      /|||\
     / | | \
      /   \
     |     |
   _/ \___/ \_
  /   ( | )   \
 '    _\_//_   '
      o2  o2
""".trim('\n')

// `.ascii-dancer-side`
private val AsciiDancerArt = """
 ♫♪
 /|\
/ | \
  |
 ( )
  o2
""".trim('\n')

// 0..1 ease-in-out bounce factor (web `side-bounce`/`title` motion), phase-shiftable.
private fun bounceFactor(phase: Float): Float {
    val p = phase - kotlin.math.floor(phase)
    return (0.5 - 0.5 * kotlin.math.cos(2.0 * Math.PI * p)).toFloat()
}

@Composable
private fun HeroVisual() {
    // Motion ported from modern.css: woman-dance (2s, rotate ±3°), ring-rotate (20s, 360°).
    val t = rememberInfiniteTransition(label = "hero")
    val wphase by t.animateFloat(0f, 1f,
        infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), label = "woman")
    val ring by t.animateFloat(0f, 360f,
        infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart), label = "ring")

    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val p = 14.dp.toPx()
            val w = size.width
            val h = size.height
            // cyan ring — spins (ring-rotate)
            rotate(degrees = ring) {
                drawOval(
                    color = NeonCyan,
                    topLeft = Offset(p, p * 1.8f),
                    size = Size(w - p * 2f, h - p * 2.4f),
                    style = Stroke(width = 3f),
                )
            }
            // magenta ring — spins, offset to overlap (the two .hero-ring ellipses)
            rotate(degrees = ring + 18f) {
                drawOval(
                    color = NeonMagenta,
                    topLeft = Offset(p * 1.6f, p * 0.4f),
                    size = Size(w - p * 2.4f, h - p * 1.6f),
                    style = Stroke(width = 3f),
                )
            }
        }
        Text(
            AsciiWoman,
            color = NeonOrange,
            fontFamily = PlexMonoFamily,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            softWrap = false,
            style = TextStyle(shadow = Shadow(NeonOrange, Offset.Zero, 8f)),
            modifier = Modifier.graphicsLayer {
                rotationZ = -3f * kotlin.math.sin(2.0 * Math.PI * wphase).toFloat()
            },
        )
    }
}

/** The two bouncing side dancers (web `.ascii-dancer-cluster`, sits with the callouts). */
@Composable
private fun AsciiDancersRow() {
    val t = rememberInfiniteTransition(label = "dancers")
    val bounce by t.animateFloat(0f, 1f,
        infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart), label = "bounce")
    val amp = with(LocalDensity.current) { 8.dp.toPx() }
    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        AsciiDancer(NeonCyan, -amp * bounceFactor(bounce))
        AsciiDancer(DancerPink, -amp * bounceFactor(bounce + 0.4f))
    }
}

@Composable
private fun AsciiDancer(color: androidx.compose.ui.graphics.Color, translateY: Float) {
    Text(
        AsciiDancerArt,
        color = color,
        fontFamily = PlexMonoFamily,
        fontSize = 13.sp,
        lineHeight = 15.sp,
        softWrap = false,
        style = TextStyle(shadow = Shadow(color, Offset.Zero, 8f)),
        modifier = Modifier.graphicsLayer { this.translationY = translateY },
    )
}

@Composable
private fun LabBadge(label: String, color: androidx.compose.ui.graphics.Color) {
    // web `badge-glow` 2s ease-in-out pulse
    val pulse = rememberInfiniteTransition(label = "badge")
    val blur by pulse.animateFloat(6f, 14f,
        infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), label = "badge-blur")
    Text(
        label,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        style = TextStyle(shadow = Shadow(color, Offset.Zero, blur)),
        modifier = Modifier
            .border(3.dp, color)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
