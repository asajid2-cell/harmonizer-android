package cc.harmonizerlabs.app.player

import cc.harmonizerlabs.app.api.models.TrackAnalysis
import cc.harmonizerlabs.app.api.models.TrackData

/** Shared state handed to playback engines */
data class PlaybackContext(
    val track: TrackData,
    val voiceCount: Int = 2,
    val loopEnabled: Boolean = true,
    val noBurnout: Boolean = true,
)

/** Contract all beat-based playback engines implement */
interface PlaybackEngine {
    /** Return the next beat index to seek to, given the current beat index and analysis */
    fun nextBeatIndex(currentIndex: Int, analysis: TrackAnalysis, context: PlaybackContext): Int

    /** True if this mode needs a secondary (overlay) ExoPlayer */
    val requiresOverlayPlayer: Boolean get() = false

    /** Offset in beats for the overlay player (only used when requiresOverlayPlayer) */
    fun overlayOffsetBeats(analysis: TrackAnalysis, context: PlaybackContext): Int = 0
}

/** Canon engine: plays sequentially, pairs beats with overlay voice offset by canon_alignment.offset */
class CanonEngine : PlaybackEngine {
    override val requiresOverlayPlayer = true

    override fun nextBeatIndex(currentIndex: Int, analysis: TrackAnalysis, context: PlaybackContext): Int =
        (currentIndex + 1).coerceAtMost(analysis.beats.lastIndex)

    override fun overlayOffsetBeats(analysis: TrackAnalysis, context: PlaybackContext): Int =
        analysis.canonAlignment?.offset ?: analysis.globalVoiceOffsets?.firstOrNull() ?: 16
}

/** Jukebox engine: jumps to similar beats using loop_candidates */
class JukeboxEngine : PlaybackEngine {
    private var consecutiveSameSection = 0
    private val random = java.util.Random()

    override val requiresOverlayPlayer = false

    override fun nextBeatIndex(currentIndex: Int, analysis: TrackAnalysis, context: PlaybackContext): Int {
        val candidates = analysis.loopCandidates
            ?.filter { it.source == currentIndex && it.similarity > 0.55 }
            ?: emptyList()

        if (candidates.isEmpty()) return (currentIndex + 1).coerceAtMost(analysis.beats.lastIndex)

        val currentSection = analysis.beats.getOrNull(currentIndex)?.section ?: 0

        // anti-burnout: if we've been in same section >8 beats, force a section jump
        val sectionJump = context.noBurnout && consecutiveSameSection > 8

        val picked = if (sectionJump) {
            candidates.filter {
                (analysis.beats.getOrNull(it.target)?.section ?: 0) != currentSection
            }.maxByOrNull { it.similarity } ?: candidates.maxByOrNull { it.similarity }!!
        } else {
            // weighted random: higher similarity = more likely
            val totalWeight = candidates.sumOf { it.similarity }
            var r = random.nextDouble() * totalWeight
            var result = candidates.last()
            for (c in candidates) {
                r -= c.similarity
                if (r <= 0) { result = c; break }
            }
            result
        }

        val nextSection = analysis.beats.getOrNull(picked.target)?.section ?: 0
        consecutiveSameSection = if (nextSection == currentSection) consecutiveSameSection + 1 else 0

        return picked.target.coerceIn(0, analysis.beats.lastIndex)
    }
}

/** Eternal Canonizer = Jukebox + Canon overlay */
class EternalEngine : PlaybackEngine {
    private val jukebox = JukeboxEngine()
    override val requiresOverlayPlayer = true

    override fun nextBeatIndex(currentIndex: Int, analysis: TrackAnalysis, context: PlaybackContext): Int =
        jukebox.nextBeatIndex(currentIndex, analysis, context)

    override fun overlayOffsetBeats(analysis: TrackAnalysis, context: PlaybackContext): Int =
        analysis.canonAlignment?.offset ?: analysis.globalVoiceOffsets?.firstOrNull() ?: 16
}

/** Phase Shifter: plays sequentially (render-only, no live beat jumping needed) */
class PhaseShifterEngine : PlaybackEngine {
    override fun nextBeatIndex(currentIndex: Int, analysis: TrackAnalysis, context: PlaybackContext): Int =
        (currentIndex + 1).coerceAtMost(analysis.beats.lastIndex)
}

fun engineFor(modeKey: String): PlaybackEngine = when (modeKey) {
    "canon"         -> CanonEngine()
    "jukebox"       -> JukeboxEngine()
    "eternal"       -> EternalEngine()
    "phaseshifter"  -> PhaseShifterEngine()
    else            -> JukeboxEngine()
}
