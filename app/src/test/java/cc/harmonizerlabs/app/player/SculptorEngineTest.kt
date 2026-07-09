package cc.harmonizerlabs.app.player

import cc.harmonizerlabs.app.api.models.*
import org.junit.Assert.*
import org.junit.Test

class SculptorEngineTest {

    // ── Fixture: 3 sections of 3s each, one beat per second (9 beats) ──────────
    //   section 0 -> beats 0,1,2   section 1 -> beats 3,4,5   section 2 -> beats 6,7,8
    private fun beat(start: Double, section: Int) = Beat(start = start, duration = 1.0, section = section)
    private fun section(start: Double, dur: Double) = Section(start = start, duration = dur)

    private val beats = listOf(
        beat(0.0, 0), beat(1.0, 0), beat(2.0, 0),
        beat(3.0, 1), beat(4.0, 1), beat(5.0, 1),
        beat(6.0, 2), beat(7.0, 2), beat(8.0, 2),
    )
    private val sections = listOf(section(0.0, 3.0), section(3.0, 3.0), section(6.0, 3.0))
    private val analysis = TrackAnalysis(beats = beats, segments = emptyList(), sections = sections)
    private val track = TrackData(
        id = "t", info = TrackInfo("/a.flac"),
        audioSummary = AudioSummary(duration = 9.0), analysis = analysis,
    )
    private fun ctx() = PlaybackContext(track = track)

    // ── Natural order: plays linearly, jumps to next section at each boundary ──

    @Test fun naturalOrderAdvancesLinearlyWithinSection() {
        val e = SculptorEngine()  // empty arrangement => natural [0,1,2]
        assertEquals(1, e.nextBeatIndex(0, analysis, ctx()))
        assertEquals(2, e.nextBeatIndex(1, analysis, ctx()))
    }

    @Test fun naturalOrderJumpsToNextSectionAtBoundary() {
        val e = SculptorEngine()
        // walk through section 0 then expect a jump to section 1's first beat (3)
        e.nextBeatIndex(0, analysis, ctx())
        e.nextBeatIndex(1, analysis, ctx())
        assertEquals(3, e.nextBeatIndex(2, analysis, ctx())) // end of section 0 -> first of section 1
    }

    @Test fun naturalOrderLoopsBackAfterLastSection() {
        val e = SculptorEngine()
        // advance step through sections 0 -> 1 -> 2
        e.nextBeatIndex(2, analysis, ctx()) // -> step 1 (beat 3)
        e.nextBeatIndex(5, analysis, ctx()) // -> step 2 (beat 6)
        assertEquals(0, e.nextBeatIndex(8, analysis, ctx())) // end of last section -> loop to section 0 (beat 0)
    }

    // ── Custom arrangement order ──────────────────────────────────────────────

    @Test fun restartReturnsFirstSectionFirstBeat() {
        val e = SculptorEngine().apply { arrangement = listOf(1, 2, 0) }
        assertEquals(3, e.restart(analysis)) // first section in order is 1 -> beat 3
    }

    @Test fun customOrderJumpsToArrangedNextSection() {
        val e = SculptorEngine().apply { arrangement = listOf(2, 0, 1) }
        e.restart(analysis) // step 0, playing section 2 (beats 6..8)
        assertEquals(7, e.nextBeatIndex(6, analysis, ctx()))           // linear within section 2
        assertEquals(0, e.nextBeatIndex(8, analysis, ctx()))           // section 2 end -> arranged next is section 0 (beat 0)
    }

    @Test fun singleSectionArrangementLoopsOnItself() {
        val e = SculptorEngine().apply { arrangement = listOf(1) }
        e.restart(analysis)
        // section 1 = beats 3,4,5; at its end it should loop back to its own first beat
        assertEquals(4, e.nextBeatIndex(3, analysis, ctx()))
        assertEquals(3, e.nextBeatIndex(5, analysis, ctx())) // end of only section -> back to its start
    }

    // ── Dispatch ──────────────────────────────────────────────────────────────

    @Test fun engineForSculptorReturnsSculptor() {
        assertTrue(engineFor("sculptor") is SculptorEngine)
    }

    @Test fun emptyAnalysisAdvancesLinearly() {
        val e = SculptorEngine()
        val empty = TrackAnalysis(beats = emptyList(), segments = emptyList(), sections = emptyList())
        assertEquals(0, e.nextBeatIndex(0, empty, ctx()))
    }
}
