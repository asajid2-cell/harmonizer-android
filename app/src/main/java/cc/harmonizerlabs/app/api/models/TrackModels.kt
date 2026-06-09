package cc.harmonizerlabs.app.api.models

import com.google.gson.annotations.SerializedName

// Root response wrapper — matches the Echo Nest / Harmonizer analysis JSON format
data class TrackAnalysisResponse(
    @SerializedName("response") val response: TrackResponse,
)

data class TrackResponse(
    @SerializedName("track") val track: TrackData,
)

data class TrackData(
    @SerializedName("id")            val id: String,
    @SerializedName("info")          val info: TrackInfo,
    @SerializedName("audio_summary") val audioSummary: AudioSummary,
    @SerializedName("analysis")      val analysis: TrackAnalysis,
    @SerializedName("title")         val title: String? = null,
    @SerializedName("artist")        val artist: String? = null,
)

data class TrackInfo(
    @SerializedName("url") val url: String,
)

data class AudioSummary(
    @SerializedName("duration")        val duration: Double,
    @SerializedName("time_signature")  val timeSignature: Int = 4,
    @SerializedName("tempo")           val tempo: Double = 120.0,
)

data class TrackAnalysis(
    @SerializedName("beats")                  val beats: List<Beat>,
    @SerializedName("segments")               val segments: List<Segment>,
    @SerializedName("sections")               val sections: List<Section>,
    @SerializedName("loop_candidates")        val loopCandidates: List<LoopCandidate>? = null,
    @SerializedName("eternal_loop_candidates") val eternalLoopCandidates: Map<String, List<LoopCandidate>>? = null,
    @SerializedName("canon_alignment")        val canonAlignment: CanonAlignment? = null,
    @SerializedName("canon_candidates")       val canonCandidates: Map<String, Any>? = null,
    @SerializedName("global_voice_offsets")   val globalVoiceOffsets: List<Int>? = null,
)

data class Beat(
    @SerializedName("start")          val start: Double,
    @SerializedName("duration")       val duration: Double,
    @SerializedName("confidence")     val confidence: Double = 1.0,
    @SerializedName("median_volume")  val medianVolume: Double = 0.5,
    @SerializedName("loudness_start") val loudnessStart: Double = -10.0,
    @SerializedName("loudness_max")   val loudnessMax: Double = -5.0,
    // Index into sections list for coloring
    @SerializedName("section")        val section: Int = 0,
)

data class Segment(
    @SerializedName("timbre")          val timbre: List<Double>,
    @SerializedName("pitches")         val pitches: List<Double>,
    @SerializedName("loudness_start")  val loudnessStart: Double,
    @SerializedName("loudness_max")    val loudnessMax: Double,
    @SerializedName("duration")        val duration: Double,
    @SerializedName("confidence")      val confidence: Double,
)

data class Section(
    @SerializedName("label")    val label: String = "",
    @SerializedName("start")    val start: Double,
    @SerializedName("duration") val duration: Double,
)

data class LoopCandidate(
    @SerializedName("source")     val source: Int,
    @SerializedName("target")     val target: Int,
    @SerializedName("similarity") val similarity: Double,
    @SerializedName("span")       val span: Int = 0,
)

data class CanonAlignment(
    @SerializedName("pairs")           val pairs: List<Int>,
    @SerializedName("pair_similarity") val pairSimilarity: List<Double>,
    @SerializedName("offset")          val offset: Int,
    @SerializedName("segments")        val segments: List<CanonSegment>,
)

data class CanonSegment(
    @SerializedName("start")  val start: Int,
    @SerializedName("end")    val end: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("label")  val label: String = "",
)

// ── Cached song library (/api/cache/list) ─────────────────────────────────────

data class CachedSongsResponse(
    @SerializedName("tracks") val tracks: List<CachedSong> = emptyList(),
)

data class CachedSong(
    @SerializedName("trackId")  val trackId: String,
    @SerializedName("title")    val title: String? = null,
    @SerializedName("artist")   val artist: String? = null,
    @SerializedName("duration") val duration: Double? = null,
)
