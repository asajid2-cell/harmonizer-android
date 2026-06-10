package cc.harmonizerlabs.app.ui.player

import androidx.compose.animation.*
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
                    modifier = Modifier.clickable { viewModel.showModePicker() },
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 12.dp)
                    .border(2.dp, NeonLime.copy(alpha = 0.3f))
                    .background(SurfaceDark),
            ) {
                BeatVisualizer(
                    beats              = beats,
                    sections           = sections,
                    canonAlignment     = canon,
                    currentBeatIndex   = state.playback.currentBeatIndex,
                    mode               = state.mode,
                    modifier           = Modifier.fillMaxSize().padding(4.dp),
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
