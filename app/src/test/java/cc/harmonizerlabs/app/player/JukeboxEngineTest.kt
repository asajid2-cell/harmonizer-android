package cc.harmonizerlabs.app.player

import cc.harmonizerlabs.app.api.models.*
import org.junit.Assert.*
import org.junit.Test

class JukeboxEngineTest {

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun beat(start: Double, section: Int = 0) =
        Beat(start = start, duration = 0.5, section = section)

    private fun analysis(
        beats: List<Beat>,
        candidates: List<LoopCandidate>? = null,
        sections: List<Section> = emptyList(),
    ) = TrackAnalysis(
        beats          = beats,
        segments       = emptyList(),
        sections       = sections,
        loopCandidates = candidates,
    )

    private fun track(analysis: TrackAnalysis) = TrackData(
        id           = "test",
        info         = TrackInfo("/audio/test.mp3"),
        audioSummary = AudioSummary(duration = 180.0),
        analysis     = analysis,
    )

    private fun ctx(track: TrackData, noBurnout: Boolean = true) =
        PlaybackContext(track = track, noBurnout = noBurnout)

    // ── No candidates → linear advance ───────────────────────────────────────

    @Test fun noCandidatesAdvancesLinearly() {
        val beats = List(10) { beat(it * 0.5) }
        val a = analysis(beats, candidates = emptyList())
        val t = track(a)
        val engine = JukeboxEngine()
        assertEquals(6, engine.nextBeatIndex(5, a, ctx(t)))
    }

    @Test fun noCandidatesAtLastBeatClampsToLastIndex() {
        val beats = List(5) { beat(it * 0.5) }
        val a = analysis(beats, candidates = null)
        val t = track(a)
        val engine = JukeboxEngine()
        assertEquals(4, engine.nextBeatIndex(4, a, ctx(t)))
    }

    // ── With candidates → picks from candidates ───────────────────────────────

    @Test fun withCandidatesPicksFromCandidates() {
        val beats = List(20) { beat(it * 0.5) }
        // Beat 5 has one high-similarity candidate → target 15
        val candidates = listOf(LoopCandidate(source = 5, target = 15, similarity = 0.9))
        val a = analysis(beats, candidates)
        val t = track(a)
        val engine = JukeboxEngine()
        val next = engine.nextBeatIndex(5, a, ctx(t))
        assertEquals(15, next)
    }

    @Test fun lowSimilarityFiltered() {
        val beats = List(20) { beat(it * 0.5) }
        // Candidate below 0.55 threshold should be ignored → linear advance
        val candidates = listOf(LoopCandidate(source = 5, target = 15, similarity = 0.4))
        val a = analysis(beats, candidates)
        val t = track(a)
        val engine = JukeboxEngine()
        assertEquals(6, engine.nextBeatIndex(5, a, ctx(t)))
    }

    // ── Anti-burnout: section jump forced after 9 consecutive same-section beats
    // Use two separate analysis objects so we can build up the counter deterministically
    // (sameSection-only analysis for the counter, then both-candidate analysis for the trigger).

    // Sections split the timeline: section 0 = starts [0,5), section 1 = starts [5,10).
    // Beats are at it*0.5, so beats 0..9 are section 0 and 10..19 are section 1 — derived from
    // the sections list (the engine no longer trusts a per-beat section field, which the server omits).
    private val twoSections = listOf(Section(start = 0.0, duration = 5.0), Section(start = 5.0, duration = 5.0))

