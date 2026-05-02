package com.nsu.transcriptanalyzer.data.api

import com.nsu.transcriptanalyzer.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Retrofit service – endpoints match backend/app.py exactly.
 *
 * Auth token is injected automatically by [AuthTokenInterceptor].
 * All paths are RELATIVE (no leading slash) so Retrofit resolves them
 * against the base URL: https://android-transcript-analyzer.onrender.com/
 *
 * ── KEY FINDINGS FROM app.py ───────────────────────────────────────────────
 *  /api/mobile/auth/google  → JSON body key "id_token"       (line 1625)
 *  /api/mobile/analyze      → JSON body (manual/csv only)    (line 1750-1770)
 *  /api/mobile/ocr/extract  → multipart form, file key "file"(line 1835)
 *  /api/mobile/history      → runs[].id (not run_id)         (line 1354)
 * ────────────────────────────────────────────────────────────────────────────
 */
interface TranscriptApiService {

    // ─── Health ───────────────────────────────────────────────────────────────

    @GET("api/health")
    suspend fun healthCheck(): Map<String, String>

    // ─── Authentication ───────────────────────────────────────────────────────

    /**
     * Exchange a Google ID-token for a backend session token.
     * Body: { "id_token": "<google_id_token>" }
     * Backend reads: payload.get("id_token")  → line 1625
     */
    @POST("api/mobile/auth/google")
    suspend fun authenticateWithGoogle(
        @Body request: Map<String, String>
    ): AuthResponse

    /**
     * Email/name login (NSU @northsouth.edu accounts only).
     * Body: { "email": "...", "name": "..." }
     */
    @POST("api/mobile/auth/email")
    suspend fun authenticateWithEmail(
        @Body request: Map<String, String>
    ): AuthResponse

    /**
     * Verify stored token & fetch current user profile.
     * Bearer token injected by [AuthTokenInterceptor].
     */
    @GET("api/mobile/auth/me")
    suspend fun getCurrentUser(): MeResponse

    // ─── Analysis ─────────────────────────────────────────────────────────────

    /**
     * Analyze transcript — JSON body.
     *
     * IMPORTANT: The mobile analyze endpoint ONLY supports:
     *   input_method = "manual"  →  manual_text field
     *   input_method = "csv"     →  csv_text field  (raw CSV string)
     * (line 1765-1770 in app.py)
     *
     * For PDF/image upload, use [ocrExtract] first, then submit the
     * extracted manual_text here with input_method="manual".
     */
    @POST("api/mobile/analyze")
    suspend fun analyzeTranscript(
        @Body request: AnalyzeRequest
    ): AnalyzeResponse

    // ─── OCR Extract (PDF / Image → text) ────────────────────────────────────

    /**
     * Upload a PDF or image file; backend returns extracted course rows
     * as manual_text that can be submitted to [analyzeTranscript].
     *
     * Multipart fields:
     *   "input_method"  →  "pdf" or "image"
     *   "file"          →  the file bytes  (line 1835: .get("file_key") or .get("file"))
     */
    @Multipart
    @POST("api/mobile/ocr/extract")
    suspend fun ocrExtract(
        @Part("input_method") inputMethod: RequestBody,
        @Part file: MultipartBody.Part
    ): OcrExtractResponse

    // ─── History ──────────────────────────────────────────────────────────────

    /** Returns runs[].id (not run_id) – see run_to_summary line 1354. */
    @GET("api/mobile/history")
    suspend fun getHistory(): HistoryResponse

    /** run_id path param maps to TranscriptRun.id */
    @GET("api/mobile/history/{run_id}")
    suspend fun getHistoryDetails(
        @Path("run_id") runId: Int
    ): HistoryDetailsResponse

    // ─── AI Chat ──────────────────────────────────────────────────────────────

    @POST("api/mobile/ai/chat")
    suspend fun aiChat(
        @Body request: AiChatRequest
    ): AiChatResponse
}
