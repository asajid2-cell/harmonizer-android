package cc.harmonizerlabs.app.model

enum class HarmonizerMode(
    val key: String,
    val displayName: String,
    val icon: String,
    val description: String,
    val isExperimental: Boolean = false,
) {
    // ── Primary modes (match web app keys exactly) ────────────────────────────
    CANON(
        key         = "canon",
        displayName = "CANON",
        icon        = "∞",
        description = "Creates a canon by layering delayed copies of the track.",
    ),
    JUKEBOX(
        key         = "jukebox",
        displayName = "JUKEBOX",
        icon        = "○",
        description = "Jumps between similar beats for endless looping.",
    ),
    ETERNAL(
        key         = "eternal",
        displayName = "ETERNAL",
        icon        = "↺",
        description = "Combines canon overlay with jukebox looping.",
    ),
    PHASE_SHIFTER(
        key         = "phaseshifter",
        displayName = "PHASE SHIFTER",
        icon        = "≈",
        description = "Applies a synced phaser sweep to the audio.",
    ),
    ELASTIC_VELOCITY(
        key         = "elasticvelo",
        displayName = "ELASTIC VELOCITY",
        icon        = "⇆",
        description = "Maps beat energy to playback speed.",
    ),
    REVERSE_BLOOM(
        key         = "reversebloom",
        displayName = "REVERSE BLOOM",
        icon        = "↶",
        description = "Rewinds on drops then jumps to similar peaks.",
    ),
    AUTOHARMONIZER(
        key         = "autoharmonizer",
        displayName = "AUTOHARMONIZER",
        icon        = "↔",
        description = "Links two tracks and jumps between matches.",
    ),
    SECTION_SCULPTOR(
        key         = "sculptor",
        displayName = "SECTION SCULPTOR",
        icon        = "✂",
        description = "Arrange detected sections into a custom timeline.",
    ),

    // ── Experimental modes ────────────────────────────────────────────────────
    AUTOCROONER(
        key            = "autocrooner",
        displayName    = "AUTOCROONER",
        icon           = "♪",
        description    = "Slow, warm, vintage croon with wow/flutter.",
        isExperimental = true,
    ),
    DOPAMINE_MINER(
        key            = "dopamine",
        displayName    = "DOPAMINE MINER",
        icon           = "▲",
        description    = "Loops high-energy peak clusters.",
        isExperimental = true,
    ),
    HARMONIC_TRAP(
        key            = "harmonictrap",
        displayName    = "HARMONIC TRAP",
        icon           = "♫",
        description    = "Stays in one key by filtering beats.",
        isExperimental = true,
    ),
    GRANULAR_FREEZE(
        key            = "granularfreeze",
        displayName    = "GRANULAR FREEZE",
        icon           = "❄",
        description    = "Stutters sustained beats into repeats.",
        isExperimental = true,
    ),
    MATH_ROCKER(
        key            = "mathrocker",
        displayName    = "MATH ROCKER",
        icon           = "÷",
        description    = "Drops beats on a cycle for odd meter.",
        isExperimental = true,
    ),
    THE_STALKER(
        key            = "stalker",
        displayName    = "THE STALKER",
        icon           = "●",
        description    = "Returns to a chosen target when similar.",
        isExperimental = true,
    ),
    TIMBRE_SURFING(
        key            = "timbresurf",
        displayName    = "TIMBRE SURFING",
        icon           = "♯",
        description    = "Jumps by timbre similarity instead of time.",
        isExperimental = true,
    ),
    CHROMA_STACKING(
        key            = "chromastack",
        displayName    = "CHROMA STACKING",
        icon           = "♬",
        description    = "Stacks harmonically similar overlays.",
        isExperimental = true,
    ),
    BEAT_SORTING(
        key            = "beatsort",
        displayName    = "BEAT SORTING",
        icon           = "⇅",
        description    = "Reorders beats by a chosen feature.",
        isExperimental = true,
    ),
    BARBER_POLE(
        key            = "barberpole",
        displayName    = "BARBER POLE",
        icon           = "⇈",
        description    = "Creates a continuous rise or fall.",
        isExperimental = true,
    ),
    PALINDROME(
        key            = "palindrome",
        displayName    = "PALINDROME",
        icon           = "↩",
        description    = "Plays forward then reverse with pivots.",
        isExperimental = true,
    ),
    SPECTRAL_GRAVITY(
        key            = "spectralgravity",
        displayName    = "SPECTRAL GRAVITY",
        icon           = "◎",
        description    = "Pulls toward a target spectral band.",
        isExperimental = true,
    ),
    CALL_AND_RESPONSE(
        key            = "callresponse",
        displayName    = "CALL & RESPONSE",
        icon           = "⇄",
        description    = "Alternates low and high energy bars.",
        isExperimental = true,
    ),
    ORBIT_WEAVER(
        key            = "orbitweaver",
        displayName    = "ORBIT WEAVER",
        icon           = "◉",
        description    = "Weaves among anchor bars with similarity jumps.",
        isExperimental = true,
    ),
}

/** All non-experimental modes shown in the primary grid */
val PrimaryModes = HarmonizerMode.entries.filter { !it.isExperimental }

/** All experimental modes */
val ExperimentalModes = HarmonizerMode.entries.filter { it.isExperimental }
