package cc.harmonizerlabs.app.ui.render

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.harmonizerlabs.app.BuildConfig
import cc.harmonizerlabs.app.api.HarmonizerApi
import cc.harmonizerlabs.app.api.models.BackgroundRenderRequest
import cc.harmonizerlabs.app.api.models.RenderDuration
import cc.harmonizerlabs.app.api.models.RenderQuality
import cc.harmonizerlabs.app.model.AdvancedSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RenderUiState(
    val duration: RenderDuration = RenderDuration.TEN,
    val quality: RenderQuality = RenderQuality.BALANCED,
    val isRendering: Boolean = false,
    val progressPercent: Int = 0,
    val progressText: String = "",
    val resultUrl: String? = null,
    val error: String? = null,
)

@HiltViewModel
class RenderViewModel @Inject constructor(
    private val api: HarmonizerApi,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(RenderUiState())
    val state: StateFlow<RenderUiState> = _state.asStateFlow()

    fun setDuration(d: RenderDuration) = _state.update { it.copy(duration = d) }
    fun setQuality(q: RenderQuality)  = _state.update { it.copy(quality = q) }
    fun clearError()                  = _state.update { it.copy(error = null) }
    fun reset() = _state.update { RenderUiState(duration = it.duration, quality = it.quality) }

    fun startRender(
        trackId: String,
        modeKey: String,
        voiceCount: Int,
        advancedSettings: AdvancedSettings = AdvancedSettings(),
    ) {
        _state.update { it.copy(isRendering = true, resultUrl = null, error = null, progressPercent = 0) }
        viewModelScope.launch {
            try {
                val r = api.startBackgroundRender(
                    BackgroundRenderRequest(
                        trackId          = trackId,
                        mode             = modeKey,
                        minutes          = _state.value.duration.minutes,
                        voiceCount       = voiceCount,
                        quality          = _state.value.quality.key,
                        advancedSettings = advancedSettings.toApiMap(modeKey).ifEmpty { null },
                    )
                )
                if (!r.isSuccessful) {
                    _state.update { it.copy(isRendering = false, error = "Render start failed ${r.code()}") }
                    return@launch
                }
                pollRender(r.body()!!.jobId)
            } catch (e: Exception) {
                _state.update { it.copy(isRendering = false, error = e.localizedMessage ?: "Network error") }
            }
        }
    }

    private suspend fun pollRender(jobId: String) {
        while (true) {
            delay(3000)
            try {
                val r = api.getBackgroundRenderStatus(jobId)
                if (!r.isSuccessful) {
                    _state.update { it.copy(isRendering = false, error = "Poll failed ${r.code()}") }
                    return
                }
                val body = r.body()!!
                _state.update {
                    it.copy(
                        progressPercent = body.progressPercent ?: it.progressPercent,
                        progressText    = body.progress ?: "",
                    )
                }
                // Server terminal states are "completed" / "failed" (the web checks these exact
                // strings). Accept the shorter variants too, defensively.
                when (body.status) {
                    "completed", "complete", "done" -> {
                        val url = body.result?.url
                        if (url != null) {
                            _state.update { it.copy(isRendering = false, resultUrl = url, progressPercent = 100) }
                        } else {
                            _state.update { it.copy(isRendering = false, error = "No URL in result") }
                        }
                        return
                    }
                    "failed", "error" -> {
                        _state.update { it.copy(isRendering = false, error = body.error ?: "Render failed") }
                        return
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isRendering = false, error = e.localizedMessage ?: "Poll error") }
                return
            }
        }
    }

    fun downloadFile(url: String, filename: String) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val resolved = if (url.startsWith("http")) url
                       else "${BuildConfig.BASE_URL.trimEnd('/')}$url"
        val req = DownloadManager.Request(Uri.parse(resolved))
            .setTitle(filename)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, filename)
            .setMimeType("audio/mpeg")
        dm.enqueue(req)
    }
}
