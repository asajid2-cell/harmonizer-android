package cc.harmonizerlabs.app.model

// ---------------------------------------------------------------------------
// Per-mode advanced settings — defaults match the web app's ADVANCED_GROUP_METADATA.
// Each inner class maps to one API group key.  toMap() produces the flat map
// that gets nested under that group key in the render request payload.
// ---------------------------------------------------------------------------

data class CanonOverlaySettings(
    val musicality: Float = 60f,
    val minOffsetBeats: Float = 4f,
    val maxOffsetBeats: Float = 32f,
    val dwellBeats: Float = 8f,
    val density: Float = 2f,
    val jumpBubbleBeats: Float = 2f,
    val variation: Float = 10f,
    val rlMinDwellBeats: Float = 4f,
    val rlRepeatPenalty: Float = 4f,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "musicality" to musicality.toDouble(),
        "minOffsetBeats" to minOffsetBeats.toDouble(),
        "maxOffsetBeats" to maxOffsetBeats.toDouble(),
        "dwellBeats" to dwellBeats.toDouble(),
        "density" to density.toDouble(),
        "jumpBubbleBeats" to jumpBubbleBeats.toDouble(),
        "variation" to variation.toDouble(),
        "rlMinDwellBeats" to rlMinDwellBeats.toDouble(),
        "rlRepeatPenalty" to rlRepeatPenalty.toDouble(),
    )
}

data class JukeboxLoopSettings(
    val musicality: Float = 50f,
    val minLoopBeats: Float = 8f,
    val maxSequentialBeats: Float = 32f,
    val loopThreshold: Float = 0.6f,
    val sectionBias: Float = 0.3f,
    val jumpVariance: Float = 0.3f,
    val routeLength: Float = 8f,
    val jumpTemperature: Float = 0.3f,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "musicality" to musicality.toDouble(),
        "minLoopBeats" to minLoopBeats.toDouble(),
        "maxSequentialBeats" to maxSequentialBeats.toDouble(),
        "loopThreshold" to loopThreshold.toDouble(),
        "sectionBias" to sectionBias.toDouble(),
        "jumpVariance" to jumpVariance.toDouble(),
        "routeLength" to routeLength.toDouble(),
        "jumpTemperature" to jumpTemperature.toDouble(),
    )
}

data class DopamineMinerSettings(
    val peakFraction: Float = 0.15f,
    val minClusterBeats: Float = 8f,
    val clusterGapBeats: Float = 2f,
    val largestClusterOnly: Boolean = false,
    val minDwellBeats: Float = 2f,
    val maxSequentialBeats: Float = 32f,
    val minJumpSpanBeats: Float = 4f,
    val minJumpSimilarity: Float = 0.5f,
    val crossClusterBias: Float = 0.5f,
    val burnoutWindowBeats: Float = 32f,
    val burnoutUniqueRatio: Float = 0.4f,
    val burnoutCooldownBeats: Float = 16f,
    val jumpTemperature: Float = 0.3f,
    val escapeProb: Float = 0.05f,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "peakFraction" to peakFraction.toDouble(),
        "minClusterBeats" to minClusterBeats.toDouble(),
        "clusterGapBeats" to clusterGapBeats.toDouble(),
        "largestClusterOnly" to if (largestClusterOnly) 1.0 else 0.0,
        "minDwellBeats" to minDwellBeats.toDouble(),
        "maxSequentialBeats" to maxSequentialBeats.toDouble(),
        "minJumpSpanBeats" to minJumpSpanBeats.toDouble(),
        "minJumpSimilarity" to minJumpSimilarity.toDouble(),
        "crossClusterBias" to crossClusterBias.toDouble(),
        "burnoutWindowBeats" to burnoutWindowBeats.toDouble(),
        "burnoutUniqueRatio" to burnoutUniqueRatio.toDouble(),
        "burnoutCooldownBeats" to burnoutCooldownBeats.toDouble(),
        "jumpTemperature" to jumpTemperature.toDouble(),
        "escapeProb" to escapeProb.toDouble(),
    )
}

