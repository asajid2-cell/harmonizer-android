package cc.harmonizerlabs.app.model

enum class HarmonizerMode(
    val key: String,
    val displayName: String,
    val icon: String,
    val description: String,
    val isExperimental: Boolean = false,
) {
    CANON(
        key         = "canon",
        displayName = "CANON",
        icon        = "♾",
        description = "Mirror voices with delays",
    ),
    JUKEBOX(
        key         = "jukebox",
        displayName = "JUKEBOX",
        icon        = "⟳",
        description = "Jump between similar beats",
    ),
    ETERNAL(
        key         = "eternal",
        displayName = "ETERNAL",
        icon        = "∞",
        description = "Canon + jukebox combined",
    ),
    PHASE_SHIFTER(
        key         = "phaseshifter",
        displayName = "PHASE",
        icon        = "〜",
        description = "Synced phaser sweep",
    ),
    ELASTIC_VELOCITY(
        key         = "elasticvelocity",
        displayName = "ELASTIC",
        icon        = "⚡",
        description = "Beat energy to speed",
        isExperimental = true,
    ),
    REVERSE_BLOOM(
        key         = "reversebloom",
        displayName = "REVERSE",
        icon        = "↩",
        description = "Rewind on drops",
        isExperimental = true,
    ),
    DOPAMINE_MINER(
        key         = "dopamineminer",
        displayName = "DOPAMINE",
        icon        = "⬆",
        description = "High-energy peak clusters",
        isExperimental = true,
    ),
    GRANULAR_FREEZE(
        key         = "granularfreeze",
        displayName = "GRANULAR",
        icon        = "❄",
        description = "Stutter sustained beats",
        isExperimental = true,
    ),
    THE_STALKER(
        key         = "stalker",
        displayName = "STALKER",
        icon        = "◎",
        description = "Return to target beat",
        isExperimental = true,
    ),
    TIMBRE_SURFING(
        key         = "timbresurfing",
        displayName = "TIMBRE",
        icon        = "🌊",
        description = "Jump by timbre similarity",
        isExperimental = true,
    ),
}

/** Modes available in the main selector grid (non-experimental first) */
val PrimaryModes = listOf(
    HarmonizerMode.CANON,
    HarmonizerMode.JUKEBOX,
    HarmonizerMode.ETERNAL,
    HarmonizerMode.PHASE_SHIFTER,
)

val ExperimentalModes = HarmonizerMode.entries.filter { it.isExperimental }
