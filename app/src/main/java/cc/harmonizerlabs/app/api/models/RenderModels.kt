package cc.harmonizerlabs.app.api.models

import com.google.gson.annotations.SerializedName

data class BackgroundRenderRequest(
    @SerializedName("trackId")    val trackId: String,
    @SerializedName("mode")       val mode: String,       // "canon" | "jukebox" | "eternal"
    @SerializedName("minutes")    val minutes: Int,
    @SerializedName("seed")       val seed: Int = 42,
    @SerializedName("voiceCount") val voiceCount: Int = 2,
    @SerializedName("quality")    val quality: String = "standard",  // "draft" | "standard" | "high"
    @SerializedName("settings")   val settings: Map<String, Any>? = null,
)

data class BackgroundRenderResponse(
    @SerializedName("jobId")  val jobId: String,
    @SerializedName("status") val status: String,
)

data class RenderStatusResponse(
    @SerializedName("jobId")           val jobId: String,
    @SerializedName("status")          val status: String,
    @SerializedName("progress")        val progress: String? = null,
    @SerializedName("progressPercent") val progressPercent: Int? = null,
    @SerializedName("result")          val result: RenderResult? = null,
    @SerializedName("error")           val error: String? = null,
)

data class RenderResult(
    @SerializedName("url")      val url: String,
    @SerializedName("filename") val filename: String? = null,
    @SerializedName("duration") val duration: Double? = null,
)

enum class RenderDuration(val minutes: Int, val label: String) {
    FIVE(5, "5 MIN"),
    TEN(10, "10 MIN"),
    THIRTY(30, "30 MIN"),
    SIXTY(60, "60 MIN"),
}

enum class RenderQuality(val key: String, val label: String) {
    DRAFT("draft", "DRAFT"),
    STANDARD("standard", "STANDARD"),
    HIGH("high", "HIGH"),
}