data class HarmonicTrapSettings(
    val autoTarget: Boolean = true,
    val targetPitchClass: Float = 0f,   // 0=C … 11=B
    val similarityThreshold: Float = 0.6f,
    val graceBeats: Float = 2f,
    val cooldownBeats: Float = 4f,
    val searchTopK: Float = 8f,
    val minJumpSpanBeats: Float = 4f,
    val escapeProb: Float = 0.05f,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "autoTarget" to if (autoTarget) 1.0 else 0.0,
        "targetPitchClass" to targetPitchClass.toDouble(),
        "similarityThreshold" to similarityThreshold.toDouble(),
        "graceBeats" to graceBeats.toDouble(),
        "cooldownBeats" to cooldownBeats.toDouble(),
        "searchTopK" to searchTopK.toDouble(),
        "minJumpSpanBeats" to minJumpSpanBeats.toDouble(),
        "escapeProb" to escapeProb.toDouble(),
    )
}

data class PhaseShifterSettings(
    val rateDelta: Float = 0.003f,
    val overlayGain: Float = 0.5f,
    val resyncOnJump: Boolean = true,
    val resyncThresholdBeats: Float = 32f,
    val overlayLoop: Boolean = true,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "rateDelta" to rateDelta.toDouble(),
        "overlayGain" to overlayGain.toDouble(),
        "resyncOnJump" to if (resyncOnJump) 1.0 else 0.0,
        "resyncThresholdBeats" to resyncThresholdBeats.toDouble(),
        "overlayLoop" to if (overlayLoop) 1.0 else 0.0,
    )
}

data class GranularFreezeSettings(
    val freezeChance: Float = 0.3f,
    val cooldownBeats: Float = 4f,
    val repeatMode: Int = 1,            // 0=Short 1=Classic 2=Long
    val repeatLongBias: Float = 0.3f,
    val minVolume: Float = 0.2f,
    val sustainAttackMin: Float = 0.5f,
    val sustainSegDurMin: Float = 0.15f,
    val percussiveRatioMax: Float = 0.85f,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "freezeChance" to freezeChance.toDouble(),
        "cooldownBeats" to cooldownBeats.toDouble(),
        "repeatMode" to repeatMode.toDouble(),
        "repeatLongBias" to repeatLongBias.toDouble(),
        "minVolume" to minVolume.toDouble(),
        "sustainAttackMin" to sustainAttackMin.toDouble(),
        "sustainSegDurMin" to sustainSegDurMin.toDouble(),
        "percussiveRatioMax" to percussiveRatioMax.toDouble(),
    )
}

data class ElasticVelocitySettings(
    val minRate: Float = 0.5f,
    val maxRate: Float = 2.0f,
    val curve: Float = 1.5f,
    val smoothingBeats: Float = 4f,
    val maxDeltaPerBeat: Float = 0.2f,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "minRate" to minRate.toDouble(),
        "maxRate" to maxRate.toDouble(),
        "curve" to curve.toDouble(),
        "smoothingBeats" to smoothingBeats.toDouble(),
        "maxDeltaPerBeat" to maxDeltaPerBeat.toDouble(),
    )
}

data class MathRockerSettings(
    val cycleBeats: Float = 8f,
    val dropBeats: Float = 1f,
    val resetOnJump: Boolean = false,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "cycleBeats" to cycleBeats.toDouble(),
        "dropBeats" to dropBeats.toDouble(),
        "resetOnJump" to if (resetOnJump) 1.0 else 0.0,
    )
}

data class StalkerSettings(
    val similarityThreshold: Float = 0.75f,
    val cooldownBeats: Float = 8f,
    val armBeats: Float = 4f,
    val symmetricLookup: Boolean = true,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "similarityThreshold" to similarityThreshold.toDouble(),
        "cooldownBeats" to cooldownBeats.toDouble(),
        "armBeats" to armBeats.toDouble(),
        "symmetricLookup" to if (symmetricLookup) 1.0 else 0.0,
    )
}