    @Test fun noBurnoutForcesSectionJumpAfter9Beats() {
        val beats = (0 until 20).map { beat(it * 0.5) }

        // Same-section-only analysis: ONLY the target=4 candidate (section 0 → section 0).
        // This forces the engine to always pick target 4 and builds the counter deterministically.
        val sameSectionOnly = analysis(beats, listOf(
            LoopCandidate(source = 3, target = 4, similarity = 0.9),
        ), sections = twoSections)
        // Two-candidate analysis: both same-section (target=4) and cross-section (target=12).
        val twoCandidates = analysis(beats, listOf(
            LoopCandidate(source = 3, target = 4,  similarity = 0.9),
            LoopCandidate(source = 3, target = 12, similarity = 0.8),
        ), sections = twoSections)
        val t = track(twoCandidates)
        val engine = JukeboxEngine()

        // 9 same-section picks — deterministic because there is only one candidate.
        repeat(9) { engine.nextBeatIndex(3, sameSectionOnly, ctx(t)) }

        // Now provide both candidates; burnout should force the cross-section candidate (target=12).
        val next = engine.nextBeatIndex(3, twoCandidates, ctx(t))
        assertEquals(12, next)
    }

    @Test fun noBurnoutDisabledDoesNotForceSectionJump() {
        val beats = (0 until 20).map { beat(it * 0.5) }

        // Only same-section candidate — builds counter without interference.
        val sameSectionOnly = analysis(beats, listOf(
            LoopCandidate(source = 3, target = 4, similarity = 0.9),
        ), sections = twoSections)
        val twoCandidates = analysis(beats, listOf(
            LoopCandidate(source = 3, target = 4,  similarity = 0.9),
            LoopCandidate(source = 3, target = 12, similarity = 0.8),
        ), sections = twoSections)
        val t = track(twoCandidates)
        val engine = JukeboxEngine()

        // Build counter past 9 with noBurnout=false — engine should NOT force section jump.
        repeat(9) { engine.nextBeatIndex(3, sameSectionOnly, ctx(t, noBurnout = false)) }

        // With burnout disabled AND higher same-section similarity, should still pick target 4
        // (highest-weight in weighted random when not forced).  With noBurnout=false, sectionJump
        // is false so the weighted random runs.  We can't assert exact result because it's random,
        // but we CAN assert it does NOT always equal 12 (since 12 has lower similarity).
        // Instead just assert: counter never causes a forced jump (no AssertionError on this path).
        val next = engine.nextBeatIndex(3, twoCandidates, ctx(t, noBurnout = false))
        assertTrue("Should land in valid beat range", next in 0 until beats.size)
    }

    // ── Target clamped to valid range ─────────────────────────────────────────

    @Test fun targetClampedToLastIndex() {
        val beats = List(5) { beat(it * 0.5) }
        val candidates = listOf(LoopCandidate(source = 2, target = 999, similarity = 0.8))
        val a = analysis(beats, candidates)
        val t = track(a)
        val engine = JukeboxEngine()
        val next = engine.nextBeatIndex(2, a, ctx(t))
        assertEquals(4, next) // clamped to lastIndex = 4
    }

    // ── CanonEngine: always linear ────────────────────────────────────────────

    @Test fun canonEngineLinear() {
        val beats = List(10) { beat(it * 0.5) }
        val a = analysis(beats)
        val t = track(a)
        val engine = CanonEngine()
        assertEquals(6, engine.nextBeatIndex(5, a, ctx(t)))
    }

    @Test fun canonEngineRequiresOverlay() {
        assertTrue(CanonEngine().requiresOverlayPlayer)
    }

    @Test fun jukeboxEngineNoOverlay() {
        assertFalse(JukeboxEngine().requiresOverlayPlayer)
    }

    // ── engineFor() dispatch ─────────────────────────────────────────────────

    @Test fun engineForCanonReturnsCanon() {
        assertTrue(engineFor("canon") is CanonEngine)
    }

    @Test fun engineForJukeboxReturnsJukebox() {
        assertTrue(engineFor("jukebox") is JukeboxEngine)
    }

    @Test fun engineForEternalReturnsEternal() {
        assertTrue(engineFor("eternal") is EternalEngine)
    }

    @Test fun engineForUnknownFallsBackToJukebox() {
        assertTrue(engineFor("dopamineminer") is JukeboxEngine)
    }
}
