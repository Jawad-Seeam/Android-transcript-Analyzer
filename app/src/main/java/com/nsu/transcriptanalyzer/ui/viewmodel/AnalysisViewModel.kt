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
    val name: String    = "",
    val credits: String = "",
    val grade: String   = ""
)

data class AnalysisUiState(
    val isLoading: Boolean          = false,
    val selectedProgram: String     = "CSE",
    val selectedInputMethod: String = "manual",

    // File selection state – shared by csv/pdf/image modes
    val selectedFileUri: Uri?       = null,
    val selectedFileName: String?   = null, // e.g. "transcript.pdf"

    // Image preview (same URI for image mode, kept separate for clarity)
    val selectedImageUri: Uri?      = null,

    // Manual mode
    val courses: List<Course>       = listOf(Course()),

    // Analysis output
    val analysisResult: AnalysisResult? = null,
    val errorMessage: String?       = null,
    val showResults: Boolean        = false
)

class AnalysisViewModel(private val repository: TranscriptRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    // ── Program & mode ────────────────────────────────────────────────────────

    fun setProgram(program: String) {
        _uiState.value = _uiState.value.copy(selectedProgram = program)
    }

    fun setInputMethod(method: String) {
        // Reset file / image selection when mode changes
        _uiState.value = _uiState.value.copy(
            selectedInputMethod = method,
            selectedFileUri     = null,
            selectedFileName    = null,
            selectedImageUri    = null,
            errorMessage        = null
        )
    }

    // ── File selection ────────────────────────────────────────────────────────

    fun setSelectedFile(uri: Uri?, fileName: String?) {
        _uiState.value = _uiState.value.copy(
            selectedFileUri  = uri,
            selectedFileName = fileName,
            errorMessage     = null
        )
    }

    fun setSelectedImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            selectedFileUri  = uri,          // reuse same field for upload
            selectedFileName = "photo.jpg",
            errorMessage     = null
        )
    }

    // ── Manual mode ───────────────────────────────────────────────────────────

    fun updateCourse(index: Int, course: Course) {
        val updated = _uiState.value.courses.toMutableList()
        if (index in updated.indices) {
            updated[index] = course
            _uiState.value = _uiState.value.copy(courses = updated)
        }
    }

    fun addCourse() {
        val updated = _uiState.value.courses.toMutableList()
        updated.add(Course())
        _uiState.value = _uiState.value.copy(courses = updated)
    }

    fun removeCourse(index: Int) {
        val updated = _uiState.value.courses.toMutableList()
        if (updated.size > 1) {
            updated.removeAt(index)
            _uiState.value = _uiState.value.copy(courses = updated)
        }
    }

    // ── Analysis ──────────────────────────────────────────────────────────────

    fun analyzeTranscript(context: Context) {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = when (state.selectedInputMethod) {
                "manual" -> {
                    val manualText = state.courses
                        .filter { it.code.isNotBlank() }
                        .joinToString("\n") { c ->
                            // Backend expects: Course_Code,Credits,Grade,Semester
                            // We default Semester to "Spring 2024" when not provided
                            val sem = "Spring 2024"
                            "${c.code},${c.credits},${c.grade},$sem"
                        }
                    if (manualText.isBlank()) {
                        _uiState.value = state.copy(
                            isLoading    = false,
                            errorMessage = "Please add at least one course."
                        )
                        return@launch
                    }
                    repository.analyzeTranscript(
                        inputMethod = "manual",
                        program     = state.selectedProgram,
                        manualText  = manualText
                    )
                }
                "csv", "pdf", "image" -> {
                    if (state.selectedFileUri == null) {
                        _uiState.value = state.copy(
                            isLoading    = false,
                            errorMessage = "Please select a file first."
                        )
                        return@launch
                    }
                    repository.analyzeTranscript(
                        inputMethod = state.selectedInputMethod,
                        program     = state.selectedProgram,
                        fileUri     = state.selectedFileUri
                    )
                }
                else -> {
                    _uiState.value = state.copy(isLoading = false, errorMessage = "Unknown input method.")
                    return@launch
                }
            }

            when (result) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Analysis success: ${result.data.cgpa}")
                    _uiState.value = state.copy(
                        isLoading      = false,
                        analysisResult = result.data,
                        showResults    = true
                    )
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Analysis error: ${result.message}")
                    _uiState.value = state.copy(
                        isLoading    = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun resetAnalysis() {
        _uiState.value = AnalysisUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
