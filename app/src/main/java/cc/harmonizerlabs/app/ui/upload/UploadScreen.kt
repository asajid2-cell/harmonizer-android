package cc.harmonizerlabs.app.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cc.harmonizerlabs.app.model.*
import cc.harmonizerlabs.app.ui.components.*
import cc.harmonizerlabs.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onTrackReady: (trackId: String, mode: HarmonizerMode) -> Unit,
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

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            NeonBar()

            // ── Title ─────────────────────────────────────────────────────────
            Spacer(Modifier.height(24.dp))
            Text(
                text      = "H A R M O N I Z E R",
                style     = MaterialTheme.typography.displayLarge,
                color     = NeonMagenta,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth(),
            )
            Text(
                text      = "harmonizerlabs.cc",
                style     = MaterialTheme.typography.titleMedium,
                color     = TextMuted,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth().padding(top = 4.dp),
            )

            Spacer(Modifier.height(28.dp))

            // ── Mode panel ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(2.dp, NeonLime)
                    .background(Black)
                    .padding(16.dp),
            ) {
                Text(
                    "SELECT MODE",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonLime,
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
                    .border(2.dp, NeonLime)
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
                        // Tap-to-pick zone
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
