package cc.harmonizerlabs.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.harmonizerlabs.app.model.*
import cc.harmonizerlabs.app.ui.theme.*

// ---------------------------------------------------------------------------
// Advanced Settings Panel — renders mode-specific knobs inside the player
// screen's advanced section.  Each mode group is a collapsible sub-section.
// ---------------------------------------------------------------------------

@Composable
fun AdvancedSettingsPanel(
    modeKey: String,
    settings: AdvancedSettings,
    onUpdate: (AdvancedSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (modeKey !in AdvancedSettings.MODES_WITH_SETTINGS) {
        Text(
            "No advanced settings for this mode.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = modifier.padding(vertical = 4.dp),
        )
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        when (modeKey) {
            "canon"          -> CanonPanel(settings, onUpdate)
            "jukebox"        -> JukeboxPanel(settings, onUpdate)
            "eternal"        -> {
                SettingsGroup("CANON OVERLAY") { CanonPanel(settings, onUpdate, groupOnly = true) }
                SettingsGroup("JUKEBOX LOOP") { JukeboxPanel(settings, onUpdate, groupOnly = true) }
            }
            "dopamine"       -> DopaminePanel(settings) { onUpdate(it) }
            "harmonictrap"   -> HarmonicTrapPanel(settings) { onUpdate(it) }
            "phaseshifter"   -> PhaseShifterPanel(settings) { onUpdate(it) }
            "granularfreeze" -> GranularFreezePanel(settings) { onUpdate(it) }
            "elasticvelo"    -> ElasticVelocityPanel(settings) { onUpdate(it) }
            "mathrocker"     -> MathRockerPanel(settings) { onUpdate(it) }
            "stalker"        -> StalkerPanel(settings) { onUpdate(it) }
            "timbresurf"     -> TimbreSurfingPanel(settings) { onUpdate(it) }
            "chromastack"    -> ChromaStackingPanel(settings) { onUpdate(it) }
            "beatsort"       -> BeatSortingPanel(settings) { onUpdate(it) }
            "reversebloom"   -> ReverseBloomPanel(settings) { onUpdate(it) }
            "barberpole"     -> BarberPolePanel(settings) { onUpdate(it) }
            "palindrome"     -> PalindromePanel(settings) { onUpdate(it) }
            "spectralgravity"-> SpectralGravityPanel(settings) { onUpdate(it) }
            "callresponse"   -> CallResponsePanel(settings) { onUpdate(it) }
            "orbitweaver"    -> OrbitWeaverPanel(settings) { onUpdate(it) }
            "sculptor"       -> SculptorConfigPanel(settings) { onUpdate(it) }
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .border(1.dp, NeonMagenta.copy(alpha = 0.3f))
            .background(Black),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                title,
                style    = MaterialTheme.typography.labelSmall,
                color    = NeonMagenta,
                modifier = Modifier.weight(1f),
            )
            Text(if (expanded) "▲" else "▼", style = MaterialTheme.typography.labelSmall, color = NeonMagenta)
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp).padding(bottom = 10.dp),
                content = content,
            )
        }
    }
}

/** Float slider with a 2-decimal label. */
@Composable
private fun FSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    onValueChange: (Float) -> Unit,
    format: (Float) -> String = { "%.2f".format(it) },
) {
    NeonSlider(
        label         = label,
        value         = value,
        onValueChange = onValueChange,
        valueRange    = min..max,
        valueLabel    = format(value),
        modifier      = Modifier.padding(vertical = 4.dp),
    )
}

/** Integer slider. */
@Composable
private fun ISlider(label: String, value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit) {
    val steps = (max - min - 1).toInt().coerceAtLeast(0)
    NeonSlider(
        label         = label,
        value         = value,
        onValueChange = onValueChange,
        valueRange    = min..max,
        steps         = steps,
        valueLabel    = value.toInt().toString(),
        modifier      = Modifier.padding(vertical = 4.dp),
    )
}

/** Boolean toggle rendered as OFF/ON buttons. */
@Composable
private fun BoolToggle(label: String, value: Boolean, onValueChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            NeonButton("OFF", onClick = { onValueChange(false) }, active = !value, borderColor = NeonCyan)
            NeonButton("ON",  onClick = { onValueChange(true) },  active = value,  borderColor = NeonCyan)
        }
    }
}

