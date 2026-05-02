package com.nsu.transcriptanalyzer.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nsu.transcriptanalyzer.data.model.AnalysisResult
import com.nsu.transcriptanalyzer.data.repository.ApiResult
import com.nsu.transcriptanalyzer.data.repository.TranscriptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "AnalysisViewModel"

data class Course(
    val code: String    = "",
    val credits: String = "",
    val grade: String   = "",
    val semester: String = "Spring 2024"   // backend requires Semester field
)

data class AnalysisUiState(
    val isLoading: Boolean          = false,
    val selectedProgram: String     = "CSE",
    val selectedInputMethod: String = "manual",

    // File state (csv / pdf / image pickers)
    val selectedFileUri: Uri?       = null,
    val selectedFileName: String?   = null,
    val selectedImageUri: Uri?      = null,

    // OCR intermediate state (for PDF/image flow)
    val ocrExtractedText: String?   = null,
    val ocrConfidence: String?      = null,
    val ocrWarning: String?         = null,
    val isOcrLoading: Boolean       = false,

    // Manual mode
    val courses: List<Course>       = listOf(Course()),

    // Analysis result
    val analysisResult: AnalysisResult? = null,
    val runId: Int?                 = null,
    val errorMessage: String?       = null,
    val showResults: Boolean        = false,

    // 401 flag to force re-login
    val requiresReLogin: Boolean    = false
)

