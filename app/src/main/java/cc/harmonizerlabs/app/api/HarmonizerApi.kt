package cc.harmonizerlabs.app.api

import cc.harmonizerlabs.app.api.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface HarmonizerApi {

    // ── Track processing ─────────────────────────────────────────────────────

    @Multipart
    @POST("api/process")
    suspend fun processTrackUpload(
        @Part audio: MultipartBody.Part,
        @Part("source") source: RequestBody,
        @Part("algorithm") algorithm: RequestBody,
        @Part("title") title: RequestBody,
        @Part("artist") artist: RequestBody,
    ): Response<ProcessResponse>

    @FormUrlEncoded
    @POST("api/process")
    suspend fun processTrackUrl(
        @Field("source") source: String,
        @Field("algorithm") algorithm: String,
        @Field("youtube_url") youtubeUrl: String? = null,
        @Field("spotify_url") spotifyUrl: String? = null,
        @Field("title") title: String = "",
        @Field("artist") artist: String = "",
    ): Response<ProcessResponse>

    @GET("api/process/status/{jobId}")
    suspend fun getProcessStatus(
        @Path("jobId") jobId: String,
    ): Response<JobStatusResponse>

    // ── Track analysis data ───────────────────────────────────────────────────

    @GET("data/{trackId}.json")
    suspend fun getTrackAnalysis(
        @Path("trackId") trackId: String,
    ): Response<TrackAnalysisResponse>

    // ── Background render ─────────────────────────────────────────────────────

    @POST("api/background-render")
    suspend fun startBackgroundRender(
        @Body request: BackgroundRenderRequest,
    ): Response<BackgroundRenderResponse>

    @GET("api/background-render/status/{jobId}")
    suspend fun getBackgroundRenderStatus(
        @Path("jobId") jobId: String,
    ): Response<RenderStatusResponse>

    // ── Song library ─────────────────────────────────────────────────────────

    @GET("api/cache/list")
    suspend fun getCachedSongs(): Response<CachedSongsResponse>

    // ── Playlist info ─────────────────────────────────────────────────────────

    @POST("api/playlist-info")
    suspend fun getPlaylistInfo(
        @Body request: PlaylistInfoRequest,
    ): Response<PlaylistInfoResponse>
}
