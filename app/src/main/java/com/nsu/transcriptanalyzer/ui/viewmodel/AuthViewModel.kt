package com.nsu.transcriptanalyzer.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.nsu.transcriptanalyzer.data.model.User
import com.nsu.transcriptanalyzer.data.repository.ApiResult
import com.nsu.transcriptanalyzer.data.repository.TranscriptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

class AuthViewModel(private val repository: TranscriptRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Check if the user is already signed in (token in EncryptedSharedPreferences)
    init {
        if (repository.isLoggedIn()) {
            _uiState.value = AuthUiState(
                isAuthenticated = true,
                user = repository.getCachedUser()
            )
        }
    }

    // ── Google Sign-In via Credential Manager ─────────────────────────────────

    /**
     * Launches the modern Credential Manager bottom-sheet for Google Sign-In.
     *
     * Pass your Web Client ID (the one registered in Google Cloud Console,
     * NOT the Android Client ID) as [webClientId].
     *
     * @param activityContext Must be an Activity context (NOT applicationContext)
     *                        so that the Credential Manager can display its UI.
     */
    fun signInWithGoogle(activityContext: Context, webClientId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activityContext)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // show all accounts, not just previously used
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)  // always show the picker
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = activityContext,
                    request = request
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    Log.d(TAG, "Got Google ID token. Sending to backend …")
                    sendGoogleTokenToBackend(idToken)
                } else {
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Unexpected credential type. Please try again."
                    )
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "Google Sign-In cancelled by user")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "GetCredentialException: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google Sign-In failed: ${e.message}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in signInWithGoogle", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.localizedMessage}"
                )
            }
        }
    }

    private suspend fun sendGoogleTokenToBackend(idToken: String) {
        when (val result = repository.authenticateWithGoogle(idToken)) {
            is ApiResult.Success -> {
                Log.d(TAG, "Backend auth success: ${result.data.user?.email}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    user = result.data.user
                )
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Backend auth error: ${result.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
            is ApiResult.Loading -> { /* no-op */ }
        }
    }

    // ── Email Sign-In ─────────────────────────────────────────────────────────

    fun authenticateWithEmail(email: String, name: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.authenticateWithEmail(email, name)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = result.data.user
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
