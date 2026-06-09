package cc.harmonizerlabs.app.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import cc.harmonizerlabs.app.BuildConfig
import cc.harmonizerlabs.app.MainActivity
import cc.harmonizerlabs.app.api.models.TrackAnalysis
import cc.harmonizerlabs.app.api.models.TrackData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentBeatIndex: Int = 0,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val mode: String = "canon",
    val voiceCount: Int = 2,
    val loopEnabled: Boolean = true,
    val noBurnout: Boolean = true,
    val trackTitle: String = "",
    val trackArtist: String = "",
)

class HarmonizerPlaybackService : MediaSessionService() {

    // ── Binder for in-process clients ─────────────────────────────────────────
    inner class LocalBinder : Binder() {
        fun getService(): HarmonizerPlaybackService = this@HarmonizerPlaybackService
    }
    private val binder = LocalBinder()

    // ── Players ───────────────────────────────────────────────────────────────
    private lateinit var mainPlayer: ExoPlayer
    private lateinit var overlayPlayer: ExoPlayer
    private var mediaSession: MediaSession? = null

    // ── State ─────────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var trackData: TrackData? = null
    private var engine: PlaybackEngine = CanonEngine()
    // Default dispatcher for beat timing — more accurate than Main under UI load
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var beatJob: Job? = null

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    override fun onCreate() {
        super.onCreate()

        mainPlayer = ExoPlayer.Builder(this).build().also { p ->
            p.volume = MAIN_VOLUME
            p.repeatMode = Player.REPEAT_MODE_OFF
            p.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.update { it.copy(isPlaying = isPlaying) }
                    if (isPlaying) startBeatLoop() else beatJob?.cancel()
                }
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED && _state.value.loopEnabled) {
                        seekToStart()
                    }
                }
            })
        }

        overlayPlayer = ExoPlayer.Builder(this).build().also { p ->
            p.volume = OVERLAY_VOLUME
            p.repeatMode = Player.REPEAT_MODE_ALL
        }

        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        mediaSession = MediaSession.Builder(this, mainPlayer)
            .setSessionActivity(pi)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    // Route Media3 session-controller binds to super; local ViewModel binds get our binder.
    // If both use the same binder, Media3 can't manage the foreground notification or
    // lock-screen controls — they'd receive an unexpected IBinder type and silently fail.
    override fun onBind(intent: Intent?): IBinder? {
        return if (intent?.action == SERVICE_INTERFACE) super.onBind(intent) else binder
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run { release(); mediaSession = null }
        mainPlayer.release()
        overlayPlayer.release()
        super.onDestroy()
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun loadTrack(track: TrackData, modeKey: String) {
        trackData = track
        engine = engineFor(modeKey)

        val audioUrl = resolveAudioUrl(track.info.url)
        val mainItem = MediaItem.Builder()
            .setUri(audioUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title ?: "Unknown Track")
                    .setArtist(track.artist ?: "")
                    .build()
            )
            .build()

        mainPlayer.setMediaItem(mainItem)
        mainPlayer.prepare()

        // Overlay — only for modes that need two voices
        if (engine.requiresOverlayPlayer) {
            val offsetBeats = engine.overlayOffsetBeats(track.analysis, buildContext())
            val offsetMs    = beatsToMs(offsetBeats, track.analysis)

            overlayPlayer.setMediaItem(mainItem)
            overlayPlayer.prepare()
            // Seek overlay to the offset position so it plays ahead by `offsetMs`
            overlayPlayer.seekTo(offsetMs)
        }

        _state.update {
            it.copy(
                mode        = modeKey,
                trackTitle  = track.title ?: "",
                trackArtist = track.artist ?: "",
                durationMs  = (track.audioSummary.duration * 1000).toLong(),
                currentBeatIndex = 0,
            )
        }
    }

    fun play() {
        mainPlayer.play()
        if (engine.requiresOverlayPlayer) overlayPlayer.play()
    }

    fun pause() {
        mainPlayer.pause()
        if (engine.requiresOverlayPlayer) overlayPlayer.pause()
        beatJob?.cancel()
    }

    fun togglePlayPause() { if (mainPlayer.isPlaying) pause() else play() }

    fun seekToBeat(beatIndex: Int) {
        val track = trackData ?: return
        val beat  = track.analysis.beats.getOrNull(beatIndex) ?: return
        val ms    = (beat.start * 1000).toLong()
        mainPlayer.seekTo(ms)
        _state.update { it.copy(currentBeatIndex = beatIndex, currentPositionMs = ms) }
    }

    fun setVoiceCount(n: Int) = _state.update { it.copy(voiceCount = n) }
    fun setLoop(v: Boolean)   = _state.update { it.copy(loopEnabled = v) }
    fun setNoBurnout(v: Boolean) = _state.update { it.copy(noBurnout = v) }

    fun loadRenderedAudio(url: String) {
        // Switch to a rendered file (background render result) — stop beat engine
        beatJob?.cancel()
        val audioUrl = resolveAudioUrl(url)
        val td = trackData
        val item = if (td != null) {
            MediaItem.Builder()
                .setUri(audioUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(td.title ?: "Unknown Track")
                        .setArtist(td.artist ?: "")
                        .build()
                )
                .build()
        } else {
            MediaItem.fromUri(audioUrl)
        }
        mainPlayer.setMediaItem(item)
        mainPlayer.prepare()
        overlayPlayer.stop()
    }

    // ── Beat loop ─────────────────────────────────────────────────────────────

    private fun startBeatLoop() {
        beatJob?.cancel()
        val track  = trackData ?: return
        val beats  = track.analysis.beats
        if (beats.isEmpty()) return

        beatJob = serviceScope.launch {
            var currentIdx = _state.value.currentBeatIndex

            while (isActive) {
                // ExoPlayer must be queried on the main thread
                val (posMs, isPlaying) = withContext(Dispatchers.Main) {
                    mainPlayer.currentPosition to mainPlayer.isPlaying
                }
                if (!isPlaying) break

                _state.update { it.copy(currentPositionMs = posMs) }

                // Advance beat index to match current position
                val posS = posMs / 1000.0
                while (currentIdx + 1 < beats.size && beats[currentIdx + 1].start <= posS) {
                    currentIdx++
                }

                val currentBeat = beats.getOrNull(currentIdx)
                if (currentBeat != null) {
                    val beatEndMs = ((currentBeat.start + currentBeat.duration) * 1000).toLong()
                    val remainMs  = beatEndMs - posMs

                    _state.update { it.copy(currentBeatIndex = currentIdx) }

                    if (remainMs > 20) {
                        delay(remainMs - 10) // wake up just before the beat ends
                    }

                    // Only jump for jukebox/eternal modes; canon plays linearly
                    val stillPlaying = withContext(Dispatchers.Main) { mainPlayer.isPlaying }
                    if (stillPlaying && engine !is CanonEngine && engine !is PhaseShifterEngine) {
                        val nextIdx = engine.nextBeatIndex(currentIdx, track.analysis, buildContext())
                        if (nextIdx != currentIdx + 1) {
                            withContext(Dispatchers.Main) { seekToBeat(nextIdx) }
                            currentIdx = nextIdx
                        } else {
                            currentIdx++
                        }
                    } else {
                        currentIdx++
                    }
                } else {
                    delay(50)
                }
            }
        }
    }

    private fun seekToStart() {
        mainPlayer.seekTo(0)
        if (engine.requiresOverlayPlayer) {
            val track = trackData
            if (track != null) {
                val offsetMs = beatsToMs(
                    engine.overlayOffsetBeats(track.analysis, buildContext()), track.analysis
                )
                overlayPlayer.seekTo(offsetMs)
            }
        }
        mainPlayer.play()
        if (engine.requiresOverlayPlayer) overlayPlayer.play()
    }

    private fun buildContext() = PlaybackContext(
        track        = trackData!!,
        voiceCount   = _state.value.voiceCount,
        loopEnabled  = _state.value.loopEnabled,
        noBurnout    = _state.value.noBurnout,
    )

    private fun beatsToMs(beatCount: Int, analysis: TrackAnalysis): Long {
        val beat = analysis.beats.getOrNull(beatCount) ?: return 0L
        return (beat.start * 1000).toLong()
    }

    private fun resolveAudioUrl(url: String): String {
        if (url.startsWith("http")) return url
        return "${BuildConfig.BASE_URL.trimEnd('/')}$url"
    }

    companion object {
        private const val MAIN_VOLUME    = 0.82f
        private const val OVERLAY_VOLUME = 0.48f
    }
}
