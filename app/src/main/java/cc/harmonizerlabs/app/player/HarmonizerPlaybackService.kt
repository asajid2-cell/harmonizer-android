package cc.harmonizerlabs.app.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
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
    val beatsPlayed: Int = 0,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val mode: String = "canon",
    val voiceCount: Int = 2,
    val loopEnabled: Boolean = true,
    val noBurnout: Boolean = true,
    val trackTitle: String = "",
    val trackArtist: String = "",
    val phaseIntensity: Float = 1.0f,
    val baseAudioOnly: Boolean = false,
    val errorMessage: String? = null,
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
    // Source-error retries — large lossless FLACs streamed over HTTPS occasionally drop a
    // chunk (esp. canon mode streaming two files at once); a re-prepare usually recovers.
    private var sourceRetries = 0

    // Robust HTTP source — generous timeouts + redirects so big FLAC streams don't error out.
    private fun buildPlayer(volume: Float, repeat: Int): ExoPlayer {
        val http = DefaultHttpDataSource.Factory()
            .setUserAgent("HarmonizerAndroid/1.0 (ExoPlayer)")
            .setConnectTimeoutMs(30_000)
            .setReadTimeoutMs(30_000)
            .setAllowCrossProtocolRedirects(true)
        val sourceFactory = DefaultMediaSourceFactory(DefaultDataSource.Factory(this, http))
        return ExoPlayer.Builder(this)
            .setMediaSourceFactory(sourceFactory)
            .build()
            .also { it.volume = volume; it.repeatMode = repeat }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        mainPlayer = buildPlayer(MAIN_VOLUME, Player.REPEAT_MODE_OFF).also { p ->
            p.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.update { it.copy(isPlaying = isPlaying) }
                    if (isPlaying) {
                        // On API 31+ startForeground() throws ForegroundServiceStartNotAllowedException
                        // if the app isn't in a foreground-permitted state (e.g. playback auto-starts
                        // mid screen-transition, or after a source-error retry). Never let that crash
                        // playback — show the notification when allowed, otherwise keep playing without it.
                        try {
                            startForeground(NOTIFICATION_ID, buildNotification())
                        } catch (_: Exception) { /* foreground promotion denied; audio still plays */ }
                        startBeatLoop()
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(false)
                        beatJob?.cancel()
                    }
                }
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        // ExoPlayer's decoded duration is authoritative — fixes any mismatch
                        // with the analysis JSON's audio_summary.duration.
                        val d = mainPlayer.duration
                        if (d > 0) _state.update { it.copy(durationMs = d, errorMessage = null) }
                        sourceRetries = 0
                    }
                    if (state == Player.STATE_ENDED && _state.value.loopEnabled) {
                        seekToStart()
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    // Transient source/IO errors: re-prepare a couple times before giving up,
                    // rather than silently dying on a dead play button.
                    if (sourceRetries < MAX_SOURCE_RETRIES) {
                        sourceRetries++
                        serviceScope.launch(Dispatchers.Main) {
                            mainPlayer.prepare()
                            if (engine.requiresOverlayPlayer) overlayPlayer.prepare()
                            mainPlayer.play()
                            if (engine.requiresOverlayPlayer) overlayPlayer.play()
                        }
                    } else {
                        _state.update { it.copy(errorMessage = "Couldn't load audio — tap play to retry") }
                    }
                }
            })
        }

        overlayPlayer = buildPlayer(OVERLAY_VOLUME, Player.REPEAT_MODE_ALL)

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
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
        sourceRetries = 0

        // Web parity: prefer track.audio_url, fall back to info.url.
        val audioUrl = resolveAudioUrl(track.audioUrl ?: track.info.url)
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
                beatsPlayed = 0,
            )
        }
    }

    fun play() {
        // Manual play also clears a prior error and re-arms retries (the "tap to retry" path).
        if (_state.value.errorMessage != null || mainPlayer.playbackState == Player.STATE_IDLE) {
            sourceRetries = 0
            _state.update { it.copy(errorMessage = null) }
            mainPlayer.prepare()
            if (engine.requiresOverlayPlayer) overlayPlayer.prepare()
        }
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

    /** Section Sculptor — set the section play order and seek to its start. */
    fun setSculptorArrangement(order: List<Int>) {
        val eng = engine as? SculptorEngine ?: return
        val track = trackData ?: return
        eng.arrangement = order
        val firstBeat = eng.restart(track.analysis)
        seekToBeat(firstBeat)
        // Re-sync the beat loop so its local index matches the seek (the loop only catches up
        // forward on its own, so a backward seek from an edit would otherwise desync).
        if (mainPlayer.isPlaying) startBeatLoop()
    }

    fun setPhaseIntensity(v: Float) {
        _state.update { it.copy(phaseIntensity = v) }
        // Phase intensity changes the overlay volume during Phase Shifter mode.
        // A value of 0 = dry (overlay silent), 4 = fully wet (max phase effect).
        if (_state.value.mode == "phaseshifter") {
            overlayPlayer.volume = (v / 4f).coerceIn(0f, 1f) * OVERLAY_VOLUME
        }
    }

    fun setBaseAudioOnly(v: Boolean) {
        _state.update { it.copy(baseAudioOnly = v) }
        overlayPlayer.volume = if (v) 0f else OVERLAY_VOLUME
    }

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

                    // Count every beat dwelt on — cumulative, so jukebox/eternal climb past
                    // the track length as the engine jumps and loops (web's "beats played").
                    _state.update { it.copy(currentBeatIndex = currentIdx, beatsPlayed = it.beatsPlayed + 1) }

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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Harmonizer Playback", NotificationManager.IMPORTANCE_LOW)
            ch.description = "Music playback controls"
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val title  = _state.value.trackTitle.ifBlank { "Harmonizer" }
        val artist = _state.value.trackArtist
        val intent = Intent(this, MainActivity::class.java)
        val pi     = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID         = "harmonizer_playback"
        private const val NOTIFICATION_ID    = 1001
        private const val MAIN_VOLUME        = 0.82f
        private const val OVERLAY_VOLUME     = 0.48f
        private const val MAX_SOURCE_RETRIES = 3
    }
}
