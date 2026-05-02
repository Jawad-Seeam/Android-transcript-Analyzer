package com.nsu.transcriptanalyzer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nsu.transcriptanalyzer.data.model.HistoryDetails
import com.nsu.transcriptanalyzer.data.model.HistoryRun
import com.nsu.transcriptanalyzer.data.repository.ApiResult
import com.nsu.transcriptanalyzer.data.repository.TranscriptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = false,
    val runs: List<HistoryRun> = emptyList(),
    val selectedRunDetails: HistoryDetails? = null,
    val errorMessage: String? = null
)

class HistoryViewModel(private val repository: TranscriptRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun loadHistory() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.getHistory()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, runs = result.data)
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun loadRunDetails(runId: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.getHistoryDetails(runId)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, selectedRunDetails = result.data)
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun clearDetails() {
        _uiState.value = _uiState.value.copy(selectedRunDetails = null)
    }
}