data class TimbreSurfingSettings(
    val topK: Float = 8f,
    val minSimilarity: Float = 0.5f,
    val minJumpSpanBeats: Float = 4f,
    val excludeNeighborBeats: Float = 2f,
    val temperature: Float = 0.5f,
    val recentWindowBeats: Float = 32f,
    val repeatPenalty: Float = 0.3f,
    val applyChance: Float = 0.8f,
    val overrideJumps: Boolean = false,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "topK" to topK.toDouble(),
        "minSimilarity" to minSimilarity.toDouble(),
        "minJumpSpanBeats" to minJumpSpanBeats.toDouble(),
        "excludeNeighborBeats" to excludeNeighborBeats.toDouble(),
        "temperature" to temperature.toDouble(),
        "recentWindowBeats" to recentWindowBeats.toDouble(),
        "repeatPenalty" to repeatPenalty.toDouble(),
        "applyChance" to applyChance.toDouble(),
        "overrideJumps" to if (overrideJumps) 1.0 else 0.0,
    )
}

data class ChromaStackingSettings(
    val overlayGain: Float = 0.5f,
    val minChromaSimilarity: Float = 0.7f,
    val minTimbreDistance: Float = 50f,
    val excludeNeighborBeats: Float = 2f,
    val minJumpSpanBeats: Float = 8f,
    val searchTopK: Float = 8f,
    val randomSample: Float = 16f,
    val temperature: Float = 0.5f,
    val resampleBeats: Float = 8f,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "overlayGain" to overlayGain.toDouble(),
        "minChromaSimilarity" to minChromaSimilarity.toDouble(),
        "minTimbreDistance" to minTimbreDistance.toDouble(),
        "excludeNeighborBeats" to excludeNeighborBeats.toDouble(),
        "minJumpSpanBeats" to minJumpSpanBeats.toDouble(),
        "searchTopK" to searchTopK.toDouble(),
        "randomSample" to randomSample.toDouble(),
        "temperature" to temperature.toDouble(),
        "resampleBeats" to resampleBeats.toDouble(),
    )
}

data class BeatSortingSettings(
    val feature: Int = 2,               // 0=Pitch 1=Brightness 2=Loudness
    val direction: Int = 0,             // 0=Ascending 1=Descending
    val minVolume: Float = 0.1f,
    val repeatEach: Float = 1f,
    val overrideJumps: Boolean = false,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "feature" to feature.toDouble(),
        "direction" to direction.toDouble(),
        "minVolume" to minVolume.toDouble(),
        "repeatEach" to repeatEach.toDouble(),
        "overrideJumps" to if (overrideJumps) 1.0 else 0.0,
    )
}

data class ReverseBloomSettings(
    val triggerThreshold: Float = 0.3f,
    val rewindBeats: Float = 16f,
    val rewindChance: Float = 0.7f,
    val cooldownBeats: Float = 8f,
    val resumeMode: Int = 1,            // 0=Linear 1=Bloom Hop
    val minSimilarity: Float = 0.5f,
    val bloomMinSpanBeats: Float = 16f,
    val bloomTopK: Float = 8f,
    val bloomTemperature: Float = 0.5f,
    val overrideJumps: Boolean = false,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "triggerThreshold" to triggerThreshold.toDouble(),
        "rewindBeats" to rewindBeats.toDouble(),
        "rewindChance" to rewindChance.toDouble(),
        "cooldownBeats" to cooldownBeats.toDouble(),
        "resumeMode" to resumeMode.toDouble(),
        "minSimilarity" to minSimilarity.toDouble(),
        "bloomMinSpanBeats" to bloomMinSpanBeats.toDouble(),
        "bloomTopK" to bloomTopK.toDouble(),
        "bloomTemperature" to bloomTemperature.toDouble(),
        "overrideJumps" to if (overrideJumps) 1.0 else 0.0,
    )
}

