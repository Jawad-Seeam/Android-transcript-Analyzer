package com.nsu.transcriptanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nsu.transcriptanalyzer.data.api.RetrofitClient
import com.nsu.transcriptanalyzer.data.prefs.SecurePreferencesManager
import com.nsu.transcriptanalyzer.data.repository.TranscriptRepository
import com.nsu.transcriptanalyzer.ui.MainScreen
import com.nsu.transcriptanalyzer.ui.viewmodel.AnalysisViewModel
import com.nsu.transcriptanalyzer.ui.viewmodel.AuthViewModel
import com.nsu.transcriptanalyzer.ui.viewmodel.HistoryViewModel

class MainActivity : ComponentActivity() {

    private lateinit var securePrefs: SecurePreferencesManager
    private lateinit var repository: TranscriptRepository
    private lateinit var authViewModel: AuthViewModel
    private lateinit var analysisViewModel: AnalysisViewModel
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── 1. Secure preferences (EncryptedSharedPreferences) ─────────────
        securePrefs = SecurePreferencesManager(applicationContext)

        // ── 2. Initialise Retrofit with AuthTokenInterceptor ────────────────
        val baseUrl = getString(R.string.backend_url)
        RetrofitClient.init(
            context    = applicationContext,
            baseUrl    = baseUrl,
            securePrefs = securePrefs
        )

        // ── 3. Repository & ViewModels ──────────────────────────────────────
        repository       = TranscriptRepository(applicationContext, RetrofitClient.apiService, securePrefs)
        authViewModel    = AuthViewModel(repository)
        analysisViewModel = AnalysisViewModel(repository)
        historyViewModel = HistoryViewModel(repository)

        setContent {
            val authUiState by authViewModel.uiState.collectAsState()

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0D1B4F)) {
                MainScreen(
                    authViewModel     = authViewModel,
                    analysisViewModel = analysisViewModel,
                    historyViewModel  = historyViewModel,
                    // Derive authentication state from ViewModel (not hardcoded true)
                    isAuthenticated   = authUiState.isAuthenticated,
                    onGoogleSignInClick = {
                        // Pass the Activity context (this) – required by Credential Manager
                        authViewModel.signInWithGoogle(
                            activityContext = this,
                            webClientId     = getString(R.string.google_client_id)
                        )
                    },
                    onLogout = {
                        authViewModel.logout()
                    }
                )
            }
        }
    }
}
