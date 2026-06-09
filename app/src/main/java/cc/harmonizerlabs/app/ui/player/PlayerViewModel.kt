package cc.harmonizerlabs.app.ui.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.harmonizerlabs.app.api.models.TrackData
import cc.harmonizerlabs.app.model.HarmonizerMode
import cc.harmonizerlabs.app.player.HarmonizerPlaybackService
import cc.harmonizerlabs.app.player.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val trackData: TrackData? = null,
    val mode: HarmonizerMode = HarmonizerMode.CANON,
    val playback: PlaybackState = PlaybackState(),
    val showAdvanced: Boolean = false,
    val showModePicker: Boolean = false,
    val showRenderSheet: Boolean = false,
    val serviceBound: Boolean = false,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var service: HarmonizerPlaybackService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val svc = (binder as HarmonizerPlaybackService.LocalBinder).getService()
            service = svc
            _state.update { it.copy(serviceBound = true) }
            // Sync playback state from service
            viewModelScope.launch {
                svc.state.collect { ps -> _state.update { it.copy(playback = ps) } }
            }
            // Load the pending track only if the service hasn't already loaded one.
            // Guarding here prevents a reconnect (e.g. after screen rotation) from
            // restarting playback from beat 0 while the track is already playing.
            _state.value.trackData?.let { td ->
                if (svc.state.value.durationMs == 0L) {
                    svc.loadTrack(td, _state.value.mode.key)
                }
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            _state.update { it.copy(serviceBound = false) }
        }
    }

    fun bindService() {
        val intent = Intent(context, HarmonizerPlaybackService::class.java)
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        if (_state.value.serviceBound) {
            context.unbindService(connection)
            _state.update { it.copy(serviceBound = false) }
        }
    }

    fun loadTrack(track: TrackData, mode: HarmonizerMode) {
        _state.update { it.copy(trackData = track, mode = mode) }
        service?.loadTrack(track, mode.key)
    }

    fun togglePlayPause() { service?.togglePlayPause() }

    fun seekToBeat(index: Int) { service?.seekToBeat(index) }

    fun setVoiceCount(n: Int) {
        service?.setVoiceCount(n)
        _state.update { it.copy(playback = _state.value.playback.copy(voiceCount = n)) }
    }

    fun setMode(mode: HarmonizerMode) {
        _state.update { it.copy(mode = mode, showModePicker = false) }
        val track = _state.value.trackData ?: return
        service?.loadTrack(track, mode.key)
    }

    fun setLoop(v: Boolean) { service?.setLoop(v) }
    fun setNoBurnout(v: Boolean) { service?.setNoBurnout(v) }

    fun toggleAdvanced() = _state.update { it.copy(showAdvanced = !it.showAdvanced) }
    fun showModePicker() = _state.update { it.copy(showModePicker = true) }
    fun hideModePicker() = _state.update { it.copy(showModePicker = false) }
    fun showRenderSheet() = _state.update { it.copy(showRenderSheet = true) }
    fun hideRenderSheet() = _state.update { it.copy(showRenderSheet = false) }

    fun loadRenderedAudio(url: String) { service?.loadRenderedAudio(url) }

    override fun onCleared() {
        unbindService()
        super.onCleared()
    }
}
