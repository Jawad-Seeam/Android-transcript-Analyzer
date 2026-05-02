package com.nsu.transcriptanalyzer.data.model

import com.google.gson.annotations.SerializedName

// ─── Authentication ───────────────────────────────────────────────────────────

/**
 * Response from /api/mobile/auth/google and /api/mobile/auth/email.
 * { "ok": true, "access_token": "...", "token_type": "Bearer",
 *   "expires_in": 2592000, "user": { ... } }
 */
data class AuthResponse(
    @SerializedName("ok")           val ok: Boolean,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type")   val tokenType: String? = null,
    @SerializedName("expires_in")   val expiresIn: Int? = null,
    @SerializedName("user")         val user: User? = null,
    @SerializedName("error")        val error: String? = null
)

/**
 * Response from GET /api/mobile/auth/me.
 * { "ok": true, "user": { ... } }
 */
data class MeResponse(
    @SerializedName("ok")    val ok: Boolean,
    @SerializedName("user")  val user: User? = null,
    @SerializedName("error") val error: String? = null
)

data class User(
    @SerializedName("id")         val id: Int,
    @SerializedName("email")      val email: String,
    @SerializedName("name")       val name: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

// ─── Analysis request (JSON body) ─────────────────────────────────────────────

/**
 * Request body for POST /api/mobile/analyze.
 *
 * Backend reads (app.py lines 1751-1768):
 *   payload.get("input_method")   →  "manual" | "csv"
 *   payload.get("program")        →  "CSE" | "BBA"
 *   payload.get("manual_text")    →  required for manual mode
 *   payload.get("csv_text")       →  required for csv mode
 *   payload.get("waived")         →  list of course codes (optional)
 */
data class AnalyzeRequest(
    @SerializedName("input_method") val inputMethod: String,
    @SerializedName("program")      val program: String,
    @SerializedName("manual_text")  val manualText: String? = null,
    @SerializedName("csv_text")     val csvText: String? = null,
    @SerializedName("waived")       val waived: List<String> = emptyList()
)

/**
 * Response from POST /api/mobile/analyze.
 * { "ok": true, "run_id": 42, "result": { ... } }
 */
data class AnalyzeResponse(
    @SerializedName("ok")     val ok: Boolean,
    @SerializedName("run_id") val runId: Int? = null,
    @SerializedName("result") val result: AnalysisResult? = null,
    @SerializedName("error")  val error: String? = null
)

data class AnalysisResult(
    @SerializedName("input_method")     val inputMethod: String,
    @SerializedName("program")          val program: String,
    @SerializedName("cgpa")             val cgpa: Double,
    @SerializedName("earned_credits")   val earnedCredits: Int,
    @SerializedName("required_credits") val requiredCredits: Int,
    @SerializedName("remaining_credits")val remainingCredits: Int,
    @SerializedName("eligible")         val eligible: Boolean,
    @SerializedName("issues")           val issues: List<String>,
    @SerializedName("waived")           val waived: List<String>,
    @SerializedName("latest_rows")      val latestRows: List<Map<String, Any?>> = emptyList(),
    @SerializedName("course_audit")     val courseAudit: List<CourseAudit> = emptyList(),
    @SerializedName("retake_summary")   val retakeSummary: List<Map<String, Any?>> = emptyList()
)

data class CourseAudit(
    @SerializedName("category") val category: String,
    @SerializedName("course")   val course: String,
    @SerializedName("status")   val status: String,
    @SerializedName("details")  val details: String
)

// ─── OCR Extract ─────────────────────────────────────────────────────────────

/**
 * Response from POST /api/mobile/ocr/extract (multipart).
 * Returns extracted course rows as manual_text ready for /api/mobile/analyze.
 */
data class OcrExtractResponse(
    @SerializedName("ok")            val ok: Boolean,
    @SerializedName("manual_text")   val manualText: String? = null,
    @SerializedName("confidence")    val confidence: String? = null,
    @SerializedName("score")         val score: Int? = null,
    @SerializedName("detected_rows") val detectedRows: Int? = null,
    @SerializedName("blocked")       val blocked: Boolean = false,
    @SerializedName("warning")       val warning: String? = null,
    @SerializedName("preview")       val preview: String? = null,
    @SerializedName("error")         val error: String? = null
)

// ─── History ─────────────────────────────────────────────────────────────────

/**
 * Response from GET /api/mobile/history.
 * { "ok": true, "count": 5, "runs": [ { "id": 42, ... } ] }
 *
 * NOTE: backend uses key "id" not "run_id" in run_to_summary (line 1354).
 */
data class HistoryResponse(
    @SerializedName("ok")    val ok: Boolean,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("runs")  val runs: List<HistoryRun>? = null,
    @SerializedName("error") val error: String? = null
)

data class HistoryRun(
    @SerializedName("id")               val id: Int,
    @SerializedName("input_method")     val inputMethod: String,
    @SerializedName("program")          val program: String,
    @SerializedName("cgpa")             val cgpa: Double,
    @SerializedName("earned_credits")   val earnedCredits: Int,
    @SerializedName("required_credits") val requiredCredits: Int,
    @SerializedName("eligible")         val eligible: Boolean,
    @SerializedName("created_at")       val createdAt: String
)

/**
 * Response from GET /api/mobile/history/<run_id>.
 * { "ok": true, "run": { "id": 42, ... }, "issues": [...], ... }
 */
data class HistoryDetailsResponse(
    @SerializedName("ok")               val ok: Boolean,
    @SerializedName("run")              val run: HistoryRun? = null,
    @SerializedName("waived")           val waived: List<String>? = null,
    @SerializedName("issues")           val issues: List<String>? = null,
    @SerializedName("transcript_rows")  val transcriptRows: List<Map<String, Any?>>? = null,
    @SerializedName("latest_rows")      val latestRows: List<Map<String, Any?>>? = null,
    @SerializedName("cgpa")             val cgpa: Double? = null,
    @SerializedName("error")            val error: String? = null
)

// ─── AI Chat ─────────────────────────────────────────────────────────────────

data class AiChatRequest(
    @SerializedName("message") val message: String,
    @SerializedName("context") val context: Map<String, Any?> = emptyMap()
)

data class AiChatResponse(
    @SerializedName("reply")        val reply: String,
    @SerializedName("tool_trace")   val toolTrace: List<Any?> = emptyList(),
    @SerializedName("request_id")   val requestId: String? = null,
    @SerializedName("fallback_used")val fallbackUsed: Boolean = false
)
