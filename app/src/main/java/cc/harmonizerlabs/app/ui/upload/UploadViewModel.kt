package cc.harmonizerlabs.app.ui.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.harmonizerlabs.app.api.HarmonizerApi
import cc.harmonizerlabs.app.api.models.CachedSong
import cc.harmonizerlabs.app.model.HarmonizerMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

enum class UploadSource { FILE, YOUTUBE, SPOTIFY }

data class UploadUiState(
    val selectedMode: HarmonizerMode = HarmonizerMode.CANON,
    val source: UploadSource = UploadSource.FILE,
    val urlInput: String = "",
    val title: String = "",
    val artist: String = "",
    val selectedFileUri: Uri? = null,
    val selectedFileName: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val pendingJobId: String? = null,
    val completedTrackId: String? = null,
    val recentTracks: List<RecentTrack> = emptyList(),
    val showExperimentalModes: Boolean = false,
    val showSongList: Boolean = false,
    val cachedSongs: List<CachedSong> = emptyList(),
    val isSongsLoading: Boolean = false,
    val songsError: String? = null,
)

data class RecentTrack(
    val trackId: String,
    val title: String,
    val artist: String,
    val mode: HarmonizerMode,
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val api: HarmonizerApi,
) : ViewModel() {

    private val _state = MutableStateFlow(UploadUiState())
    val state: StateFlow<UploadUiState> = _state.asStateFlow()

    fun selectMode(mode: HarmonizerMode) = _state.update { it.copy(selectedMode = mode) }
    fun selectSource(src: UploadSource) = _state.update { it.copy(source = src, urlInput = "") }
    fun setUrlInput(v: String) = _state.update { it.copy(urlInput = v) }
    fun setTitle(v: String) = _state.update { it.copy(title = v) }
    fun setArtist(v: String) = _state.update { it.copy(artist = v) }
    fun toggleExperimental() = _state.update { it.copy(showExperimentalModes = !it.showExperimentalModes) }
    fun clearError() = _state.update { it.copy(errorMessage = null) }
    fun clearCompletedTrack() = _state.update { it.copy(completedTrackId = null) }

    fun openSongList() {
        _state.update { it.copy(showSongList = true, isSongsLoading = true, songsError = null) }
        viewModelScope.launch {
            try {
                val r = api.getCachedSongs()
                if (r.isSuccessful) {
                    _state.update { it.copy(isSongsLoading = false, cachedSongs = r.body()?.tracks ?: emptyList()) }
                } else {
                    _state.update { it.copy(isSongsLoading = false, songsError = "Server error ${r.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSongsLoading = false, songsError = e.localizedMessage ?: "Network error") }
            }
        }
    }

    fun closeSongList() = _state.update { it.copy(showSongList = false) }

    fun onFilePicked(context: Context, uri: Uri) {
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "audio"
        _state.update { it.copy(selectedFileUri = uri, selectedFileName = name) }
    }

    fun submit(context: Context) {
        val st = _state.value
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = when (st.source) {
                    UploadSource.FILE -> {
                        val uri = st.selectedFileUri
                            ?: throw IllegalStateException("No file selected")
                        val tmpFile = uriToTempFile(context, uri)
                        val audioPart = MultipartBody.Part.createFormData(
                            "audio",
                            tmpFile.name,
                            tmpFile.asRequestBody("audio/*".toMediaTypeOrNull()),
                        )
                        api.processTrackUpload(
                            audio     = audioPart,
                            source    = "upload".toRequestBody("text/plain".toMediaTypeOrNull()),
                            algorithm = st.selectedMode.key.toRequestBody("text/plain".toMediaTypeOrNull()),
                            title     = st.title.toRequestBody("text/plain".toMediaTypeOrNull()),
                            artist    = st.artist.toRequestBody("text/plain".toMediaTypeOrNull()),
                        )
                    }
                    UploadSource.YOUTUBE -> api.processTrackUrl(
                        source     = "youtube",
                        algorithm  = st.selectedMode.key,
                        youtubeUrl = st.urlInput,
                        title      = st.title,
                        artist     = st.artist,
                    )
                    UploadSource.SPOTIFY -> api.processTrackUrl(
                        source     = "spotify",
                        algorithm  = st.selectedMode.key,
                        spotifyUrl = st.urlInput,
                        title      = st.title,
                        artist     = st.artist,
                    )
                }

                if (!response.isSuccessful) {
                    _state.update { it.copy(isSubmitting = false, errorMessage = "Server error ${response.code()}") }
                    return@launch
                }

                val body = response.body()!!
                if (body.trackId != null) {
                    // Cached / instant result
                    finishWithTrack(body.trackId)
                } else if (body.jobId != null) {
                    _state.update { it.copy(pendingJobId = body.jobId) }
                    pollJob(body.jobId)
                } else {
                    _state.update { it.copy(isSubmitting = false, errorMessage = "Unexpected response") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSubmitting = false, errorMessage = e.localizedMessage ?: "Network error") }
            }
        }
    }

    private suspend fun pollJob(jobId: String) {
        while (true) {
            delay(2000)
            try {
                val r = api.getProcessStatus(jobId)
                if (!r.isSuccessful) {
                    _state.update { it.copy(isSubmitting = false, errorMessage = "Poll failed ${r.code()}") }
                    return
                }
                when (r.body()?.status) {
                    "complete" -> {
                        val trackId = r.body()?.result?.trackId
                        if (trackId != null) finishWithTrack(trackId)
                        else _state.update { it.copy(isSubmitting = false, errorMessage = "No trackId in result") }
                        return
                    }
                    "error" -> {
                        _state.update { it.copy(isSubmitting = false, errorMessage = r.body()?.error ?: "Processing failed") }
                        return
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSubmitting = false, errorMessage = e.localizedMessage ?: "Poll error") }
                return
            }
        }
    }

    private fun finishWithTrack(trackId: String) {
        val st = _state.value
        val recent = RecentTrack(
            trackId = trackId,
            title   = st.title.ifBlank { "Track" },
            artist  = st.artist.ifBlank { "Unknown" },
            mode    = st.selectedMode,
        )
        _state.update {
            it.copy(
                isSubmitting     = false,
                pendingJobId     = null,
                completedTrackId = trackId,
                recentTracks     = (listOf(recent) + it.recentTracks).take(8),
            )
        }
    }

    private fun uriToTempFile(context: Context, uri: Uri): File {
        val tmp = File.createTempFile("harmonizer_upload", ".audio", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tmp).use { output -> input.copyTo(output) }
        }
        return tmp
    }
}
