package com.nsu.transcriptanalyzer.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nsu.transcriptanalyzer.data.model.AnalysisResult
import com.nsu.transcriptanalyzer.data.repository.ApiResult
import com.nsu.transcriptanalyzer.data.repository.TranscriptRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "AnalysisViewModel"

// Valid letter grades accepted by the backend grade-points table
val VALID_GRADES = setOf("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F", "W", "I")

data class Course(
    val code: String     = "",
    val credits: String  = "",
    val grade: String    = "",          // must be a letter grade: A, B+, C, etc.
    val semester: String = "Spring 2024"
)

data class AnalysisUiState(
    val isLoading: Boolean          = false,
    val selectedProgram: String     = "CSE",
    val selectedInputMethod: String = "manual",

    // File state (csv / pdf / image pickers)
    val selectedFileUri: Uri?       = null,
    val selectedFileName: String?   = null,
    val selectedImageUri: Uri?      = null,

    // OCR / extraction intermediate state
    val ocrExtractedText: String?   = null,   // for pdf/image: manual_text rows
    val csvExtractedText: String?   = null,   // for csv: raw CSV file content
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
            csvExtractedText    = null,
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
            csvExtractedText = null,
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
            csvExtractedText = null,
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

    // ── Extract step ──────────────────────────────────────────────────────────

    /**
     * Unified "extract" handler:
     *  - CSV  → reads file bytes locally as UTF-8; NO API call needed
     *  - PDF  → calls /api/mobile/ocr/extract with input_method=pdf
     *  - Image→ calls /api/mobile/ocr/extract with input_method=image
     */
    fun extractOcr(context: Context) {
        val state = _uiState.value
        val uri   = state.selectedFileUri
        val method = state.selectedInputMethod

        if (uri == null) {
            _uiState.value = state.copy(errorMessage = "Please select a file first.")
            return
        }

        when (method) {
            "csv" -> extractCsvLocally(context, uri)
            "pdf", "image" -> extractViaOcrApi(context, uri, method)
            else -> _uiState.value = state.copy(errorMessage = "Select a file input mode first.")
        }
    }

    /** Reads the CSV file on-device — no network call. */
    private fun extractCsvLocally(context: Context, uri: Uri) {
        _uiState.value = _uiState.value.copy(
            isOcrLoading     = true,
            errorMessage     = null,
            csvExtractedText = null
        )
        viewModelScope.launch {
            val content: String? = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use {
                        it.bufferedReader(Charsets.UTF_8).readText()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "CSV read failed", e)
                    null
                }
            }
            if (!content.isNullOrBlank()) {
                val lineCount = content.lines().count { it.isNotBlank() }
                _uiState.value = _uiState.value.copy(
                    isOcrLoading     = false,
                    csvExtractedText = content,
                    // Reuse ocrExtractedText for the status row display
                    ocrExtractedText = content,
                    ocrConfidence    = "LOCAL",
                    ocrWarning       = null,
                    errorMessage     = null
                )
                Log.d(TAG, "CSV loaded locally: $lineCount lines")
            } else {
                _uiState.value = _uiState.value.copy(
                    isOcrLoading = false,
                    errorMessage = "Could not read CSV file. Make sure it is plain text UTF-8."
                )
            }
        }
    }

    /** Uploads PDF/image to backend OCR endpoint. */
    private fun extractViaOcrApi(context: Context, uri: Uri, method: String) {
        _uiState.value = _uiState.value.copy(
            isOcrLoading     = true,
            errorMessage     = null,
            ocrExtractedText = null
        )
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
                        isOcrLoading    = false,
                        errorMessage    = result.message,
                        requiresReLogin = result.code == 401
                    )
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Analyze ───────────────────────────────────────────────────────────────

    /**
     * Submits transcript for analysis via POST /api/mobile/analyze (JSON).
     *
     * Mode routing:
     *  manual → validates letter grades, sends manual_text
     *  csv    → sends raw CSV file content as csv_text (input_method="csv")
     *  pdf/image → OCR result already formatted as manual rows; sends manual_text
     */
    fun analyzeTranscript(context: Context) {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = when (state.selectedInputMethod) {

                "manual" -> {
                    val validCourses = state.courses.filter { it.code.isNotBlank() }
                    if (validCourses.isEmpty()) {
                        _uiState.value = state.copy(isLoading = false, errorMessage = "Add at least one course.")
                        return@launch
                    }
                    // Validate that each grade is a recognised letter grade
                    val badGrades = validCourses.filter { c ->
                        c.grade.isNotBlank() && c.grade.uppercase() !in VALID_GRADES
                    }
                    if (badGrades.isNotEmpty()) {
                        val sample = badGrades.first().grade
                        _uiState.value = state.copy(
                            isLoading    = false,
                            errorMessage = "Grade \"$sample\" is not valid. Use letter grades: A, B+, C, D, F, W, etc."
                        )
                        return@launch
                    }
                    // Format: "Course_Code,Credits,Grade,Semester"
                    val lines = validCourses.map { c ->
                        "${c.code},${c.credits.ifBlank { "3" }},${c.grade.ifBlank { "A" }.uppercase()},${c.semester}"
                    }
                    repository.analyzeTranscript(
                        inputMethod = "manual",
                        program     = state.selectedProgram,
                        manualText  = lines.joinToString("\n")
                    )
                }

                "csv" -> {
                    // csvExtractedText holds the raw file content read locally
                    val csv = state.csvExtractedText
                    if (csv.isNullOrBlank()) {
                        _uiState.value = state.copy(
                            isLoading    = false,
                            errorMessage = "Please tap 'Extract from CSV' first to load your file."
                        )
                        return@launch
                    }
                    // Send raw CSV content as csv_text — backend parses it
                    repository.analyzeTranscript(
                        inputMethod = "csv",
                        program     = state.selectedProgram,
                        csvText     = csv
                    )
                }

                "pdf", "image" -> {
                    val ocr = state.ocrExtractedText
                    if (ocr.isNullOrBlank()) {
                        _uiState.value = state.copy(
                            isLoading    = false,
                            errorMessage = "Please tap 'Extract Text' first to run OCR on your file."
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
