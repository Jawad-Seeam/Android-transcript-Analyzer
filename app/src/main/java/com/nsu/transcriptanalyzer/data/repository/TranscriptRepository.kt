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

private const val TAG = "TranscriptRepo"

// ─── Result wrapper ───────────────────────────────────────────────────────────

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

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun authenticateWithGoogle(idToken: String): ApiResult<AuthResponse> = runCatching {
        val response = apiService.authenticateWithGoogle(mapOf("id_token" to idToken))
        if (response.ok && response.accessToken != null) {
            securePrefs.saveAccessToken(response.accessToken)
            response.user?.let { securePrefs.saveUser(it) }
            ApiResult.Success(response)
        } else {
            ApiResult.Error(response.error ?: "Authentication failed")
        }
    }.getOrElse {
        Log.e(TAG, "authenticateWithGoogle", it)
        ApiResult.Error(it.message ?: "Network error during Google authentication")
    }

    suspend fun authenticateWithEmail(email: String, name: String): ApiResult<AuthResponse> = runCatching {
        val response = apiService.authenticateWithEmail(mapOf("email" to email, "name" to name))
        if (response.ok && response.accessToken != null) {
            securePrefs.saveAccessToken(response.accessToken)
            response.user?.let { securePrefs.saveUser(it) }
            ApiResult.Success(response)
        } else {
            ApiResult.Error(response.error ?: "Authentication failed")
        }
    }.getOrElse {
        Log.e(TAG, "authenticateWithEmail", it)
        ApiResult.Error(it.message ?: "Network error during email authentication")
    }

    fun logout() {
        securePrefs.clearAll()
    }

    fun isLoggedIn(): Boolean = securePrefs.isLoggedIn
    fun getCachedUser(): User? = securePrefs.getUser()

    // ── Analysis ──────────────────────────────────────────────────────────────

    /**
     * Upload a transcript for analysis.
     *
     * For CSV, PDF, and Image modes: reads the file bytes from the content URI
     * and builds a proper multipart/form-data request.
     *
     * For Manual mode: serialises the course list as CSV text sent in the
     * `manual_text` form field.
     */
    suspend fun analyzeTranscript(
        inputMethod: String,
        program: String,
        fileUri: Uri? = null,
        manualText: String? = null
    ): ApiResult<AnalysisResult> {

        if (!securePrefs.isLoggedIn) return ApiResult.Error("Not authenticated. Please sign in.")

        return runCatching {
            val programBody     = program.toRequestBody("text/plain".toMediaTypeOrNull())
            val inputMethodBody = inputMethod.toRequestBody("text/plain".toMediaTypeOrNull())
            val manualBody      = manualText?.toRequestBody("text/plain".toMediaTypeOrNull())

            // Build multipart file part (null for manual mode)
            val filePart: MultipartBody.Part? = fileUri?.let { uri ->
                buildFilePart(uri, inputMethod)
            }

            val response = apiService.analyzeTranscript(
                file        = filePart,
                program     = programBody,
                inputMethod = inputMethodBody,
                manualText  = manualBody
            )

            if (response.ok && response.result != null) {
                ApiResult.Success(response.result)
            } else {
                ApiResult.Error(response.error ?: "Analysis failed")
            }
        }.getOrElse {
            Log.e(TAG, "analyzeTranscript", it)
            ApiResult.Error(it.message ?: "Network error during analysis")
        }
    }

    /** Build a correctly-typed multipart Part from a content URI. */
    private fun buildFilePart(uri: Uri, inputMethod: String): MultipartBody.Part? {
        return try {
            val fileName = resolveFileName(uri) ?: "upload"
            val mimeType = when (inputMethod) {
                "csv"   -> "text/csv"
                "pdf"   -> "application/pdf"
                "image" -> context.contentResolver.getType(uri) ?: "image/jpeg"
                else    -> "application/octet-stream"
            }
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", fileName, requestBody)
        } catch (e: Exception) {
            Log.e(TAG, "buildFilePart: could not read URI $uri", e)
            null
        }
    }

    /** Resolve the human-readable file name from a content URI. */
    private fun resolveFileName(uri: Uri): String? = try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (col != -1 && cursor.moveToFirst()) cursor.getString(col) else null
        }
    } catch (e: Exception) {
        Log.w(TAG, "resolveFileName failed", e)
        null
    }

    // ── History ───────────────────────────────────────────────────────────────

    suspend fun getHistory(): ApiResult<List<HistoryRun>> = runCatching {
        if (!securePrefs.isLoggedIn) return ApiResult.Error("Not authenticated")
        val response = apiService.getHistory()
        if (response.ok && response.runs != null) {
            ApiResult.Success(response.runs)
        } else {
            ApiResult.Error(response.error ?: "Failed to load history")
        }
    }.getOrElse {
        Log.e(TAG, "getHistory", it)
        ApiResult.Error(it.message ?: "Network error loading history")
    }

    suspend fun getHistoryDetails(runId: Int): ApiResult<HistoryDetails> = runCatching {
        if (!securePrefs.isLoggedIn) return ApiResult.Error("Not authenticated")
        val response = apiService.getHistoryDetails(runId)
        if (response.ok && response.run != null) {
            ApiResult.Success(response.run)
        } else {
            ApiResult.Error(response.error ?: "Failed to load details")
        }
    }.getOrElse {
        Log.e(TAG, "getHistoryDetails", it)
        ApiResult.Error(it.message ?: "Network error loading details")
    }
}
