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

        // ── 1. Secure token storage ─────────────────────────────────────────
        securePrefs = SecurePreferencesManager(applicationContext)

        // ── 2. Retrofit – use constant BASE_URL or resource override ─────────
        //    build.gradle.kts resValue sets backend_url to the Render URL.
        //    RetrofitClient.BASE_URL is the hardcoded fallback.
        val baseUrl = try {
            getString(R.string.backend_url).takeIf { it.isNotBlank() }
                ?: RetrofitClient.BASE_URL
        } catch (e: Exception) {
            RetrofitClient.BASE_URL
        }

        RetrofitClient.init(
            context     = applicationContext,
            baseUrl     = baseUrl,
            securePrefs = securePrefs
        )

        // ── 3. Repository & ViewModels ──────────────────────────────────────
        repository        = TranscriptRepository(applicationContext, RetrofitClient.apiService, securePrefs)
        authViewModel     = AuthViewModel(repository)
        analysisViewModel = AnalysisViewModel(repository)
        historyViewModel  = HistoryViewModel(repository)

        setContent {
            val authUiState     by authViewModel.uiState.collectAsState()
            val analysisUiState by analysisViewModel.uiState.collectAsState()
            val historyUiState  by historyViewModel.uiState.collectAsState()

            // ── Handle 401 from ANY ViewModel → force re-login ──────────────
            LaunchedEffect(analysisUiState.requiresReLogin) {
                if (analysisUiState.requiresReLogin) {
                    authViewModel.logout()
                    analysisViewModel.clearReLogin()
                }
            }
            LaunchedEffect(historyUiState.requiresReLogin) {
                if (historyUiState.requiresReLogin) {
                    authViewModel.logout()
                    historyViewModel.clearReLogin()
                }
            }

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0D1B4F)) {
                MainScreen(
                    authViewModel     = authViewModel,
                    analysisViewModel = analysisViewModel,
                    historyViewModel  = historyViewModel,
                    isAuthenticated   = authUiState.isAuthenticated,
                    onGoogleSignInClick = {
                        // Must pass Activity context for Credential Manager bottom-sheet
                        authViewModel.signInWithGoogle(
                            activityContext = this@MainActivity,
                            webClientId     = getString(R.string.google_client_id)
                        )
                    },
                    onLogout = { authViewModel.logout() }
                )
            }
        }
    }
}
