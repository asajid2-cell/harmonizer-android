package cc.harmonizerlabs.app.ui.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.harmonizerlabs.app.api.HarmonizerApi
import cc.harmonizerlabs.app.api.models.TrackData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProcessingUiState(
    val trackId: String = "",
    val statusText: String = "LOADING ANALYSIS...",
    val progressPercent: Int = 0,
    val isComplete: Boolean = false,
    val error: String? = null,
    val trackData: TrackData? = null,
)

@HiltViewModel
class ProcessingViewModel @Inject constructor(
    private val api: HarmonizerApi,
) : ViewModel() {

    private val _state = MutableStateFlow(ProcessingUiState())
    val state: StateFlow<ProcessingUiState> = _state.asStateFlow()

    fun loadTrack(trackId: String) {
        if (_state.value.trackId == trackId && _state.value.isComplete) return
        _state.update { it.copy(trackId = trackId, progressPercent = 10, statusText = "FETCHING ANALYSIS...") }

        viewModelScope.launch {
            repeat(30) { attempt ->
                try {
                    val r = api.getTrackAnalysis(trackId)
                    if (r.isSuccessful) {
                        val track = r.body()?.response?.track
                        if (track != null) {
                            _state.update {
                                it.copy(
                                    isComplete      = true,
                                    trackData       = track,
                                    progressPercent = 100,
                                    statusText      = "READY",
                                )
                            }
                            return@launch
                        }
                    }
                    // analysis not ready yet — this can happen if the server
                    // is still writing the JSON; back off and retry
                    val pct = (10 + attempt * 3).coerceAtMost(85)
                    _state.update { it.copy(progressPercent = pct, statusText = "COMPUTING...") }
                    delay(2000)
                } catch (e: Exception) {
                    if (attempt >= 4) {
                        _state.update { it.copy(error = e.localizedMessage ?: "Failed to load analysis") }
                        return@launch
                    }
                    delay(2000)
                }
            }
            _state.update { it.copy(error = "Analysis load timed out") }
        }
    }
}
