package cc.harmonizerlabs.app.api.models

import com.google.gson.annotations.SerializedName

data class BackgroundRenderRequest(
    @SerializedName("trackId")          val trackId: String,
    @SerializedName("mode")             val mode: String,
    @SerializedName("minutes")          val minutes: Int,
    @SerializedName("seed")             val seed: Int = 42,
    @SerializedName("voiceCount")       val voiceCount: Int = 2,
    @SerializedName("quality")          val quality: String = "balanced",  // "fast" | "balanced" | "high"
    @SerializedName("advancedSettings") val advancedSettings: Map<String, Map<String, Double>>? = null,
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

// Keys must match what the server accepts: fast=22kHz/96k, balanced=32kHz/128k, high=44.1kHz/160k
enum class RenderQuality(val key: String, val label: String, val description: String) {
    FAST("fast", "FAST", "22 kHz · 96k"),
    BALANCED("balanced", "BALANCED", "32 kHz · 128k"),
    HIGH("high", "HIGH", "44.1 kHz · 160k"),
}