data class BarberPoleSettings(
    val feature: Int = 0,               // 0=Loudness 1=Brightness 2=Pitch
    val direction: Int = 1,             // 0=Down 1=Up
    val stepRanks: Float = 4f,
    val minSimilarity: Float = 0.4f,
    val minVolume: Float = 0.1f,
    val minSpanBeats: Float = 8f,
    val excludeNeighborBeats: Float = 2f,
    val topK: Float = 16f,
    val temperature: Float = 0.5f,
    val recentWindowBeats: Float = 64f,
    val repeatPenalty: Float = 0.3f,
    val applyChance: Float = 1.0f,
    val overrideJumps: Boolean = true,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "feature" to feature.toDouble(),
        "direction" to direction.toDouble(),
        "stepRanks" to stepRanks.toDouble(),
        "minSimilarity" to minSimilarity.toDouble(),
        "minVolume" to minVolume.toDouble(),
        "minSpanBeats" to minSpanBeats.toDouble(),
        "excludeNeighborBeats" to excludeNeighborBeats.toDouble(),
        "topK" to topK.toDouble(),
        "temperature" to temperature.toDouble(),
        "recentWindowBeats" to recentWindowBeats.toDouble(),
        "repeatPenalty" to repeatPenalty.toDouble(),
        "applyChance" to applyChance.toDouble(),
        "overrideJumps" to if (overrideJumps) 1.0 else 0.0,
    )
}

data class PalindromeEngineSettings(
    val phraseBeats: Float = 16f,
    val turnMinSimilarity: Float = 0.5f,
    val turnTopK: Float = 8f,
    val turnTemperature: Float = 0.5f,
    val minTurnSpanBeats: Float = 8f,
    val excludeNeighborBeats: Float = 4f,
    val flipCooldownBeats: Float = 16f,
    val applyChance: Float = 1.0f,
    val overrideJumps: Boolean = true,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "phraseBeats" to phraseBeats.toDouble(),
        "turnMinSimilarity" to turnMinSimilarity.toDouble(),
        "turnTopK" to turnTopK.toDouble(),
        "turnTemperature" to turnTemperature.toDouble(),
        "minTurnSpanBeats" to minTurnSpanBeats.toDouble(),
        "excludeNeighborBeats" to excludeNeighborBeats.toDouble(),
        "flipCooldownBeats" to flipCooldownBeats.toDouble(),
        "applyChance" to applyChance.toDouble(),
        "overrideJumps" to if (overrideJumps) 1.0 else 0.0,
    )
}

data class SpectralGravitySettings(
    val axis: Int = 0,                  // 0=Brightness 1=Loudness 2=Pitch
    val target: Float = 0.5f,
    val bandWidth: Float = 0.2f,
    val triggerThreshold: Float = 0.3f,
    val minSimilarity: Float = 0.4f,
    val cooldownBeats: Float = 4f,
    val minSpanBeats: Float = 4f,
    val excludeNeighborBeats: Float = 2f,
    val topK: Float = 16f,
    val temperature: Float = 0.5f,
    val recentWindowBeats: Float = 32f,
    val repeatPenalty: Float = 0.3f,
    val applyChance: Float = 1.0f,
    val overrideJumps: Boolean = true,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "axis" to axis.toDouble(),
        "target" to target.toDouble(),
        "bandWidth" to bandWidth.toDouble(),
        "triggerThreshold" to triggerThreshold.toDouble(),
        "minSimilarity" to minSimilarity.toDouble(),
        "cooldownBeats" to cooldownBeats.toDouble(),
        "minSpanBeats" to minSpanBeats.toDouble(),
        "excludeNeighborBeats" to excludeNeighborBeats.toDouble(),
        "topK" to topK.toDouble(),
        "temperature" to temperature.toDouble(),
        "recentWindowBeats" to recentWindowBeats.toDouble(),
        "repeatPenalty" to repeatPenalty.toDouble(),
        "applyChance" to applyChance.toDouble(),
        "overrideJumps" to if (overrideJumps) 1.0 else 0.0,
    )
}

