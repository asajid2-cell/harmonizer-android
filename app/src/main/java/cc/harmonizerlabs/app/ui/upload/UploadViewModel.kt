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
    // Second track for Autoharmonizer
    val audio2FileUri: Uri? = null,
    val audio2FileName: String? = null,
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

    init {
        // Prefetch the 231-track library on start so it's usually ready the instant the
        // user taps VIEW SONGS — the cache/list round-trip otherwise stalls the sheet.
        prefetchSongs()
    }

    private fun prefetchSongs() {
        if (_state.value.cachedSongs.isNotEmpty()) return
        viewModelScope.launch {
            try {
                val r = api.getCachedSongs()
                if (r.isSuccessful) {
                    _state.update { it.copy(cachedSongs = r.body()?.tracks ?: it.cachedSongs) }
                }
            } catch (_: Exception) { /* a real error surfaces when the list is opened */ }
        }
    }

    fun selectMode(mode: HarmonizerMode) = _state.update { it.copy(selectedMode = mode) }
    fun selectSource(src: UploadSource) = _state.update { it.copy(source = src, urlInput = "") }
    fun setUrlInput(v: String) = _state.update { it.copy(urlInput = v) }
    fun setTitle(v: String) = _state.update { it.copy(title = v) }
    fun setArtist(v: String) = _state.update { it.copy(artist = v) }
    fun toggleExperimental() = _state.update { it.copy(showExperimentalModes = !it.showExperimentalModes) }
    fun clearError() = _state.update { it.copy(errorMessage = null) }
    fun clearCompletedTrack() = _state.update { it.copy(completedTrackId = null) }

    fun openSongList() {
        val haveSongs = _state.value.cachedSongs.isNotEmpty()
        // Show the cached list instantly if we already have it (prefetch / prior open); only show
        // LOADING on a true cold open. Either way, refresh quietly in the background.
        _state.update { it.copy(showSongList = true, isSongsLoading = !haveSongs, songsError = null) }
        viewModelScope.launch {
            try {
                val r = api.getCachedSongs()
                if (r.isSuccessful) {
                    _state.update { it.copy(isSongsLoading = false, cachedSongs = r.body()?.tracks ?: it.cachedSongs) }
                } else if (!haveSongs) {
                    _state.update { it.copy(isSongsLoading = false, songsError = "Server error ${r.code()}") }
                } else {
                    _state.update { it.copy(isSongsLoading = false) }
                }
            } catch (e: Exception) {
                _state.update {
                    if (haveSongs) it.copy(isSongsLoading = false)
                    else it.copy(isSongsLoading = false, songsError = e.localizedMessage ?: "Network error")
                }
            }
        }
    }

    fun closeSongList() = _state.update { it.copy(showSongList = false) }

    fun onFilePicked(context: Context, uri: Uri) {
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "audio"
        _state.update { it.copy(selectedFileUri = uri, selectedFileName = name) }
    }

    fun onAudio2Picked(context: Context, uri: Uri) {
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "audio2"
        _state.update { it.copy(audio2FileUri = uri, audio2FileName = name) }
    }

    fun submit(context: Context) {
        val st = _state.value
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = when {
                    st.selectedMode.key == "autoharmonizer" && st.source == UploadSource.FILE -> {
                        val uri1 = st.selectedFileUri
                            ?: throw IllegalStateException("No primary file selected")
                        val uri2 = st.audio2FileUri
                            ?: throw IllegalStateException("No second audio file selected for Autoharmonizer")
                        val tmp1 = uriToTempFile(context, uri1)
                        val tmp2 = uriToTempFile(context, uri2)
                        val audioPart = MultipartBody.Part.createFormData(
                            "audio", displayName(context, uri1),
                            tmp1.asRequestBody(mimeOf(context, uri1).toMediaTypeOrNull()),
                        )
                        val audio2Part = MultipartBody.Part.createFormData(
                            "audio2", displayName(context, uri2),
                            tmp2.asRequestBody(mimeOf(context, uri2).toMediaTypeOrNull()),
                        )
                        api.processAutoharmonizer(
                            audio     = audioPart,
                            audio2    = audio2Part,
                            source    = "upload".toRequestBody("text/plain".toMediaTypeOrNull()),
                            algorithm = st.selectedMode.key.toRequestBody("text/plain".toMediaTypeOrNull()),
                            title     = st.title.toRequestBody("text/plain".toMediaTypeOrNull()),
                            artist    = st.artist.toRequestBody("text/plain".toMediaTypeOrNull()),
                        )
                    }
                    st.source == UploadSource.FILE -> {
                        val uri = st.selectedFileUri
                            ?: throw IllegalStateException("No file selected")
                        val tmpFile = uriToTempFile(context, uri)
                        val audioPart = MultipartBody.Part.createFormData(
                            "audio",
                            displayName(context, uri),
                            tmpFile.asRequestBody(mimeOf(context, uri).toMediaTypeOrNull()),
                        )
                        api.processTrackUpload(
                            audio     = audioPart,
                            source    = "upload".toRequestBody("text/plain".toMediaTypeOrNull()),
                            algorithm = st.selectedMode.key.toRequestBody("text/plain".toMediaTypeOrNull()),
                            title     = st.title.toRequestBody("text/plain".toMediaTypeOrNull()),
                            artist    = st.artist.toRequestBody("text/plain".toMediaTypeOrNull()),
                        )
                    }
                    st.source == UploadSource.YOUTUBE -> api.processTrackUrl(
                        source     = "youtube",
                        algorithm  = st.selectedMode.key,
                        youtubeUrl = st.urlInput,
                        title      = st.title,
                        artist     = st.artist,
                    )
                    else -> api.processTrackUrl(
                        source     = "spotify",
                        algorithm  = st.selectedMode.key,
                        spotifyUrl = st.urlInput,
                        title      = st.title,
                        artist     = st.artist,
                    )
                }

                if (!response.isSuccessful) {
                    // Prefer the server's own message (e.g. YouTube/Spotify download failures carry
                    // actionable text like "Upload the audio file directly") over a bare HTTP code.
                    val msg = response.errorBody()?.string()?.let(::extractServerError)
                        ?: "Server error ${response.code()}"
                    _state.update { it.copy(isSubmitting = false, errorMessage = msg) }
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
                // Server terminal states are "completed" / "failed" (the web checks these exact
                // strings); accept the shorter variants defensively.
                when (r.body()?.status) {
                    "completed", "complete", "done" -> {
                        val trackId = r.body()?.result?.trackId
                        if (trackId != null) finishWithTrack(trackId)
                        else _state.update { it.copy(isSubmitting = false, errorMessage = "No trackId in result") }
                        return
                    }
                    "failed", "error" -> {
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

    // Pull the server's `error` message out of an error-response body. Servers return a long
    // multi-line message for download failures; show the headline (first line) in the banner.
    private fun extractServerError(body: String): String? = try {
        org.json.JSONObject(body).optString("error")
            .takeIf { it.isNotBlank() }
            ?.substringBefore("\n")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }

    private fun uriToTempFile(context: Context, uri: Uri): File {
        val tmp = File.createTempFile("harmonizer_upload", ".audio", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tmp).use { output -> input.copyTo(output) }
        }
        return tmp
    }

    // Real filename (with extension) for an uploaded URI. The server validates the multipart
    // part's filename to detect the audio format, so it must be e.g. "song.mp3", not a generic
    // ".audio" temp name (which yields a 400).
    private fun displayName(context: Context, uri: Uri): String {
        var name: String? = null
        context.contentResolver.query(
            uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null,
        )?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = c.getString(idx)
            }
        }
        return name?.takeIf { it.contains('.') } ?: "audio.mp3"
    }

    // Concrete MIME for the part (never the wildcard audio type, which servers reject).
    private val WILDCARD_AUDIO = "audio/" + "*"
    private fun mimeOf(context: Context, uri: Uri): String =
        context.contentResolver.getType(uri)?.takeIf { it != WILDCARD_AUDIO } ?: "audio/mpeg"
}
