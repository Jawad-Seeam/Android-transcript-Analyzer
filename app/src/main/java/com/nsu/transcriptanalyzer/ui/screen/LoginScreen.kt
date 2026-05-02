package com.nsu.transcriptanalyzer.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nsu.transcriptanalyzer.R
import com.nsu.transcriptanalyzer.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onGoogleSignInClick: () -> Unit,   // kept for nav-graph compat
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var name  by remember { mutableStateOf("") }
    var showEmailInput by remember { mutableStateOf(false) }

    // Show errors as Toasts
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    // Navigate on success
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onLoginSuccess()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D1B4F), Color(0xFF1F3A93)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            // Logo
            Box(
                Modifier.size(90.dp).clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, null, modifier = Modifier.size(52.dp), tint = Color.White)
            }

            Text("NSU Transcript Analyzer", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Sign in to analyze your transcript", fontSize = 13.sp, color = Color(0xFFB0C4FF))

            Spacer(Modifier.height(8.dp))

            // Error card
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(uiState.errorMessage ?: "", color = Color(0xFFC62828),
                        fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // ── Google Sign-In Button ──────────────────────────────────────
            Button(
                onClick = {
                    // Use the modern Credential Manager via ViewModel
                    val clientId = context.getString(R.string.google_client_id)
                    authViewModel.signInWithGoogle(context, clientId)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                enabled = !uiState.isLoading
            ) {
                Icon(Icons.Default.Person, null, tint = Color(0xFF1F3A93), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Sign in with Google", color = Color(0xFF1F3A93), fontWeight = FontWeight.SemiBold)
            }

            // ── Divider ──────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF424242))
                Text("OR", color = Color(0xFF757575), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF424242))
            }

            // ── Email Sign-In Button ──────────────────────────────────────
            Button(
                onClick = { showEmailInput = !showEmailInput },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26C97C)),
                enabled = !uiState.isLoading
            ) {
                Icon(Icons.Default.Email, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Sign in with Email", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            // ── Email input form ──────────────────────────────────────────
            AnimatedVisibility(
                visible = showEmailInput,
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("NSU Email", color = Color.White) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        placeholder = { Text("student@northsouth.edu", color = Color(0xFFBDBDBD)) }
                    )
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Full Name", color = Color.White) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                    Button(
                        onClick = { if (email.isNotEmpty() && name.isNotEmpty()) authViewModel.authenticateWithEmail(email, name) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = email.isNotEmpty() && name.isNotEmpty() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Continue", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Global loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp), color = Color.White, strokeWidth = 3.dp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
