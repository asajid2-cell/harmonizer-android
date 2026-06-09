package cc.harmonizerlabs.app.ui.processing

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cc.harmonizerlabs.app.api.models.TrackData
import cc.harmonizerlabs.app.ui.components.GridBackground
import cc.harmonizerlabs.app.ui.components.NeonBar
import cc.harmonizerlabs.app.ui.theme.*

@Composable
fun ProcessingScreen(
    trackId: String,
    onReady: (TrackData) -> Unit,
    onBack: () -> Unit,
    viewModel: ProcessingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(trackId) { viewModel.loadTrack(trackId) }

    LaunchedEffect(state.isComplete, state.trackData) {
        if (state.isComplete && state.trackData != null) {
            onReady(state.trackData!!)
        }
    }

    val pulse by rememberInfiniteTransition(label = "p").animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1.0f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    Box(Modifier.fillMaxSize().background(Black)) {
        GridBackground()

        Column(
            modifier              = Modifier.fillMaxSize(),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center,
        ) {
            NeonBar(Modifier.align(Alignment.Start))

            Spacer(Modifier.weight(1f))

            Text(
                "♫",
                style = MaterialTheme.typography.displayLarge,
                color = NeonCyan.copy(alpha = pulse),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                state.statusText,
                style     = MaterialTheme.typography.headlineMedium,
                color     = NeonCyan,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                trackId,
                style     = MaterialTheme.typography.titleMedium,
                color     = TextMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(3.dp)
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(state.progressPercent / 100f)
                        .background(NeonCyan)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "${state.progressPercent}%",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan,
            )

            state.error?.let { err ->
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .border(2.dp, NeonOrange)
                        .padding(12.dp),
                ) {
                    Text(err, style = MaterialTheme.typography.bodySmall, color = NeonOrange)
                }
            }

            Spacer(Modifier.weight(1f))

            TextButton(onClick = onBack) {
                Text("← BACK", style = MaterialTheme.typography.labelLarge, color = TextMuted)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
