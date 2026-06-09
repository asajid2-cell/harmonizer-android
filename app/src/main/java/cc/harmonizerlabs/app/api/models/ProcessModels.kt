package cc.harmonizerlabs.app.api.models

import com.google.gson.annotations.SerializedName

data class ProcessRequest(
    val source: String,     // "upload" | "youtube" | "spotify"
    val algorithm: String,  // "canon" | "jukebox" | "eternal" | etc.
    val title: String = "",
    val artist: String = "",
    val youtubeUrl: String? = null,
    val spotifyUrl: String? = null,
)

data class ProcessResponse(
    @SerializedName("jobId")   val jobId: String? = null,
    @SerializedName("trackId") val trackId: String? = null,
    @SerializedName("status")  val status: String = "processing",
    @SerializedName("error")   val error: String? = null,
)

data class JobStatusResponse(
    @SerializedName("jobId")           val jobId: String,
    @SerializedName("status")          val status: String,         // "processing" | "complete" | "error"
    @SerializedName("progress")        val progress: String? = null,
    @SerializedName("progressPercent") val progressPercent: Int? = null,
    @SerializedName("result")          val result: ProcessResult? = null,
    @SerializedName("error")           val error: String? = null,
)

data class ProcessResult(
    @SerializedName("trackId") val trackId: String,
    @SerializedName("title")   val title: String? = null,
    @SerializedName("artist")  val artist: String? = null,
)

data class PlaylistInfoRequest(val url: String)

data class PlaylistInfoResponse(
    @SerializedName("is_playlist") val isPlaylist: Boolean,
    @SerializedName("title")       val title: String? = null,
    @SerializedName("entries")     val entries: List<PlaylistEntry>? = null,
)

data class PlaylistEntry(
    @SerializedName("id")    val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("url")   val url: String,
)
