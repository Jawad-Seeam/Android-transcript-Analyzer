package com.nsu.transcriptanalyzer.data.model

import com.google.gson.annotations.SerializedName

// Authentication
data class AuthResponse(
    @SerializedName("ok")
    val ok: Boolean,
    @SerializedName("access_token")
    val accessToken: String? = null,
    @SerializedName("token_type")
    val tokenType: String? = null,
    @SerializedName("expires_in")
    val expiresIn: Int? = null,
    @SerializedName("user")
    val user: User? = null,
    @SerializedName("error")
    val error: String? = null
)

data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)

// Analysis
data class AnalyzeRequest(
    @SerializedName("input_method")
    val inputMethod: String,
    @SerializedName("program")
    val program: String,
    @SerializedName("manual_text")
    val manualText: String? = null,
    @SerializedName("csv_text")
    val csvText: String? = null,
    @SerializedName("waived")
    val waived: List<String>? = null
)

data class AnalyzeResponse(
    @SerializedName("ok")
    val ok: Boolean,
    @SerializedName("result")
    val result: AnalysisResult? = null,
    @SerializedName("error")
    val error: String? = null
)

data class AnalysisResult(
    @SerializedName("cgpa")
    val cgpa: Double,
    @SerializedName("earned_credits")
    val earnedCredits: Int,
    @SerializedName("required_credits")
    val requiredCredits: Int,
    @SerializedName("eligible")
    val eligible: Boolean,
    @SerializedName("issues")
    val issues: List<String>,
    @SerializedName("waived_courses")
    val waivedCourses: List<String>,
    @SerializedName("audit")
    val audit: List<CourseAudit>,
    @SerializedName("run_id")
    val runId: Int? = null,
    @SerializedName("courses_analyzed")
    val coursesAnalyzed: Int
)

data class CourseAudit(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("credits")
    val credits: Int,
    @SerializedName("grade")
    val grade: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("semester")
    val semester: String? = null
)

// History
data class HistoryResponse(
    @SerializedName("ok")
    val ok: Boolean,
    @SerializedName("runs")
    val runs: List<HistoryRun>? = null,
    @SerializedName("error")
    val error: String? = null
)

data class HistoryRun(
    @SerializedName("run_id")
    val runId: Int,
    @SerializedName("program")
    val program: String,
    @SerializedName("cgpa")
    val cgpa: Double,
    @SerializedName("earned_credits")
    val earnedCredits: Int,
    @SerializedName("required_credits")
    val requiredCredits: Int,
    @SerializedName("eligible")
    val eligible: Boolean,
    @SerializedName("input_method")
    val inputMethod: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class HistoryDetailsResponse(
    @SerializedName("ok")
    val ok: Boolean,
    @SerializedName("run")
    val run: HistoryDetails? = null,
    @SerializedName("error")
    val error: String? = null
)

data class HistoryDetails(
    @SerializedName("run_id")
    val runId: Int,
    @SerializedName("program")
    val program: String,
    @SerializedName("cgpa")
    val cgpa: Double,
    @SerializedName("earned_credits")
    val earnedCredits: Int,
    @SerializedName("required_credits")
    val requiredCredits: Int,
    @SerializedName("eligible")
    val eligible: Boolean,
    @SerializedName("audit")
    val audit: List<CourseAudit>,
    @SerializedName("issues")
    val issues: List<String>,
    @SerializedName("waived_courses")
    val waivedCourses: List<String>,
    @SerializedName("input_method")
    val inputMethod: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("courses_analyzed")
    val coursesAnalyzed: Int
)