/** 3-option enum toggle (index 0/1/2). */
@Composable
private fun EnumToggle3(label: String, value: Int, opt0: String, opt1: String, opt2: String, onValueChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            NeonButton(opt0, onClick = { onValueChange(0) }, active = value == 0, borderColor = NeonCyan, modifier = Modifier.weight(1f))
            NeonButton(opt1, onClick = { onValueChange(1) }, active = value == 1, borderColor = NeonCyan, modifier = Modifier.weight(1f))
            NeonButton(opt2, onClick = { onValueChange(2) }, active = value == 2, borderColor = NeonCyan, modifier = Modifier.weight(1f))
        }
    }
}

/** 2-option enum toggle. */
@Composable
private fun EnumToggle2(label: String, value: Int, opt0: String, opt1: String, onValueChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            NeonButton(opt0, onClick = { onValueChange(0) }, active = value == 0, borderColor = NeonCyan)
            NeonButton(opt1, onClick = { onValueChange(1) }, active = value == 1, borderColor = NeonCyan)
        }
    }
}

// ── Mode panels ──────────────────────────────────────────────────────────────

@Composable
private fun CanonPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit, groupOnly: Boolean = false) {
    val c = s.canonOverlay
    val upd = { n: CanonOverlaySettings -> onUpdate(s.copy(canonOverlay = n)) }
    @Composable fun content() {
        FSlider("Musicality %", c.musicality, 0f, 100f, { upd(c.copy(musicality = it)) }, { "${it.toInt()}%" })
        ISlider("Min Offset Beats", c.minOffsetBeats, 1f, 192f) { upd(c.copy(minOffsetBeats = it)) }
        ISlider("Max Offset Beats", c.maxOffsetBeats, 2f, 256f) { upd(c.copy(maxOffsetBeats = it)) }
        ISlider("Dwell Beats", c.dwellBeats, 1f, 64f) { upd(c.copy(dwellBeats = it)) }
        ISlider("Density", c.density, 1f, 16f) { upd(c.copy(density = it)) }
        ISlider("Jump Bubble Beats", c.jumpBubbleBeats, 0f, 64f) { upd(c.copy(jumpBubbleBeats = it)) }
        ISlider("Variation", c.variation, 0f, 50f) { upd(c.copy(variation = it)) }
        ISlider("RL Min Dwell", c.rlMinDwellBeats, 2f, 64f) { upd(c.copy(rlMinDwellBeats = it)) }
        ISlider("RL Repeat Penalty", c.rlRepeatPenalty, 0f, 32f) { upd(c.copy(rlRepeatPenalty = it)) }
    }
    if (groupOnly) { content() } else { SettingsGroup("AUTOCANONIZER") { content() } }
}

@Composable
private fun JukeboxPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit, groupOnly: Boolean = false) {
    val j = s.jukeboxLoop
    val upd = { n: JukeboxLoopSettings -> onUpdate(s.copy(jukeboxLoop = n)) }
    @Composable fun content() {
        FSlider("Musicality %", j.musicality, 0f, 100f, { upd(j.copy(musicality = it)) }, { "${it.toInt()}%" })
        ISlider("Min Loop Beats", j.minLoopBeats, 4f, 64f) { upd(j.copy(minLoopBeats = it)) }
        ISlider("Max Sequential Beats", j.maxSequentialBeats, 8f, 128f) { upd(j.copy(maxSequentialBeats = it)) }
        FSlider("Loop Threshold", j.loopThreshold, 0.3f, 0.95f, { upd(j.copy(loopThreshold = it)) })
        FSlider("Section Bias", j.sectionBias, 0f, 1f, { upd(j.copy(sectionBias = it)) })
        FSlider("Jump Variance", j.jumpVariance, 0f, 1f, { upd(j.copy(jumpVariance = it)) })
        ISlider("Route Length", j.routeLength, 4f, 32f) { upd(j.copy(routeLength = it)) }
        FSlider("Temperature", j.jumpTemperature, 0.05f, 0.8f, { upd(j.copy(jumpTemperature = it)) })
    }
    if (groupOnly) { content() } else { SettingsGroup("ETERNAL JUKEBOX") { content() } }
}

