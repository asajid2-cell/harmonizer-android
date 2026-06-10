package cc.harmonizerlabs.app.ui.render

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cc.harmonizerlabs.app.BuildConfig
import cc.harmonizerlabs.app.api.models.RenderDuration
import cc.harmonizerlabs.app.api.models.RenderQuality
import cc.harmonizerlabs.app.model.AdvancedSettings
import cc.harmonizerlabs.app.ui.components.NeonButton
import cc.harmonizerlabs.app.ui.components.NeonCtaButton
import cc.harmonizerlabs.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenderSheet(
    trackId: String,
    modeKey: String,
    voiceCount: Int,
    advancedSettings: AdvancedSettings = AdvancedSettings(),
    onPlayRendered: (url: String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: RenderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Black,
        scrimColor       = Black.copy(alpha = 0.75f),
        shape            = RectangleShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Title
            Text(
                "~~~~ BACKGROUND RENDER ~~~~",
                style     = MaterialTheme.typography.titleMedium,
                color     = NeonLime,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )

            HorizontalDivider(color = NeonLime, thickness = 2.dp)

            Spacer(Modifier.height(20.dp))

            // Duration
            Text("DURATION", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                RenderDuration.entries.forEach { d ->
                    NeonButton(
                        label       = d.label,
                        onClick     = { viewModel.setDuration(d) },
                        active      = state.duration == d,
                        borderColor = NeonCyan,
                        modifier    = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Quality
            Text("QUALITY", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                RenderQuality.entries.forEach { q ->
                    NeonButton(
                        label       = q.label,
                        onClick     = { viewModel.setQuality(q) },
                        active      = state.quality == q,
                        borderColor = NeonCyan,
                        modifier    = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            when {
                state.isRendering -> {
                    // Progress
                    Text(
                        state.progressText.ifBlank { "RENDERING..." },
                        style     = MaterialTheme.typography.headlineSmall,
                        color     = NeonCyan,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .border(1.dp, NeonCyan.copy(alpha = 0.25f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(state.progressPercent / 100f)
                                .background(NeonCyan)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${state.progressPercent}%",
                        style     = MaterialTheme.typography.labelMedium,
                        color     = NeonCyan,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                }

                state.resultUrl != null -> {
                    val url = state.resultUrl!!
                    val filename = "harmonizer_${modeKey}_${state.duration.minutes}min.mp3"
                    Text(
                        "✓ RENDER COMPLETE",
                        style     = MaterialTheme.typography.headlineSmall,
                        color     = NeonLime,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        NeonButton(
                            label       = "▶ PLAY",
                            onClick     = { onPlayRendered(url) },
                            borderColor = NeonCyan,
                            modifier    = Modifier.weight(1f),
                        )
                        NeonButton(
                            label       = "⬇ SAVE",
                            onClick     = { viewModel.downloadFile(url, filename) },
                            borderColor = NeonLime,
                            modifier    = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    NeonButton(
                        label       = "RENDER AGAIN",
                        onClick     = { viewModel.reset() },
                        borderColor = NeonMagenta.copy(alpha = 0.5f),
                        modifier    = Modifier.fillMaxWidth(),
                    )
                }

                else -> {
                    Text(
                        state.quality.description,
                        style     = MaterialTheme.typography.bodySmall,
                        color     = TextMuted,
                        modifier  = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    )
                    NeonCtaButton(
                        label   = "◉ RENDER ${state.duration.label} · ${state.quality.label}",
                        onClick = { viewModel.startRender(trackId, modeKey, voiceCount, advancedSettings) },
                    )
                }
            }

            state.error?.let { err ->
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, NeonOrange)
                        .padding(12.dp)
                        .clickable { viewModel.clearError() },
                ) {
                    Text(err, style = MaterialTheme.typography.bodySmall, color = NeonOrange)
                }
            }
        }
    }
}
