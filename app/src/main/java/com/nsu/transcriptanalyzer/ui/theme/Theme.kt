package com.nsu.transcriptanalyzer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1F3A93),
    secondary = Color(0xFF4CAF50),
    tertiary = Color(0xFF2196F3),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    error = Color(0xFFC62828)
)

@Composable
fun NSUTranscriptAnalyzerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