class AnalysisViewModel(private val repository: TranscriptRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    // ── Selectors ─────────────────────────────────────────────────────────────

    fun setProgram(program: String) {
        _uiState.value = _uiState.value.copy(selectedProgram = program)
    }

    fun setInputMethod(method: String) {
        _uiState.value = _uiState.value.copy(
            selectedInputMethod = method,
            selectedFileUri     = null,
            selectedFileName    = null,
            selectedImageUri    = null,
            ocrExtractedText    = null,
            ocrConfidence       = null,
            ocrWarning          = null,
            errorMessage        = null
        )
    }

    fun setSelectedFile(uri: Uri?, fileName: String?) {
        _uiState.value = _uiState.value.copy(
            selectedFileUri  = uri,
            selectedFileName = fileName,
            ocrExtractedText = null,
            ocrWarning       = null,
            errorMessage     = null
        )
    }

    fun setSelectedImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            selectedFileUri  = uri,
            selectedFileName = "photo.jpg",
            ocrExtractedText = null,
            ocrWarning       = null,
            errorMessage     = null
        )
    }

    // ── Manual courses ────────────────────────────────────────────────────────

    fun updateCourse(index: Int, course: Course) {
        val updated = _uiState.value.courses.toMutableList()
        if (index in updated.indices) {
            updated[index] = course
            _uiState.value = _uiState.value.copy(courses = updated)
        }
    }

    fun addCourse() {
        _uiState.value = _uiState.value.copy(
            courses = _uiState.value.courses + Course()
        )
    }

    fun removeCourse(index: Int) {
        val updated = _uiState.value.courses.toMutableList()
        if (updated.size > 1) {
            updated.removeAt(index)
            _uiState.value = _uiState.value.copy(courses = updated)
        }
    }

    // ── OCR Extract (PDF / Image → manual_text) ───────────────────────────────

    /**
     * Step 1 for PDF/image mode: extract text via /api/mobile/ocr/extract.
     * On success the extracted text is stored in ocrExtractedText.
     * The user can then press Analyze to submit it.
     */
    fun extractOcr(context: Context) {
        val state = _uiState.value
        val uri = state.selectedFileUri
        val method = state.selectedInputMethod

        if (uri == null) {
            _uiState.value = state.copy(errorMessage = "Please select a file first.")
            return
        }
        if (method !in listOf("pdf", "image")) {
            _uiState.value = state.copy(errorMessage = "Select PDF or Image mode for OCR.")
            return
        }

        _uiState.value = state.copy(isOcrLoading = true, errorMessage = null, ocrExtractedText = null)

        viewModelScope.launch {
            when (val result = repository.ocrExtract(uri, method)) {
                is ApiResult.Success -> {
                    val data = result.data
                    Log.d(TAG, "OCR done: ${data.detectedRows} rows, confidence=${data.confidence}")
                    _uiState.value = _uiState.value.copy(
                        isOcrLoading     = false,
                        ocrExtractedText = data.manualText,
                        ocrConfidence    = data.confidence,
                        ocrWarning       = data.warning,
                        errorMessage     = if (data.blocked)
                            "OCR blocked (low confidence). Upload a clearer file or use Manual/CSV."
                        else null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isOcrLoading = false,
                        errorMessage = result.message,
                        requiresReLogin = result.code == 401
                    )
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Analyze ───────────────────────────────────────────────────────────────

    /**
     * Submit transcript for analysis via POST /api/mobile/analyze (JSON).
     *
     * Flow per mode:
     *  - manual → formats course list as CSV text
     *  - csv    → sends raw csvText field (NOT implemented yet – use manual)
     *  - pdf/image → sends ocrExtractedText as manual_text with inputMethod="manual"
     */
    fun analyzeTranscript(context: Context) {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = when (state.selectedInputMethod) {
                "manual" -> {
                    // Format: "Course_Code,Credits,Grade,Semester" per line
                    val lines = state.courses.filter { it.code.isNotBlank() }.map { c ->
                        "${c.code},${c.credits.ifBlank { "3" }},${c.grade.ifBlank { "A" }},${c.semester}"
                    }
                    if (lines.isEmpty()) {
                        _uiState.value = state.copy(isLoading = false, errorMessage = "Add at least one course.")
                        return@launch
                    }
                    repository.analyzeTranscript(
                        inputMethod = "manual",
                        program     = state.selectedProgram,
                        manualText  = lines.joinToString("\n")
                    )
                }
                "pdf", "image" -> {
                    // Must have already run OCR extract
                    val ocr = state.ocrExtractedText
                    if (ocr.isNullOrBlank()) {
                        _uiState.value = state.copy(
                            isLoading    = false,
                            errorMessage = "Please tap 'Extract Text' first to run OCR on your file."
                        )
                        return@launch
                    }
                    // Submit OCR result as manual_text
                    repository.analyzeTranscript(
                        inputMethod = "manual",
                        program     = state.selectedProgram,
                        manualText  = ocr
                    )
                }
                "csv" -> {
                    // CSV mode: user must have uploaded a file; OCR endpoint handles it
                    val ocr = state.ocrExtractedText
                    if (ocr.isNullOrBlank()) {
                        _uiState.value = state.copy(
                            isLoading    = false,
                            errorMessage = "Please tap 'Extract from CSV' first."
                        )
                        return@launch
                    }
                    repository.analyzeTranscript(
                        inputMethod = "manual",
                        program     = state.selectedProgram,
                        manualText  = ocr
                    )
                }
                else -> {
                    _uiState.value = state.copy(isLoading = false, errorMessage = "Unknown input method.")
                    return@launch
                }
            }

            when (result) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Analysis success: runId=${result.data.runId}, cgpa=${result.data.result?.cgpa}")
                    _uiState.value = state.copy(
                        isLoading      = false,
                        analysisResult = result.data.result,
                        runId          = result.data.runId,
                        showResults    = true
                    )
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Analysis error (${result.code}): ${result.message}")
                    _uiState.value = state.copy(
                        isLoading       = false,
                        errorMessage    = result.message,
                        requiresReLogin = result.code == 401
                    )
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Misc ──────────────────────────────────────────────────────────────────

    fun resetAnalysis() { _uiState.value = AnalysisUiState() }
    fun clearError()    { _uiState.value = _uiState.value.copy(errorMessage = null) }
    fun clearReLogin()  { _uiState.value = _uiState.value.copy(requiresReLogin = false) }
}
