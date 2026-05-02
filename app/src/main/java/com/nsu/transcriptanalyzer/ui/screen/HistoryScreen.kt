package com.nsu.transcriptanalyzer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nsu.transcriptanalyzer.data.model.HistoryDetails
import com.nsu.transcriptanalyzer.data.model.HistoryRun
import com.nsu.transcriptanalyzer.ui.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by historyViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        historyViewModel.loadHistory()
    }

    if (uiState.selectedRunDetails != null) {
        HistoryDetailsScreen(
            details = uiState.selectedRunDetails!!,
            isLoading = uiState.isLoading,
            onBackClick = { historyViewModel.clearDetails() }
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F3A93))
                        .padding(24.dp)
                ) {
                    Text(
                        text = "History",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (uiState.isLoading && uiState.runs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF1F3A93))
                    }
                }
            } else if (uiState.runs.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(
                    items = uiState.runs,
                    key = { it.runId }
                ) { run ->
                    HistoryRunCard(
                        run = run,
                        onClick = { historyViewModel.loadRunDetails(run.runId) },
                        modifier = Modifier
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun HistoryRunCard(
    run: HistoryRun,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = run.program,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F3A93)
                    )
                    Text(
                        text = run.createdAt,
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
                InputMethodBadge(run.inputMethod)
            }

            // CGPA & Credits Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "CGPA", value = String.format("%.2f", run.cgpa))
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp),
                    color = Color(0xFFE0E0E0)
                )
                StatItem(label = "Earned", value = run.earnedCredits.toString())
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp),
                    color = Color(0xFFE0E0E0)
                )
                StatItem(label = "Credits", value = run.requiredCredits.toString())
            }

            // Eligibility Status
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (run.eligible) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (run.eligible) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (run.eligible) "Eligible" else "Not Eligible",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (run.eligible) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF999999)
                )
            }
        }
    }
}

@Composable
private fun InputMethodBadge(method: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFE3F2FD)
    ) {
        Text(
            text = method.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F3A93),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color(0xFF999999))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
    }
}

@Composable
private fun EmptyStateCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HistoryToggleOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFBDBDBD)
            )
            Text(
                text = "No history yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF999999)
            )
            Text(
                text = "Analyze a transcript to see it here",
                fontSize = 12.sp,
                color = Color(0xFFBDBDBD)
            )
        }
    }
}

@Composable
fun HistoryDetailsScreen(
    details: HistoryDetails,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F3A93))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = details.program,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = details.createdAt,
                            fontSize = 12.sp,
                            color = Color(0xFFBDBDBD)
                        )
                    }
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // CGPA Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (details.eligible)
                        Color(0xFF2E7D32) else Color(0xFFC62828)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CGPA",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = String.format("%.2f", details.cgpa),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Credits Info
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Input Method", details.inputMethod)
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE0E0E0))
                    InfoRow("Credits Earned", details.earnedCredits.toString())
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE0E0E0))
                    InfoRow("Credits Required", details.requiredCredits.toString())
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE0E0E0))
                    InfoRow("Courses Analyzed", details.coursesAnalyzed.toString())
                }
            }
        }

        // Audit List
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Degree Audit",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(details.audit) { course ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = course.code,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F3A93)
                        )
                        Text(
                            text = course.name,
                            fontSize = 10.sp,
                            color = Color(0xFF666666)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = course.grade,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        StatusChip(course.status)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF666666))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bgColor, textColor) = when (status) {
        "COMPLETED" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "MISSING" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "FAILED" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "INCOMPLETE" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        else -> Color(0xFFF0F0F0) to Color(0xFF666666)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = status,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