@Composable
private fun DopaminePanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val d = s.dopamineMiner
    val upd = { n: DopamineMinerSettings -> onUpdate(s.copy(dopamineMiner = n)) }
    SettingsGroup("DOPAMINE MINER") {
        FSlider("Peak Fraction", d.peakFraction, 0.02f, 0.5f, { upd(d.copy(peakFraction = it)) })
        ISlider("Min Cluster Beats", d.minClusterBeats, 4f, 128f) { upd(d.copy(minClusterBeats = it)) }
        ISlider("Cluster Gap Beats", d.clusterGapBeats, 0f, 8f) { upd(d.copy(clusterGapBeats = it)) }
        BoolToggle("Largest Cluster Only", d.largestClusterOnly) { upd(d.copy(largestClusterOnly = it)) }
        ISlider("Min Dwell Beats", d.minDwellBeats, 1f, 32f) { upd(d.copy(minDwellBeats = it)) }
        ISlider("Max Sequential", d.maxSequentialBeats, 4f, 128f) { upd(d.copy(maxSequentialBeats = it)) }
        ISlider("Min Jump Span", d.minJumpSpanBeats, 1f, 64f) { upd(d.copy(minJumpSpanBeats = it)) }
        FSlider("Min Jump Similarity", d.minJumpSimilarity, 0.3f, 0.98f, { upd(d.copy(minJumpSimilarity = it)) })
        FSlider("Cross Cluster Bias", d.crossClusterBias, 0f, 1f, { upd(d.copy(crossClusterBias = it)) })
        ISlider("Burnout Window", d.burnoutWindowBeats, 8f, 256f) { upd(d.copy(burnoutWindowBeats = it)) }
        FSlider("Burnout Unique Ratio", d.burnoutUniqueRatio, 0.1f, 0.9f, { upd(d.copy(burnoutUniqueRatio = it)) })
        ISlider("Burnout Cooldown", d.burnoutCooldownBeats, 0f, 256f) { upd(d.copy(burnoutCooldownBeats = it)) }
        FSlider("Temperature", d.jumpTemperature, 0.05f, 0.8f, { upd(d.copy(jumpTemperature = it)) })
        FSlider("Escape Prob", d.escapeProb, 0f, 0.2f, { upd(d.copy(escapeProb = it)) })
    }
}

@Composable
private fun HarmonicTrapPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val h = s.harmonicTrap
    val upd = { n: HarmonicTrapSettings -> onUpdate(s.copy(harmonicTrap = n)) }
    val noteNames = listOf("C","C#","D","D#","E","F","F#","G","G#","A","A#","B")
    SettingsGroup("HARMONIC TRAP") {
        BoolToggle("Auto-Detect Key", h.autoTarget) { upd(h.copy(autoTarget = it)) }
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("Target Key", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(4.dp))
            ISlider("", h.targetPitchClass, 0f, 11f, { upd(h.copy(targetPitchClass = it)) })
            Text(noteNames[h.targetPitchClass.toInt().coerceIn(0, 11)],
                style = MaterialTheme.typography.labelMedium, color = NeonCyan,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        FSlider("Similarity Threshold", h.similarityThreshold, 0.3f, 0.98f, { upd(h.copy(similarityThreshold = it)) })
        ISlider("Grace Beats", h.graceBeats, 0f, 8f) { upd(h.copy(graceBeats = it)) }
        ISlider("Cooldown Beats", h.cooldownBeats, 0f, 64f) { upd(h.copy(cooldownBeats = it)) }
        ISlider("Search Top-K", h.searchTopK, 1f, 16f) { upd(h.copy(searchTopK = it)) }
        ISlider("Min Jump Span", h.minJumpSpanBeats, 1f, 64f) { upd(h.copy(minJumpSpanBeats = it)) }
        FSlider("Escape Prob", h.escapeProb, 0f, 0.2f, { upd(h.copy(escapeProb = it)) })
    }
}

