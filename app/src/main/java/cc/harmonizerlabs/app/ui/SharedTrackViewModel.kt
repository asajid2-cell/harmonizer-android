package cc.harmonizerlabs.app.ui

import androidx.lifecycle.ViewModel
import cc.harmonizerlabs.app.api.models.TrackData
import cc.harmonizerlabs.app.model.HarmonizerMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Activity-scoped ViewModel that passes TrackData between Processing → Player.
 * Navigation arguments can only carry strings; this bridges the gap.
 */
@HiltViewModel
class SharedTrackViewModel @Inject constructor() : ViewModel() {

    private val _track = MutableStateFlow<TrackData?>(null)
    val track: StateFlow<TrackData?> = _track.asStateFlow()

    private val _mode = MutableStateFlow(HarmonizerMode.CANON)
    val mode: StateFlow<HarmonizerMode> = _mode.asStateFlow()

    fun setTrack(track: TrackData, mode: HarmonizerMode) {
        _track.value = track
        _mode.value  = mode
    }
}
