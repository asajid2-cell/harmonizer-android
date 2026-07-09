package cc.harmonizerlabs.app.player

import cc.harmonizerlabs.app.api.models.Beat
import cc.harmonizerlabs.app.api.models.Section
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

/**
 * Resolve a beat's section from the analysis `sections` list. The server's beats do NOT carry a
 * `section` field (only start/duration/confidence), so reading `beat.section` would always be 0 —
 * section-aware logic must look the beat's start time up against the sections instead.
 */
internal fun sectionOfBeat(beatIndex: Int, analysis: TrackAnalysis): Int {
    val start = analysis.beats.getOrNull(beatIndex)?.start ?: return 0
    if (analysis.sections.isEmpty()) return 0
    return analysis.sections.indexOfLast { it.start <= start + 1e-3 }.coerceAtLeast(0)
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

        val currentSection = sectionOfBeat(currentIndex, analysis)

        // anti-burnout: if we've been in same section >8 beats, force a section jump
        val sectionJump = context.noBurnout && consecutiveSameSection > 8

        val picked = if (sectionJump) {
            candidates.filter {
                sectionOfBeat(it.target, analysis) != currentSection
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

        val nextSection = sectionOfBeat(picked.target, analysis)
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

/**
 * Section Sculptor: plays the detected sections in a user-defined arrangement order, looping
 * the arrangement. When the last beat of the current section is reached it jumps to the first
 * beat of the next section in [arrangement]. Empty arrangement = sections in their natural order.
 */
class SculptorEngine : PlaybackEngine {
    @Volatile var arrangement: List<Int> = emptyList()
    private var step = 0

    override fun nextBeatIndex(currentIndex: Int, analysis: TrackAnalysis, context: PlaybackContext): Int {
        val sections = analysis.sections
        val beats    = analysis.beats
        if (sections.isEmpty() || beats.isEmpty()) {
            return (currentIndex + 1).coerceIn(0, beats.lastIndex.coerceAtLeast(0))
        }
        val order = arrangement.ifEmpty { sections.indices.toList() }
        if (order.isEmpty()) return (currentIndex + 1).coerceIn(0, beats.lastIndex.coerceAtLeast(0))

        val curSection = order[step.coerceIn(0, order.lastIndex)]
        val secEnd     = lastBeatOfSection(curSection, sections, beats)

        return if (currentIndex >= secEnd) {
            step = (step + 1) % order.size
            firstBeatOfSection(order[step], sections, beats)
        } else {
            (currentIndex + 1).coerceAtMost(beats.lastIndex)
        }
    }

    /** Restart the arrangement and return the beat index to seek to (first section's first beat). */
    fun restart(analysis: TrackAnalysis): Int {
        step = 0
        val order = arrangement.ifEmpty { analysis.sections.indices.toList() }
        if (order.isEmpty()) return 0
        return firstBeatOfSection(order[0], analysis.sections, analysis.beats)
    }

    private fun firstBeatOfSection(secIdx: Int, sections: List<Section>, beats: List<Beat>): Int {
        val s = sections.getOrNull(secIdx) ?: return 0
        return beats.indexOfFirst { it.start >= s.start - 1e-3 }.coerceAtLeast(0)
    }

    private fun lastBeatOfSection(secIdx: Int, sections: List<Section>, beats: List<Beat>): Int {
        val s = sections.getOrNull(secIdx) ?: return beats.lastIndex
        val end = s.start + s.duration
        val idx = beats.indexOfLast { it.start < end - 1e-3 }
        return if (idx >= 0) idx else beats.lastIndex
    }
}

fun engineFor(modeKey: String): PlaybackEngine = when (modeKey) {
    "canon"         -> CanonEngine()
    "jukebox"       -> JukeboxEngine()
    "eternal"       -> EternalEngine()
    "phaseshifter"  -> PhaseShifterEngine()
    "sculptor"      -> SculptorEngine()
    else            -> JukeboxEngine()
}