@Composable
private fun PhaseShifterPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val p = s.phaseShifter
    val upd = { n: PhaseShifterSettings -> onUpdate(s.copy(phaseShifter = n)) }
    SettingsGroup("PHASE SHIFTER") {
        FSlider("Rate Delta", p.rateDelta, 0f, 0.02f, { upd(p.copy(rateDelta = it)) }, { "%.4f".format(it) })
        FSlider("Overlay Gain", p.overlayGain, 0f, 1f, { upd(p.copy(overlayGain = it)) })
        BoolToggle("Resync On Jump", p.resyncOnJump) { upd(p.copy(resyncOnJump = it)) }
        ISlider("Resync Threshold", p.resyncThresholdBeats, 1f, 128f) { upd(p.copy(resyncThresholdBeats = it)) }
        BoolToggle("Overlay Loop", p.overlayLoop) { upd(p.copy(overlayLoop = it)) }
    }
}

@Composable
private fun GranularFreezePanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val g = s.granularFreeze
    val upd = { n: GranularFreezeSettings -> onUpdate(s.copy(granularFreeze = n)) }
    SettingsGroup("GRANULAR FREEZE") {
        FSlider("Freeze Chance", g.freezeChance, 0f, 1f, { upd(g.copy(freezeChance = it)) })
        ISlider("Cooldown Beats", g.cooldownBeats, 0f, 64f) { upd(g.copy(cooldownBeats = it)) }
        EnumToggle3("Repeat Mode", g.repeatMode, "SHORT", "CLASSIC", "LONG") { upd(g.copy(repeatMode = it)) }
        FSlider("Long Repeat Bias", g.repeatLongBias, 0f, 1f, { upd(g.copy(repeatLongBias = it)) })
        FSlider("Min Volume", g.minVolume, 0f, 1f, { upd(g.copy(minVolume = it)) })
        FSlider("Sustain Attack Min", g.sustainAttackMin, 0f, 1f, { upd(g.copy(sustainAttackMin = it)) })
        FSlider("Sustain Seg Dur Min", g.sustainSegDurMin, 0.02f, 1.25f, { upd(g.copy(sustainSegDurMin = it)) })
        FSlider("Percussive Ratio Max", g.percussiveRatioMax, 0.05f, 1f, { upd(g.copy(percussiveRatioMax = it)) })
    }
}

@Composable
private fun ElasticVelocityPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val e = s.elasticVelocity
    val upd = { n: ElasticVelocitySettings -> onUpdate(s.copy(elasticVelocity = n)) }
    SettingsGroup("ELASTIC VELOCITY") {
        FSlider("Min Rate", e.minRate, 0.25f, 2.0f, { upd(e.copy(minRate = it)) }, { "%.2fx".format(it) })
        FSlider("Max Rate", e.maxRate, 0.5f, 3.0f, { upd(e.copy(maxRate = it)) }, { "%.2fx".format(it) })
        FSlider("Curve", e.curve, 0.25f, 4.0f, { upd(e.copy(curve = it)) })
        ISlider("Smoothing Beats", e.smoothingBeats, 0f, 32f) { upd(e.copy(smoothingBeats = it)) }
        FSlider("Max Delta / Beat", e.maxDeltaPerBeat, 0f, 1f, { upd(e.copy(maxDeltaPerBeat = it)) })
    }
}

@Composable
private fun MathRockerPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val m = s.mathRocker
    val upd = { n: MathRockerSettings -> onUpdate(s.copy(mathRocker = n)) }
    SettingsGroup("MATH ROCKER") {
        ISlider("Cycle Beats", m.cycleBeats, 2f, 32f) { upd(m.copy(cycleBeats = it)) }
        ISlider("Drop Beats", m.dropBeats, 1f, 8f) { upd(m.copy(dropBeats = it)) }
        BoolToggle("Reset On Jump", m.resetOnJump) { upd(m.copy(resetOnJump = it)) }
    }
}

@Composable
private fun StalkerPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val st = s.stalker
    val upd = { n: StalkerSettings -> onUpdate(s.copy(stalker = n)) }
    SettingsGroup("THE STALKER") {
        FSlider("Similarity Threshold", st.similarityThreshold, 0.3f, 0.99f, { upd(st.copy(similarityThreshold = it)) })
        ISlider("Cooldown Beats", st.cooldownBeats, 0f, 64f) { upd(st.copy(cooldownBeats = it)) }
        ISlider("Arm Beats", st.armBeats, 0f, 16f) { upd(st.copy(armBeats = it)) }
        BoolToggle("Symmetric Lookup", st.symmetricLookup) { upd(st.copy(symmetricLookup = it)) }
    }
}