data class CallResponseSettings(
    val callQuantileMax: Float = 0.4f,
    val responseQuantileMin: Float = 0.6f,
    val barsPerCall: Float = 4f,
    val barsPerResponse: Float = 4f,
    val minSimilarity: Float = 0.4f,
    val minSpanBeats: Float = 4f,
    val excludeNeighborBeats: Float = 2f,
    val topK: Float = 16f,
    val temperature: Float = 0.5f,
    val recentWindowBars: Float = 8f,
    val repeatPenalty: Float = 0.3f,
    val energyBias: Float = 0.5f,
    val sameSectionBias: Float = 0.3f,
    val applyChance: Float = 1.0f,
    val overrideJumps: Boolean = false,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "callQuantileMax" to callQuantileMax.toDouble(),
        "responseQuantileMin" to responseQuantileMin.toDouble(),
        "barsPerCall" to barsPerCall.toDouble(),
        "barsPerResponse" to barsPerResponse.toDouble(),
        "minSimilarity" to minSimilarity.toDouble(),
        "minSpanBeats" to minSpanBeats.toDouble(),
        "excludeNeighborBeats" to excludeNeighborBeats.toDouble(),
        "topK" to topK.toDouble(),
        "temperature" to temperature.toDouble(),
        "recentWindowBars" to recentWindowBars.toDouble(),
        "repeatPenalty" to repeatPenalty.toDouble(),
        "energyBias" to energyBias.toDouble(),
        "sameSectionBias" to sameSectionBias.toDouble(),
        "applyChance" to applyChance.toDouble(),
        "overrideJumps" to if (overrideJumps) 1.0 else 0.0,
    )
}

data class OrbitWeaverSettings(
    val anchorCount: Float = 4f,
    val spinAxis: Int = 0,              // 0=Energy 1=Brightness 2=Pitch
    val barsPerAnchor: Float = 4f,
    val jumpAtBarStart: Boolean = true,
    val anchorPull: Float = 0.7f,
    val minSimilarity: Float = 0.4f,
    val minSpanBeats: Float = 4f,
    val excludeNeighborBeats: Float = 2f,
    val topK: Float = 16f,
    val temperature: Float = 0.5f,
    val recentWindowBeats: Float = 64f,
    val repeatPenalty: Float = 0.3f,
    val sameSectionBias: Float = 0.3f,
    val applyChance: Float = 1.0f,
    val overrideJumps: Boolean = false,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "anchorCount" to anchorCount.toDouble(),
        "spinAxis" to spinAxis.toDouble(),
        "barsPerAnchor" to barsPerAnchor.toDouble(),
        "jumpAtBarStart" to if (jumpAtBarStart) 1.0 else 0.0,
        "anchorPull" to anchorPull.toDouble(),
        "minSimilarity" to minSimilarity.toDouble(),
        "minSpanBeats" to minSpanBeats.toDouble(),
        "excludeNeighborBeats" to excludeNeighborBeats.toDouble(),
        "topK" to topK.toDouble(),
        "temperature" to temperature.toDouble(),
        "recentWindowBeats" to recentWindowBeats.toDouble(),
        "repeatPenalty" to repeatPenalty.toDouble(),
        "sameSectionBias" to sameSectionBias.toDouble(),
        "applyChance" to applyChance.toDouble(),
        "overrideJumps" to if (overrideJumps) 1.0 else 0.0,
    )
}

data class SculptorConfigSettings(
    val durationScale: Float = 1.0f,
    val minSectionSeconds: Float = 5f,
    val maxSectionSeconds: Float = 60f,
    val previewSeconds: Float = 3f,
    val transitionOverlapSeconds: Float = 0f,
) {
    fun toMap(): Map<String, Double> = mapOf(
        "durationScale" to durationScale.toDouble(),
        "minSectionSeconds" to minSectionSeconds.toDouble(),
        "maxSectionSeconds" to maxSectionSeconds.toDouble(),
        "previewSeconds" to previewSeconds.toDouble(),
        "transitionOverlapSeconds" to transitionOverlapSeconds.toDouble(),
    )
}

// ---------------------------------------------------------------------------
// Master settings container — one instance lives in PlayerViewModel.
// toApiMap() produces the nested map the /api/background-render endpoint
// expects as "advancedSettings".
// ---------------------------------------------------------------------------

