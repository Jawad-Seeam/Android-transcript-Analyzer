package com.nsu.transcriptanalyzer.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.nsu.transcriptanalyzer.data.api.TranscriptApiService
import com.nsu.transcriptanalyzer.data.model.*
import com.nsu.transcriptanalyzer.data.prefs.SecurePreferencesManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

private const val TAG = "TranscriptRepo"

// ─── Sealed result ────────────────────────────────────────────────────────────

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

// ─── Repository ───────────────────────────────────────────────────────────────

class TranscriptRepository(
    private val context: Context,
    private val apiService: TranscriptApiService,
    private val securePrefs: SecurePreferencesManager
) {

    // ── State helpers ─────────────────────────────────────────────────────────

    fun isLoggedIn(): Boolean = securePrefs.isLoggedIn
    fun getCachedUser(): User? = securePrefs.getUser()

    // ── Auth ──────────────────────────────────────────────────────────────────

    /** POST /api/mobile/auth/google  –  body: { "id_token": "<google_id_token>" } */
    suspend fun authenticateWithGoogle(idToken: String): ApiResult<AuthResponse> =
        safeCall {
            val response = apiService.authenticateWithGoogle(mapOf("id_token" to idToken))
            if (response.ok && response.accessToken != null) {
                securePrefs.saveAccessToken(response.accessToken)
                response.user?.let { securePrefs.saveUser(it) }
                ApiResult.Success(response)
            } else {
                ApiResult.Error(response.error ?: "Google authentication failed")
            }
        }

    /** POST /api/mobile/auth/email  –  body: { "email": "...", "name": "..." } */
    suspend fun authenticateWithEmail(email: String, name: String): ApiResult<AuthResponse> =
        safeCall {
            val response = apiService.authenticateWithEmail(mapOf("email" to email, "name" to name))
            if (response.ok && response.accessToken != null) {
                securePrefs.saveAccessToken(response.accessToken)
                response.user?.let { securePrefs.saveUser(it) }
                ApiResult.Success(response)
            } else {
                ApiResult.Error(response.error ?: "Email authentication failed")
            }
        }

    /** GET /api/mobile/auth/me  –  verifies token is still valid */
    suspend fun verifyToken(): ApiResult<User> = safeCall {
        val response = apiService.getCurrentUser()
        if (response.ok && response.user != null) {
            securePrefs.saveUser(response.user)
            ApiResult.Success(response.user)
        } else {
            ApiResult.Error(response.error ?: "Token verification failed")
        }
    }

    fun logout() {
        securePrefs.clearAll()
        Log.d(TAG, "User logged out – token cleared")
    }

    // ── Analysis ──────────────────────────────────────────────────────────────

    /**
     * POST /api/mobile/analyze with JSON body.
     *
     * The backend mobile endpoint ONLY supports:
     *   inputMethod = "manual"  →  provide manualText
     *   inputMethod = "csv"     →  provide csvText (raw CSV string)
     *
     * For PDF/image files, call [ocrExtract] first, then call this with
     * the returned manualText and inputMethod="manual".
     */
    suspend fun analyzeTranscript(
        inputMethod: String,
        program: String,
        manualText: String? = null,
        csvText: String? = null
    ): ApiResult<AnalyzeResponse> {
        if (!securePrefs.isLoggedIn) return ApiResult.Error("Not authenticated. Please sign in.", 401)

        return safeCall {
            val request = AnalyzeRequest(
                inputMethod = inputMethod,
                program     = program,
                manualText  = manualText,
                csvText     = csvText
            )
            val response = apiService.analyzeTranscript(request)
            if (response.ok) {
                ApiResult.Success(response)
            } else {
                ApiResult.Error(response.error ?: "Analysis failed")
            }
        }
    }

    /**
     * POST /api/mobile/ocr/extract (multipart).
     *
     * Uploads a PDF or image; returns extracted course rows as [OcrExtractResponse.manualText].
     * The caller should then pass that manualText to [analyzeTranscript] with
     * inputMethod = "manual".
     *
     * File part name accepted by backend: "file" (line 1835 in app.py).
     */
    suspend fun ocrExtract(
        fileUri: Uri,
        inputMethod: String   // "pdf" or "image"
    ): ApiResult<OcrExtractResponse> {
        if (!securePrefs.isLoggedIn) return ApiResult.Error("Not authenticated.", 401)

        return safeCall {
            val mimeType = if (inputMethod == "pdf") "application/pdf" else
                (context.contentResolver.getType(fileUri) ?: "image/jpeg")

            val fileName = resolveFileName(fileUri) ?: "upload"
            val bytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
                ?: return@safeCall ApiResult.Error("Could not read file")

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)
            val methodPart = inputMethod.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.ocrExtract(methodPart, filePart)
            if (response.ok) {
                ApiResult.Success(response)
            } else {
                ApiResult.Error(response.error ?: "OCR extraction failed")
            }
        }
    }

    // ── History ───────────────────────────────────────────────────────────────

    suspend fun getHistory(): ApiResult<List<HistoryRun>> {
        if (!securePrefs.isLoggedIn) return ApiResult.Error("Not authenticated.", 401)
        return safeCall {
            val response = apiService.getHistory()
            if (response.ok) {
                ApiResult.Success(response.runs ?: emptyList())
            } else {
                ApiResult.Error(response.error ?: "Failed to load history")
            }
        }
    }

    suspend fun getHistoryDetails(runId: Int): ApiResult<HistoryDetailsResponse> {
        if (!securePrefs.isLoggedIn) return ApiResult.Error("Not authenticated.", 401)
        return safeCall {
            val response = apiService.getHistoryDetails(runId)
            if (response.ok) {
                ApiResult.Success(response)
            } else {
                ApiResult.Error(response.error ?: "Failed to load run details")
            }
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private fun resolveFileName(uri: Uri): String? = try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (col != -1 && cursor.moveToFirst()) cursor.getString(col) else null
        }
    } catch (e: Exception) {
        Log.w(TAG, "resolveFileName failed", e)
        null
    }

    /**
     * Wraps a suspend call with consistent error handling.
     * Maps HTTP 401 to a typed [ApiResult.Error] with code=401 so callers can
     * force re-login. All other exceptions become generic errors.
     */
    private suspend fun <T> safeCall(block: suspend () -> ApiResult<T>): ApiResult<T> = try {
        block()
    } catch (e: HttpException) {
        val code = e.code()
        Log.e(TAG, "HTTP $code: ${e.message()}", e)
        if (code == 401) {
            // Token expired / invalid – clear it so the UI shows the login screen
            securePrefs.clearAll()
            ApiResult.Error("Session expired. Please sign in again.", 401)
        } else {
            ApiResult.Error("Server error ($code): ${e.message()}", code)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Network/parse error", e)
        ApiResult.Error(e.message ?: "Unexpected error. Check your internet connection.")
    }
}