@Composable
private fun TimbreSurfingPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val t = s.timbreSurfing
    val upd = { n: TimbreSurfingSettings -> onUpdate(s.copy(timbreSurfing = n)) }
    SettingsGroup("TIMBRE SURFING") {
        ISlider("Top-K", t.topK, 1f, 16f) { upd(t.copy(topK = it)) }
        FSlider("Min Similarity", t.minSimilarity, 0.2f, 0.99f, { upd(t.copy(minSimilarity = it)) })
        ISlider("Min Jump Span", t.minJumpSpanBeats, 0f, 128f) { upd(t.copy(minJumpSpanBeats = it)) }
        ISlider("Exclude Neighbor", t.excludeNeighborBeats, 0f, 16f) { upd(t.copy(excludeNeighborBeats = it)) }
        FSlider("Temperature", t.temperature, 0.03f, 1.25f, { upd(t.copy(temperature = it)) })
        ISlider("Recent Window", t.recentWindowBeats, 0f, 128f) { upd(t.copy(recentWindowBeats = it)) }
        FSlider("Repeat Penalty", t.repeatPenalty, 0f, 1f, { upd(t.copy(repeatPenalty = it)) })
        FSlider("Apply Chance", t.applyChance, 0f, 1f, { upd(t.copy(applyChance = it)) })
        BoolToggle("Override Jumps", t.overrideJumps) { upd(t.copy(overrideJumps = it)) }
    }
}

@Composable
private fun ChromaStackingPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val c = s.chromaStacking
    val upd = { n: ChromaStackingSettings -> onUpdate(s.copy(chromaStacking = n)) }
    SettingsGroup("CHROMA STACKING") {
        FSlider("Overlay Gain", c.overlayGain, 0f, 1f, { upd(c.copy(overlayGain = it)) })
        FSlider("Min Chroma Similarity", c.minChromaSimilarity, 0.3f, 0.99f, { upd(c.copy(minChromaSimilarity = it)) })
        FSlider("Min Timbre Distance", c.minTimbreDistance, 0f, 200f, { upd(c.copy(minTimbreDistance = it)) }, { it.toInt().toString() })
        ISlider("Exclude Neighbor", c.excludeNeighborBeats, 0f, 16f) { upd(c.copy(excludeNeighborBeats = it)) }
        ISlider("Min Jump Span", c.minJumpSpanBeats, 0f, 128f) { upd(c.copy(minJumpSpanBeats = it)) }
        ISlider("Search Top-K", c.searchTopK, 1f, 32f) { upd(c.copy(searchTopK = it)) }
        ISlider("Random Sample", c.randomSample, 0f, 128f) { upd(c.copy(randomSample = it)) }
        FSlider("Temperature", c.temperature, 0.03f, 1.25f, { upd(c.copy(temperature = it)) })
        ISlider("Resample Beats", c.resampleBeats, 1f, 32f) { upd(c.copy(resampleBeats = it)) }
    }
}

@Composable
private fun BeatSortingPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val b = s.beatSorting
    val upd = { n: BeatSortingSettings -> onUpdate(s.copy(beatSorting = n)) }
    SettingsGroup("BEAT SORTING") {
        EnumToggle3("Feature", b.feature, "PITCH", "BRIGHT", "LOUD") { upd(b.copy(feature = it)) }
        EnumToggle2("Direction", b.direction, "ASC", "DESC") { upd(b.copy(direction = it)) }
        FSlider("Min Volume", b.minVolume, 0f, 1f, { upd(b.copy(minVolume = it)) })
        ISlider("Repeat Each", b.repeatEach, 1f, 16f) { upd(b.copy(repeatEach = it)) }
        BoolToggle("Override Jumps", b.overrideJumps) { upd(b.copy(overrideJumps = it)) }
    }
}

