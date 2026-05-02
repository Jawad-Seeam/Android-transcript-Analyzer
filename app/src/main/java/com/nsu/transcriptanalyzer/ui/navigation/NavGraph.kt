package com.nsu.transcriptanalyzer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nsu.transcriptanalyzer.ui.screen.DashboardScreen
import com.nsu.transcriptanalyzer.ui.screen.HistoryScreen
import com.nsu.transcriptanalyzer.ui.screen.LoginScreen
import com.nsu.transcriptanalyzer.ui.viewmodel.AnalysisViewModel
import com.nsu.transcriptanalyzer.ui.viewmodel.AuthViewModel
import com.nsu.transcriptanalyzer.ui.viewmodel.HistoryViewModel

object NavRoutes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    analysisViewModel: AnalysisViewModel,
    historyViewModel: HistoryViewModel,
    isAuthenticated: Boolean,
    onGoogleSignInClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) NavRoutes.DASHBOARD else NavRoutes.LOGIN
    ) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onGoogleSignInClick = onGoogleSignInClick,
                onLoginSuccess = {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.DASHBOARD) {
            DashboardScreen(analysisViewModel)
        }

        composable(NavRoutes.HISTORY) {
            HistoryScreen(historyViewModel)
        }
    }
}
