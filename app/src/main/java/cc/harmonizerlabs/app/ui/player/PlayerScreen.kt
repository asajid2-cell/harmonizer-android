package cc.harmonizerlabs.app.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cc.harmonizerlabs.app.api.models.TrackData
import cc.harmonizerlabs.app.model.ExperimentalModes
import cc.harmonizerlabs.app.model.HarmonizerMode
import cc.harmonizerlabs.app.model.PrimaryModes
import cc.harmonizerlabs.app.model.AdvancedSettings
import cc.harmonizerlabs.app.ui.components.*
import cc.harmonizerlabs.app.ui.render.RenderSheet
import cc.harmonizerlabs.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    track: TrackData,
    initialMode: HarmonizerMode,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state   by viewModel.state.collectAsState()

    // Bind service on enter, unbind on leave
    DisposableEffect(Unit) {
        viewModel.bindService()
        viewModel.loadTrack(track, initialMode)
        // Unbind the connection on leave; service keeps running because it was also started
        // with startService(), so background playback continues uninterrupted.
        onDispose { viewModel.unbindService() }
    }

    // Populate the sculptor timeline (natural section order) once the service is ready
    LaunchedEffect(state.mode.key, state.serviceBound) {
        if (state.mode.key == "sculptor" && state.serviceBound) viewModel.initSculptorArrangement()
    }

    Box(Modifier.fillMaxSize().background(Black)) {
        GridBackground()

        Column(Modifier.fillMaxSize()) {
            NeonBar()

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = NeonCyan)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        track.title ?: "TRACK",
                        style    = MaterialTheme.typography.headlineMedium,
                        color    = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        track.artist ?: "",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = TextMuted,
                        maxLines = 1,
                    )
                }
                // Mode badge — tap to switch
                Surface(
                    shape  = RectangleShape,
                    color  = NeonMagenta.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .neonGlow(NeonMagenta, glowRadius = 8.dp, intensity = 0.5f)
                        .clickable { viewModel.showModePicker() },
                ) {
                    Text(
                        state.mode.displayName,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = NeonMagenta,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }

            // ── Beat visualizer ───────────────────────────────────────────────
            val beats    = track.analysis.beats
            val sections = track.analysis.sections
            val canon    = track.analysis.canonAlignment
            val isPlaying = state.playback.isPlaying

            // Breathing border glow while playing — mirrors the web's sweepGlow on the
            // music field (border alpha pulses cyan-ward instead of sitting static lime).
            val breathe by rememberInfiniteTransition(label = "breathe").animateFloat(
                initialValue  = 0.3f,
                targetValue   = 0.75f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(1600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "breathe_alpha",
            )
            val borderColor = if (isPlaying) NeonCyan.copy(alpha = breathe) else NeonLime.copy(alpha = 0.3f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 12.dp)
                    .neonGlow(NeonCyan, glowRadius = 14.dp, intensity = if (isPlaying) breathe else 0f)
                    .border(2.dp, borderColor)
                    .background(SurfaceDark),
            ) {
                // Jukebox/eternal get the circular orbit (web's viz-orbit); other modes the grid.
                if (state.mode.key == "jukebox" || state.mode.key == "eternal") {
                    JukeboxOrbitVisualizer(
                        beats            = beats,
                        sections         = sections,
                        loopCandidates   = track.analysis.loopCandidates,
                        currentBeatIndex = state.playback.currentBeatIndex,
                        segments         = track.analysis.segments,
                        modifier         = Modifier.fillMaxSize().padding(4.dp),
                    )
                } else {
                    BeatVisualizer(
                        beats              = beats,
                        sections           = sections,
                        canonAlignment     = canon,
                        currentBeatIndex   = state.playback.currentBeatIndex,
                        mode               = state.mode,
                        segments           = track.analysis.segments,
                        modifier           = Modifier.fillMaxSize().padding(4.dp),
                    )
                }
                // Floating note glyphs drifting over the grid while audio plays
                NoteParticles(
                    isPlaying = isPlaying,
                    modifier  = Modifier.fillMaxSize(),
                )
                // Beat count badge
                Text(
                    "${beats.size} BEATS",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = NeonCyan.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Controls panel ────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .neonGlow(NeonLime, glowRadius = 8.dp, intensity = 0.4f)
                    .border(2.dp, NeonLime)
                    .background(Black)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Progress bar
                val durationMs = state.playback.durationMs.takeIf { it > 0 } ?: 1L
                val progress   = (state.playback.currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(NeonCyan.copy(alpha = 0.2f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(NeonCyan)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ── Timer + stats readout (web viz-timer / viz-stats, monospace rose) ──
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "${fmtClock(state.playback.currentPositionMs)} / ${fmtClock(state.playback.durationMs)}",
                        style = MaterialTheme.typography.titleMedium,  // monospace
                        color = NeonRose,
                    )
                    // Beats stat — jump-based modes (jukebox/eternal) show the cumulative
                    // beats-played count (climbs past track length); linear modes show position.
                    val jumpMode = state.mode.key in setOf("jukebox", "eternal")
                    if (jumpMode) {
                        Text(
                            "BEATS ${state.playback.beatsPlayed}",
                            style = MaterialTheme.typography.titleMedium,
                            color = RoseGlow,
                        )
                    } else {
                        Text(
                            "${state.playback.currentBeatIndex + 1} / ${beats.size}",
                            style = MaterialTheme.typography.titleMedium,
                            color = RoseGlow.copy(alpha = 0.55f),
                        )
                    }
                }

                // Error banner — surfaces load failures instead of a silent dead play button
                state.playback.errorMessage?.let { msg ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        msg,
                        style     = MaterialTheme.typography.labelSmall,
                        color     = NeonOrange,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonOrange.copy(alpha = 0.5f))
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Play controls
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IconButton(
                        onClick = { viewModel.seekToBeat(0) },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(Icons.Default.SkipPrevious, "Restart", tint = NeonCyan)
                    }
                    Spacer(Modifier.width(16.dp))
                    // Large play/pause button
                    Surface(
                        shape  = RectangleShape,
                        color  = if (state.playback.isPlaying) NeonCyan else Black,
                        border = BorderStroke(2.dp, NeonCyan),
                        modifier = Modifier
                            .neonGlow(NeonCyan, glowRadius = 16.dp, intensity = if (state.playback.isPlaying) breathe else 0.4f)
                            .size(64.dp)
                            .clickable { viewModel.togglePlayPause() },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (state.playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (state.playback.isPlaying) "Pause" else "Play",
                                tint   = if (state.playback.isPlaying) Black else NeonCyan,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    // Loop toggle
                    IconButton(
                        onClick  = { viewModel.setLoop(!state.playback.loopEnabled) },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            Icons.Default.Repeat,
                            "Loop",
                            tint = if (state.playback.loopEnabled) NeonCyan else NeonCyan.copy(alpha = 0.3f),
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Voice count — only relevant for overlay modes (canon, eternal, phaseshifter, chromastack)
                if (state.mode.key in AdvancedSettings.OVERLAY_MODES) {
                    NeonSlider(
                        label         = "VOICES",
                        value         = state.playback.voiceCount.toFloat(),
                        onValueChange = { viewModel.setVoiceCount(it.toInt()) },
                        valueRange    = 2f..8f,
                        steps         = 5,
                        valueLabel    = state.playback.voiceCount.toString(),
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Phase intensity — Phase Shifter only
                if (state.mode.key == "phaseshifter") {
                    NeonSlider(
                        label         = "PHASE INTENSITY",
                        value         = state.advancedSettings.phaseIntensity,
                        onValueChange = { v ->
                            viewModel.updateAdvancedSettings(state.advancedSettings.copy(phaseIntensity = v))
                        },
                        valueRange    = 0f..4f,
                        valueLabel    = "%.1fx".format(state.advancedSettings.phaseIntensity),
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Section Sculptor — arrange detected sections into a custom timeline
                if (state.mode.key == "sculptor") {
                    SculptorPanel(
                        sections    = track.analysis.sections,
                        arrangement = state.sculptorArrangement,
                        onAdd       = { viewModel.addSculptorSection(it) },
                        onRemoveAt  = { viewModel.removeSculptorAt(it) },
                        onReset     = { viewModel.resetSculptor() },
                        onClear     = { viewModel.clearSculptor() },
                        onShuffle   = { viewModel.shuffleSculptor() },
                        modifier    = Modifier.padding(bottom = 8.dp),
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Action buttons row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NeonButton(
                        label       = "◉ BACKGROUND",
                        onClick     = { viewModel.showRenderSheet() },
                        borderColor = NeonOrange,
                        modifier    = Modifier.weight(1f),
                    )
                    NeonButton(
                        label       = if (state.showAdvanced) "ADVANCED ▲" else "ADVANCED ▼",
                        onClick     = { viewModel.toggleAdvanced() },
                        borderColor = NeonMagenta.copy(alpha = 0.6f),
                        modifier    = Modifier.weight(1f),
                    )
                }

                // Advanced panel
                AnimatedVisibility(visible = state.showAdvanced) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .border(1.dp, NeonMagenta.copy(alpha = 0.3f))
                            .background(SurfaceMid)
                            .padding(12.dp),
                    ) {
                        Text(
                            "ADVANCED SETTINGS",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonMagenta,
                        )
                        Spacer(Modifier.height(10.dp))

                        // Global toggles
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        ) {
                            Text(
                                "LOOP",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = TextMuted,
                                modifier = Modifier.weight(1f),
                            )
                            NeonButton(
                                label       = if (state.playback.loopEnabled) "ON" else "OFF",
                                onClick     = { viewModel.setLoop(!state.playback.loopEnabled) },
                                active      = state.playback.loopEnabled,
                                borderColor = NeonCyan,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        ) {
                            Text(
                                "NO BURNOUT",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = TextMuted,
                                modifier = Modifier.weight(1f),
                            )
                            NeonButton(
                                label       = if (state.playback.noBurnout) "ON" else "OFF",
                                onClick     = { viewModel.setNoBurnout(!state.playback.noBurnout) },
                                active      = state.playback.noBurnout,
                                borderColor = NeonCyan,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        ) {
                            Text(
                                "BASE AUDIO ONLY",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = TextMuted,
                                modifier = Modifier.weight(1f),
                            )
                            NeonButton(
                                label       = if (state.advancedSettings.baseAudioOnly) "ON" else "OFF",
                                onClick     = {
                                    viewModel.updateAdvancedSettings(
                                        state.advancedSettings.copy(
                                            baseAudioOnly = !state.advancedSettings.baseAudioOnly
                                        )
                                    )
                                },
                                active      = state.advancedSettings.baseAudioOnly,
                                borderColor = NeonOrange,
                            )
                        }

                        // Autocrooner style selector
                        if (state.mode.key == "autocrooner") {
                            Text(
                                "STYLE",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = NeonCyan,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                            if (state.autocroonerStyles.isEmpty()) {
                                NeonButton(
                                    label   = if (state.isLoadingStyles) "LOADING..." else "LOAD STYLES",
                                    onClick = { viewModel.loadAutocroonerStyles() },
                                    borderColor = NeonCyan,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                state.autocroonerStyles.forEach { style ->
                                    NeonButton(
                                        label       = style.name,
                                        onClick     = { viewModel.selectAutocroonerStyle(style.id) },
                                        active      = state.selectedStyleId == style.id,
                                        borderColor = NeonCyan,
                                        modifier    = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // Per-mode advanced knobs
                        AdvancedSettingsPanel(
                            modeKey  = state.mode.key,
                            settings = state.advancedSettings,
                            onUpdate = { viewModel.updateAdvancedSettings(it) },
                        )

                        Spacer(Modifier.height(8.dp))
                        // Mode description
                        Text(
                            state.mode.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                        )
                    }
                }
            }
        }

        // ── Mode picker bottom sheet ───────────────────────────────────────────
        if (state.showModePicker) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideModePicker() },
                containerColor   = Black,
                shape            = RectangleShape,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    Text(
                        "SELECT MODE",
                        style     = MaterialTheme.typography.labelMedium,
                        color     = NeonLime,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    (PrimaryModes + ExperimentalModes).forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setMode(mode) }
                                .background(if (mode == state.mode) NeonMagenta.copy(alpha = 0.12f) else Black)
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                        ) {
                            Text(mode.icon, style = MaterialTheme.typography.headlineSmall, color = NeonMagenta)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(mode.displayName, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                                Text(mode.description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            if (mode == state.mode) {
                                Icon(Icons.Default.Check, null, tint = NeonCyan)
                            }
                        }
                        HorizontalDivider(color = NeonLime.copy(alpha = 0.08f))
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }

        // ── Background render sheet ────────────────────────────────────────────
        if (state.showRenderSheet) {
            RenderSheet(
                trackId          = track.id,
                modeKey          = state.mode.key,
                voiceCount       = state.playback.voiceCount,
                advancedSettings = state.advancedSettings,
                onPlayRendered   = { url ->
                    viewModel.loadRenderedAudio(url)
                    viewModel.hideRenderSheet()
                },
                onDismiss = { viewModel.hideRenderSheet() },
            )
        }
    }
}

/** Format milliseconds as M:SS (or H:MM:SS for long renders), matching the web viz-timer. */
private fun fmtClock(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