@Composable
private fun ReverseBloomPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val r = s.reverseBloom
    val upd = { n: ReverseBloomSettings -> onUpdate(s.copy(reverseBloom = n)) }
    SettingsGroup("REVERSE BLOOM") {
        FSlider("Trigger Threshold", r.triggerThreshold, 0f, 1f, { upd(r.copy(triggerThreshold = it)) })
        ISlider("Rewind Beats", r.rewindBeats, 1f, 128f) { upd(r.copy(rewindBeats = it)) }
        FSlider("Rewind Chance", r.rewindChance, 0f, 1f, { upd(r.copy(rewindChance = it)) })
        ISlider("Cooldown Beats", r.cooldownBeats, 0f, 256f) { upd(r.copy(cooldownBeats = it)) }
        EnumToggle2("Resume Mode", r.resumeMode, "LINEAR", "BLOOM") { upd(r.copy(resumeMode = it)) }
        FSlider("Min Similarity", r.minSimilarity, 0.2f, 0.99f, { upd(r.copy(minSimilarity = it)) })
        ISlider("Bloom Min Span", r.bloomMinSpanBeats, 0f, 512f) { upd(r.copy(bloomMinSpanBeats = it)) }
        ISlider("Bloom Top-K", r.bloomTopK, 1f, 32f) { upd(r.copy(bloomTopK = it)) }
        FSlider("Bloom Temperature", r.bloomTemperature, 0.03f, 1.25f, { upd(r.copy(bloomTemperature = it)) })
        BoolToggle("Override Jumps", r.overrideJumps) { upd(r.copy(overrideJumps = it)) }
    }
}

@Composable
private fun BarberPolePanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val b = s.barberPole
    val upd = { n: BarberPoleSettings -> onUpdate(s.copy(barberPole = n)) }
    SettingsGroup("BARBER POLE") {
        EnumToggle3("Feature", b.feature, "LOUD", "BRIGHT", "PITCH") { upd(b.copy(feature = it)) }
        EnumToggle2("Direction", b.direction, "DOWN", "UP") { upd(b.copy(direction = it)) }
        ISlider("Step Ranks", b.stepRanks, 1f, 64f) { upd(b.copy(stepRanks = it)) }
        FSlider("Min Similarity", b.minSimilarity, 0.2f, 0.99f, { upd(b.copy(minSimilarity = it)) })
        FSlider("Min Volume", b.minVolume, 0f, 1f, { upd(b.copy(minVolume = it)) })
        ISlider("Min Span Beats", b.minSpanBeats, 0f, 512f) { upd(b.copy(minSpanBeats = it)) }
        ISlider("Exclude Neighbor", b.excludeNeighborBeats, 0f, 32f) { upd(b.copy(excludeNeighborBeats = it)) }
        ISlider("Top-K", b.topK, 1f, 32f) { upd(b.copy(topK = it)) }
        FSlider("Temperature", b.temperature, 0.03f, 1.25f, { upd(b.copy(temperature = it)) })
        ISlider("Recent Window", b.recentWindowBeats, 0f, 256f) { upd(b.copy(recentWindowBeats = it)) }
        FSlider("Repeat Penalty", b.repeatPenalty, 0f, 1f, { upd(b.copy(repeatPenalty = it)) })
        FSlider("Apply Chance", b.applyChance, 0f, 1f, { upd(b.copy(applyChance = it)) })
        BoolToggle("Override Jumps", b.overrideJumps) { upd(b.copy(overrideJumps = it)) }
    }
}

@Composable
private fun PalindromePanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val p = s.palindromeEngine
    val upd = { n: PalindromeEngineSettings -> onUpdate(s.copy(palindromeEngine = n)) }
    SettingsGroup("PALINDROME ENGINE") {
        ISlider("Phrase Beats", p.phraseBeats, 2f, 256f) { upd(p.copy(phraseBeats = it)) }
        FSlider("Turn Min Similarity", p.turnMinSimilarity, 0.2f, 0.99f, { upd(p.copy(turnMinSimilarity = it)) })
        ISlider("Turn Top-K", p.turnTopK, 1f, 32f) { upd(p.copy(turnTopK = it)) }
        FSlider("Turn Temperature", p.turnTemperature, 0.03f, 1.25f, { upd(p.copy(turnTemperature = it)) })
        ISlider("Min Turn Span", p.minTurnSpanBeats, 0f, 512f) { upd(p.copy(minTurnSpanBeats = it)) }
        ISlider("Exclude Neighbor", p.excludeNeighborBeats, 0f, 32f) { upd(p.copy(excludeNeighborBeats = it)) }
        ISlider("Flip Cooldown", p.flipCooldownBeats, 0f, 256f) { upd(p.copy(flipCooldownBeats = it)) }
        FSlider("Apply Chance", p.applyChance, 0f, 1f, { upd(p.copy(applyChance = it)) })
        BoolToggle("Override Jumps", p.overrideJumps) { upd(p.copy(overrideJumps = it)) }
    }
}