data class AdvancedSettings(
    // Group objects (one per API group key)
    val canonOverlay: CanonOverlaySettings = CanonOverlaySettings(),
    val jukeboxLoop: JukeboxLoopSettings = JukeboxLoopSettings(),
    // Eternal reuses the canon-overlay and jukebox-loop groups
    val eternalOverlay: CanonOverlaySettings = CanonOverlaySettings(),
    val eternalLoop: JukeboxLoopSettings = JukeboxLoopSettings(),
    val dopamineMiner: DopamineMinerSettings = DopamineMinerSettings(),
    val harmonicTrap: HarmonicTrapSettings = HarmonicTrapSettings(),
    val phaseShifter: PhaseShifterSettings = PhaseShifterSettings(),
    val granularFreeze: GranularFreezeSettings = GranularFreezeSettings(),
    val elasticVelocity: ElasticVelocitySettings = ElasticVelocitySettings(),
    val mathRocker: MathRockerSettings = MathRockerSettings(),
    val stalker: StalkerSettings = StalkerSettings(),
    val timbreSurfing: TimbreSurfingSettings = TimbreSurfingSettings(),
    val chromaStacking: ChromaStackingSettings = ChromaStackingSettings(),
    val beatSorting: BeatSortingSettings = BeatSortingSettings(),
    val reverseBloom: ReverseBloomSettings = ReverseBloomSettings(),
    val barberPole: BarberPoleSettings = BarberPoleSettings(),
    val palindromeEngine: PalindromeEngineSettings = PalindromeEngineSettings(),
    val spectralGravity: SpectralGravitySettings = SpectralGravitySettings(),
    val callResponse: CallResponseSettings = CallResponseSettings(),
    val orbitWeaver: OrbitWeaverSettings = OrbitWeaverSettings(),
    val sculptorConfig: SculptorConfigSettings = SculptorConfigSettings(),
    // Top-level controls (not inside a group)
    val phaseIntensity: Float = 1.0f,   // 0-4, Phase Shifter mode only
    val baseAudioOnly: Boolean = false,  // mute overlay voices for A/B comparison
) {
    /** Returns the group map the render API expects for the given mode. */
    fun toApiMap(modeKey: String): Map<String, Map<String, Double>> = when (modeKey) {
        "canon"          -> mapOf("canonOverlay" to canonOverlay.toMap())
        "jukebox"        -> mapOf("jukeboxLoop" to jukeboxLoop.toMap())
        "eternal"        -> mapOf(
                                "eternalOverlay" to eternalOverlay.toMap(),
                                "eternalLoop" to eternalLoop.toMap(),
                            )
        "dopamine"       -> mapOf("dopamineMiner" to dopamineMiner.toMap())
        "harmonictrap"   -> mapOf("harmonicTrap" to harmonicTrap.toMap())
        "phaseshifter"   -> mapOf("phaseShifter" to phaseShifter.toMap())
        "granularfreeze" -> mapOf("granularFreeze" to granularFreeze.toMap())
        "elasticvelo"    -> mapOf("elasticVelocity" to elasticVelocity.toMap())
        "mathrocker"     -> mapOf("mathRocker" to mathRocker.toMap())
        "stalker"        -> mapOf("stalker" to stalker.toMap())
        "timbresurf"     -> mapOf("timbreSurfing" to timbreSurfing.toMap())
        "chromastack"    -> mapOf("chromaStacking" to chromaStacking.toMap())
        "beatsort"       -> mapOf("beatSorting" to beatSorting.toMap())
        "reversebloom"   -> mapOf("reverseBloom" to reverseBloom.toMap())
        "barberpole"     -> mapOf("barberPole" to barberPole.toMap())
        "palindrome"     -> mapOf("palindromeEngine" to palindromeEngine.toMap())
        "spectralgravity"-> mapOf("spectralGravity" to spectralGravity.toMap())
        "callresponse"   -> mapOf("callResponse" to callResponse.toMap())
        "orbitweaver"    -> mapOf("orbitWeaver" to orbitWeaver.toMap())
        "sculptor"       -> mapOf("sculptorConfig" to sculptorConfig.toMap())
        else             -> emptyMap()
    }

    /** True if the given mode has a live advanced settings panel. */
    companion object {
        val MODES_WITH_SETTINGS = setOf(
            "canon", "jukebox", "eternal", "dopamine", "harmonictrap",
            "phaseshifter", "granularfreeze", "elasticvelo", "mathrocker",
            "stalker", "timbresurf", "chromastack", "beatsort", "reversebloom",
            "barberpole", "palindrome", "spectralgravity", "callresponse",
            "orbitweaver", "sculptor",
        )

        /** Modes that use an overlay player (show voice count slider). */
        val OVERLAY_MODES = setOf("canon", "eternal", "phaseshifter", "chromastack")
    }
}
