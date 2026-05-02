package com.nsu.transcriptanalyzer.data.api

import com.nsu.transcriptanalyzer.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Retrofit service for the NSU Transcript Analyzer backend.
 *
 * The `Authorization: Bearer <token>` header is injected automatically
 * by [AuthTokenInterceptor] – no manual @Header parameter needed here.
 *
 * File uploads use @Multipart so that CSV, PDF, and Image files are sent
 * as multipart/form-data parts (proper binary upload, not JSON).
 */
interface TranscriptApiService {

    // ─── Authentication ───────────────────────────────────────────────────────

    /**
     * Exchange a Google ID-token (from Credential Manager) for a backend JWT.
     */
    @POST("api/mobile/auth/google")
    suspend fun authenticateWithGoogle(
        @Body request: Map<String, String>
    ): AuthResponse

    /**
     * Simple email-based auth (for testing / non-Google fallback).
     */
    @POST("api/mobile/auth/email")
    suspend fun authenticateWithEmail(
        @Body request: Map<String, String>
    ): AuthResponse

    /**
     * Fetch the currently authenticated user's profile.
     * The Bearer token is attached automatically.
     */
    @GET("api/mobile/auth/me")
    suspend fun getCurrentUser(): AuthResponse

    // ─── Analysis ─────────────────────────────────────────────────────────────

    /**
     * Upload a transcript file (CSV, PDF, or Image) for analysis.
     *
     * @param file        The physical file bytes as a multipart part.
     *                    Part name MUST be "file" to match the Flask endpoint.
     *                    Null for manual input mode.
     * @param program     "CSE" or "BBA"
     * @param inputMethod "csv" | "pdf" | "image" | "manual"
     * @param manualText  CSV-formatted course list for "manual" mode (optional).
     */
    @Multipart
    @POST("api/mobile/analyze")
    suspend fun analyzeTranscript(
        @Part file: MultipartBody.Part?,
        @Part("program")      program:     RequestBody,
        @Part("input_method") inputMethod: RequestBody,
        @Part("manual_text")  manualText:  RequestBody? = null
    ): AnalyzeResponse

    // ─── History ──────────────────────────────────────────────────────────────

    @GET("api/mobile/history")
    suspend fun getHistory(): HistoryResponse

    @GET("api/mobile/history/{run_id}")
    suspend fun getHistoryDetails(
        @Path("run_id") runId: Int
    ): HistoryDetailsResponse

    // ─── Health ───────────────────────────────────────────────────────────────

    @GET("api/health")
    suspend fun healthCheck(): Map<String, String>
}