@Composable
private fun SpectralGravityPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val sg = s.spectralGravity
    val upd = { n: SpectralGravitySettings -> onUpdate(s.copy(spectralGravity = n)) }
    SettingsGroup("SPECTRAL GRAVITY") {
        EnumToggle3("Axis", sg.axis, "BRIGHT", "LOUD", "PITCH") { upd(sg.copy(axis = it)) }
        FSlider("Target", sg.target, 0f, 1f, { upd(sg.copy(target = it)) })
        FSlider("Band Width", sg.bandWidth, 0.01f, 1f, { upd(sg.copy(bandWidth = it)) })
        FSlider("Trigger Threshold", sg.triggerThreshold, 0f, 1f, { upd(sg.copy(triggerThreshold = it)) })
        FSlider("Min Similarity", sg.minSimilarity, 0.2f, 0.99f, { upd(sg.copy(minSimilarity = it)) })
        ISlider("Cooldown Beats", sg.cooldownBeats, 0f, 256f) { upd(sg.copy(cooldownBeats = it)) }
        ISlider("Min Span Beats", sg.minSpanBeats, 0f, 512f) { upd(sg.copy(minSpanBeats = it)) }
        ISlider("Exclude Neighbor", sg.excludeNeighborBeats, 0f, 32f) { upd(sg.copy(excludeNeighborBeats = it)) }
        ISlider("Top-K", sg.topK, 1f, 32f) { upd(sg.copy(topK = it)) }
        FSlider("Temperature", sg.temperature, 0.03f, 1.25f, { upd(sg.copy(temperature = it)) })
        ISlider("Recent Window", sg.recentWindowBeats, 0f, 256f) { upd(sg.copy(recentWindowBeats = it)) }
        FSlider("Repeat Penalty", sg.repeatPenalty, 0f, 1f, { upd(sg.copy(repeatPenalty = it)) })
        FSlider("Apply Chance", sg.applyChance, 0f, 1f, { upd(sg.copy(applyChance = it)) })
        BoolToggle("Override Jumps", sg.overrideJumps) { upd(sg.copy(overrideJumps = it)) }
    }
}

@Composable
private fun CallResponsePanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val c = s.callResponse
    val upd = { n: CallResponseSettings -> onUpdate(s.copy(callResponse = n)) }
    SettingsGroup("CALL & RESPONSE") {
        FSlider("Call Quantile Max", c.callQuantileMax, 0f, 1f, { upd(c.copy(callQuantileMax = it)) })
        FSlider("Response Quantile Min", c.responseQuantileMin, 0f, 1f, { upd(c.copy(responseQuantileMin = it)) })
        ISlider("Bars Per Call", c.barsPerCall, 1f, 16f) { upd(c.copy(barsPerCall = it)) }
        ISlider("Bars Per Response", c.barsPerResponse, 1f, 16f) { upd(c.copy(barsPerResponse = it)) }
        FSlider("Min Similarity", c.minSimilarity, 0.2f, 0.99f, { upd(c.copy(minSimilarity = it)) })
        ISlider("Min Span Beats", c.minSpanBeats, 0f, 512f) { upd(c.copy(minSpanBeats = it)) }
        ISlider("Exclude Neighbor", c.excludeNeighborBeats, 0f, 32f) { upd(c.copy(excludeNeighborBeats = it)) }
        ISlider("Top-K", c.topK, 1f, 32f) { upd(c.copy(topK = it)) }
        FSlider("Temperature", c.temperature, 0.03f, 1.25f, { upd(c.copy(temperature = it)) })
        ISlider("Recent Window", c.recentWindowBars, 0f, 128f) { upd(c.copy(recentWindowBars = it)) }
        FSlider("Repeat Penalty", c.repeatPenalty, 0f, 1f, { upd(c.copy(repeatPenalty = it)) })
        FSlider("Energy Bias", c.energyBias, 0f, 1f, { upd(c.copy(energyBias = it)) })
        FSlider("Same Section Bias", c.sameSectionBias, 0f, 1f, { upd(c.copy(sameSectionBias = it)) })
        FSlider("Apply Chance", c.applyChance, 0f, 1f, { upd(c.copy(applyChance = it)) })
        BoolToggle("Override Jumps", c.overrideJumps) { upd(c.copy(overrideJumps = it)) }
    }
}

