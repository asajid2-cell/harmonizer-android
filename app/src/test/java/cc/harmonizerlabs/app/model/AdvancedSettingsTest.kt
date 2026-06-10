package cc.harmonizerlabs.app.model

import org.junit.Assert.*
import org.junit.Test

class AdvancedSettingsTest {

    // ── Default value contracts (must match web app ADVANCED_GROUP_METADATA) ──

    @Test fun canonDefaults() {
        val s = AdvancedSettings().canonOverlay
        assertEquals(60f, s.musicality, 0f)
        assertEquals(4f,  s.minOffsetBeats, 0f)
        assertEquals(32f, s.maxOffsetBeats, 0f)
        assertEquals(8f,  s.dwellBeats, 0f)
        assertEquals(2f,  s.density, 0f)
    }

    @Test fun jukeboxDefaults() {
        val s = AdvancedSettings().jukeboxLoop
        assertEquals(50f,  s.musicality, 0f)
        assertEquals(8f,   s.minLoopBeats, 0f)
        assertEquals(32f,  s.maxSequentialBeats, 0f)
        assertEquals(0.6f, s.loopThreshold, 0.001f)
    }

    @Test fun dopamineDefaults() {
        val s = AdvancedSettings().dopamineMiner
        assertEquals(0.15f, s.peakFraction, 0.001f)
        assertEquals(8f,    s.minClusterBeats, 0f)
        assertEquals(0.3f,  s.jumpTemperature, 0.001f)
        assertFalse(s.largestClusterOnly)
    }

    @Test fun harmonicTrapDefaults() {
        val s = AdvancedSettings().harmonicTrap
        assertTrue(s.autoTarget)
        assertEquals(0f,   s.targetPitchClass, 0f)
        assertEquals(0.6f, s.similarityThreshold, 0.001f)
    }

    @Test fun phaseShifterDefaults() {
        val s = AdvancedSettings().phaseShifter
        assertEquals(0.003f, s.rateDelta, 0.0001f)
        assertEquals(0.5f,   s.overlayGain, 0.001f)
        assertTrue(s.resyncOnJump)
        assertTrue(s.overlayLoop)
    }

    @Test fun globalDefaults() {
        val a = AdvancedSettings()
        assertEquals(1.0f, a.phaseIntensity, 0.001f)
        assertFalse(a.baseAudioOnly)
    }

    // ── toApiMap() shape contracts ────────────────────────────────────────────

    @Test fun canonApiMap() {
        val map = AdvancedSettings().toApiMap("canon")
        assertTrue("should have canonOverlay key", map.containsKey("canonOverlay"))
        assertEquals(1, map.size)
        val inner = map["canonOverlay"]!!
        assertEquals(60.0, inner["musicality"]!!, 0.0)
        assertEquals(4.0,  inner["minOffsetBeats"]!!, 0.0)
    }

    @Test fun eternalApiMapHasTwoGroups() {
        val map = AdvancedSettings().toApiMap("eternal")
        assertTrue(map.containsKey("eternalOverlay"))
        assertTrue(map.containsKey("eternalLoop"))
        assertEquals(2, map.size)
    }

    @Test fun jukeboxApiMap() {
        val map = AdvancedSettings().toApiMap("jukebox")
        assertTrue(map.containsKey("jukeboxLoop"))
        assertEquals(1, map.size)
    }

    @Test fun granularFreezeApiMap() {
        val map = AdvancedSettings().toApiMap("granularfreeze")
        assertTrue(map.containsKey("granularFreeze"))
        val inner = map["granularFreeze"]!!
        assertEquals(0.3, inner["freezeChance"]!!, 0.001)
    }

    @Test fun unknownModeReturnsEmptyMap() {
        val map = AdvancedSettings().toApiMap("notamode")
        assertTrue(map.isEmpty())
    }

    // ── BooleanEncoding — booleans must be 0.0/1.0 doubles ───────────────────

    @Test fun booleanEncodingTrue() {
        val s = AdvancedSettings().harmonicTrap.copy(autoTarget = true)
        assertEquals(1.0, s.toMap()["autoTarget"]!!, 0.0)
    }

    @Test fun booleanEncodingFalse() {
        val s = AdvancedSettings().harmonicTrap.copy(autoTarget = false)
        assertEquals(0.0, s.toMap()["autoTarget"]!!, 0.0)
    }

    // ── MODES_WITH_SETTINGS coverage ─────────────────────────────────────────

    @Test fun allModesWithSettingsProduceNonEmptyMap() {
        val settings = AdvancedSettings()
        for (mode in AdvancedSettings.MODES_WITH_SETTINGS) {
            val map = settings.toApiMap(mode)
            assertFalse("toApiMap('$mode') should not be empty", map.isEmpty())
        }
    }

    @Test fun overlayModesSubsetOfModesWithSettings() {
        assertTrue(
            AdvancedSettings.MODES_WITH_SETTINGS.containsAll(AdvancedSettings.OVERLAY_MODES)
        )
    }

    @Test fun expectedModesPresent() {
        val required = setOf(
            "canon", "jukebox", "eternal", "dopamine", "harmonictrap",
            "phaseshifter", "granularfreeze", "elasticvelo", "mathrocker",
            "stalker", "timbresurf", "chromastack", "beatsort",
            "reversebloom", "barberpole", "palindrome", "spectralgravity",
            "callresponse", "orbitweaver", "sculptor",
        )
        assertTrue(AdvancedSettings.MODES_WITH_SETTINGS.containsAll(required))
    }

    // ── Per-field serialisation spot-checks ───────────────────────────────────

    @Test fun stalkerMapKeys() {
        val keys = AdvancedSettings().stalker.toMap().keys
        assertTrue(keys.containsAll(listOf("similarityThreshold", "cooldownBeats", "armBeats", "symmetricLookup")))
    }

    @Test fun mathRockerMapKeys() {
        val keys = AdvancedSettings().mathRocker.toMap().keys
        assertTrue(keys.containsAll(listOf("cycleBeats", "dropBeats", "resetOnJump")))
    }
}
