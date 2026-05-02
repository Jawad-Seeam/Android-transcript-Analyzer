package com.nsu.transcriptanalyzer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nsu.transcriptanalyzer.ui.navigation.AppNavGraph
import com.nsu.transcriptanalyzer.ui.navigation.NavRoutes
import com.nsu.transcriptanalyzer.ui.viewmodel.AnalysisViewModel
import com.nsu.transcriptanalyzer.ui.viewmodel.AuthViewModel
import com.nsu.transcriptanalyzer.ui.viewmodel.HistoryViewModel

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    analysisViewModel: AnalysisViewModel,
    historyViewModel: HistoryViewModel,
    isAuthenticated: Boolean,
    onGoogleSignInClick: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = isAuthenticated && currentRoute != NavRoutes.LOGIN

    // Use Scaffold so the NavBar is always pinned to the bottom and the
    // content automatically gets the correct bottom padding – no overlap.
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F3A93)
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Dashboard") },
                        selected = currentRoute == NavRoutes.DASHBOARD,
                        onClick = {
                            navController.navigate(NavRoutes.DASHBOARD) {
                                popUpTo(NavRoutes.DASHBOARD) { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text("History") },
                        selected = currentRoute == NavRoutes.HISTORY,
                        onClick = {
                            navController.navigate(NavRoutes.HISTORY)
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            authViewModel.logout()
                            onLogout()
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // innerPadding.calculateBottomPadding() == height of the NavigationBar
        // so content is never obscured by the bottom bar.
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            AppNavGraph(
                navController      = navController,
                authViewModel      = authViewModel,
                analysisViewModel  = analysisViewModel,
                historyViewModel   = historyViewModel,
                isAuthenticated    = isAuthenticated,
                onGoogleSignInClick = onGoogleSignInClick
            )
        }
    }
}