@Composable
private fun OrbitWeaverPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val o = s.orbitWeaver
    val upd = { n: OrbitWeaverSettings -> onUpdate(s.copy(orbitWeaver = n)) }
    SettingsGroup("ORBIT WEAVER") {
        ISlider("Anchor Count", o.anchorCount, 2f, 16f) { upd(o.copy(anchorCount = it)) }
        EnumToggle3("Spin Axis", o.spinAxis, "ENERGY", "BRIGHT", "PITCH") { upd(o.copy(spinAxis = it)) }
        ISlider("Bars Per Anchor", o.barsPerAnchor, 1f, 16f) { upd(o.copy(barsPerAnchor = it)) }
        BoolToggle("Jump At Bar Start", o.jumpAtBarStart) { upd(o.copy(jumpAtBarStart = it)) }
        FSlider("Anchor Pull", o.anchorPull, 0f, 1f, { upd(o.copy(anchorPull = it)) })
        FSlider("Min Similarity", o.minSimilarity, 0.2f, 0.99f, { upd(o.copy(minSimilarity = it)) })
        ISlider("Min Span Beats", o.minSpanBeats, 0f, 512f) { upd(o.copy(minSpanBeats = it)) }
        ISlider("Exclude Neighbor", o.excludeNeighborBeats, 0f, 32f) { upd(o.copy(excludeNeighborBeats = it)) }
        ISlider("Top-K", o.topK, 1f, 32f) { upd(o.copy(topK = it)) }
        FSlider("Temperature", o.temperature, 0.03f, 1.25f, { upd(o.copy(temperature = it)) })
        ISlider("Recent Window", o.recentWindowBeats, 0f, 512f) { upd(o.copy(recentWindowBeats = it)) }
        FSlider("Repeat Penalty", o.repeatPenalty, 0f, 1f, { upd(o.copy(repeatPenalty = it)) })
        FSlider("Same Section Bias", o.sameSectionBias, 0f, 1f, { upd(o.copy(sameSectionBias = it)) })
        FSlider("Apply Chance", o.applyChance, 0f, 1f, { upd(o.copy(applyChance = it)) })
        BoolToggle("Override Jumps", o.overrideJumps) { upd(o.copy(overrideJumps = it)) }
    }
}

@Composable
private fun SculptorConfigPanel(s: AdvancedSettings, onUpdate: (AdvancedSettings) -> Unit) {
    val sc = s.sculptorConfig
    val upd = { n: SculptorConfigSettings -> onUpdate(s.copy(sculptorConfig = n)) }
    SettingsGroup("SECTION SCULPTOR") {
        FSlider("Duration Scale", sc.durationScale, 0.4f, 2.5f, { upd(sc.copy(durationScale = it)) }, { "%.2fx".format(it) })
        ISlider("Min Section Secs", sc.minSectionSeconds, 2f, 60f) { upd(sc.copy(minSectionSeconds = it)) }
        ISlider("Max Section Secs", sc.maxSectionSeconds, 4f, 120f) { upd(sc.copy(maxSectionSeconds = it)) }
        ISlider("Preview Secs", sc.previewSeconds, 1f, 12f) { upd(sc.copy(previewSeconds = it)) }
        FSlider("Transition Overlap", sc.transitionOverlapSeconds, 0f, 8f, { upd(sc.copy(transitionOverlapSeconds = it)) })
    }
}
