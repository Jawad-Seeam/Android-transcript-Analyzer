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
import com.nsu.transcriptanalyzer.data.model.CourseAudit
import com.nsu.transcriptanalyzer.data.model.HistoryDetailsResponse
import com.nsu.transcriptanalyzer.data.model.HistoryRun
import com.nsu.transcriptanalyzer.ui.viewmodel.HistoryViewModel

// ─── Colors ───────────────────────────────────────────────────────────────────
private val NavyMid  = Color(0xFF1F3A93)
private val Grey50   = Color(0xFFF5F5F5)
private val Grey200  = Color(0xFFE0E0E0)
private val Grey400  = Color(0xFF999999)
private val Grey600  = Color(0xFF666666)
private val Grey800  = Color(0xFF333333)
private val Green    = Color(0xFF2E7D32)
private val Red      = Color(0xFFC62828)

// ─── Entry point ──────────────────────────────────────────────────────────────

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
            details     = uiState.selectedRunDetails!!,
            isLoading   = uiState.isLoading,
            onBackClick = { historyViewModel.clearDetails() }
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Grey50)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyMid)
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

            // Error banner
            if (uiState.errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            uiState.errorMessage ?: "",
                            color = Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            if (uiState.isLoading && uiState.runs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NavyMid)
                    }
                }
            } else if (uiState.runs.isEmpty()) {
                item { EmptyStateCard() }
            } else {
                items(
                    items = uiState.runs,
                    key   = { it.id }           // HistoryRun.id (not runId)
                ) { run ->
                    HistoryRunCard(
                        run     = run,
                        onClick = { historyViewModel.loadRunDetails(run.id) }   // run.id
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ─── Run card ─────────────────────────────────────────────────────────────────

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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row
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
                        color = NavyMid
                    )
                    Text(
                        text = run.createdAt,
                        fontSize = 12.sp,
                        color = Grey400
                    )
                }
                InputMethodBadge(run.inputMethod)
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Grey50)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "CGPA",    value = String.format("%.2f", run.cgpa))
                Divider(
                    modifier = Modifier.width(1.dp).height(40.dp),
                    color = Grey200
                )
                StatItem(label = "Earned",  value = run.earnedCredits.toString())
                Divider(
                    modifier = Modifier.width(1.dp).height(40.dp),
                    color = Grey200
                )
                StatItem(label = "Required", value = run.requiredCredits.toString())
            }

            // Eligibility row
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        tint = if (run.eligible) Green else Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (run.eligible) "Eligible" else "Not Eligible",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (run.eligible) Green else Red
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Grey400
                )
            }
        }
    }
}

// ─── Details screen ───────────────────────────────────────────────────────────

/**
 * Uses [HistoryDetailsResponse] which contains:
 *   - run: HistoryRun  (program, createdAt, cgpa, earnedCredits, requiredCredits, eligible)
 *   - issues: List<String>
 *   - courseAudit comes from the result embedded in run; we surface what we have
 *
 * NOTE: The backend /api/mobile/history/<id> returns:
 *   { ok, run: { id, program, cgpa, earned_credits, required_credits, eligible, created_at },
 *     waived, issues, transcript_rows, latest_rows, cgpa }
 * There is no "audit" or "course.code/name/grade" in the history detail response.
 * We display the summary run data + issues list + latest_rows as a course table.
 */
@Composable
fun HistoryDetailsScreen(
    details: HistoryDetailsResponse,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // The nested `run` summary object (always present when ok==true)
    val run = details.run

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Grey50)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyMid)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = run?.program ?: "Details",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = run?.createdAt ?: "",
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

        // Loading indicator
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = NavyMid) }
            }
        }

        // CGPA Card
        if (run != null) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (run.eligible) Green else Red
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("CGPA", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = String.format("%.2f", run.cgpa),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (run.eligible) "✓ Eligible for graduation" else "✗ Not eligible",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow("Program",          run.program)
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Grey200)
                        InfoRow("Input Method",     run.inputMethod.replaceFirstChar { it.uppercase() })
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Grey200)
                        InfoRow("Credits Earned",   run.earnedCredits.toString())
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Grey200)
                        InfoRow("Credits Required", run.requiredCredits.toString())
                        // cgpa from top-level (more precise than nested run.cgpa)
                        if (details.cgpa != null) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Grey200)
                            InfoRow("CGPA (precise)", String.format("%.4f", details.cgpa))
                        }
                    }
                }
            }
        }

        // Issues list
        val issues = details.issues
        if (!issues.isNullOrEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Issues (${issues.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            items(issues) { issue ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Red, modifier = Modifier.size(16.dp))
                        Text(issue, fontSize = 12.sp, color = Red, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Latest rows table (course → grade)
        val latestRows = details.latestRows
        if (!latestRows.isNullOrEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Courses (${latestRows.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            items(latestRows) { row ->
                // row is Map<String, Any?> from the backend:
                // { Course_Code, Credits, Grade, Semester }
                val code    = row["Course_Code"]?.toString() ?: "-"
                val grade   = row["Grade"]?.toString() ?: "-"
                val credits = row["Credits"]?.toString() ?: "-"
                val sem     = row["Semester"]?.toString() ?: ""

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = code,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyMid
                            )
                            if (sem.isNotBlank()) {
                                Text(sem, fontSize = 10.sp, color = Grey400)
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${credits}cr", fontSize = 11.sp, color = Grey600)
                            GradeChip(grade)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// ─── Small composables ────────────────────────────────────────────────────────

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
            color = NavyMid,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Grey400)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Grey800)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Grey600)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Grey800)
    }
}

@Composable
private fun GradeChip(grade: String) {
    val bg = when (grade) {
        "A", "A+" -> Color(0xFFE8F5E9)
        "B", "B+" -> Color(0xFFE3F2FD)
        "C", "C+" -> Color(0xFFFFF8E1)
        "D", "D+" -> Color(0xFFFFF3E0)
        "F", "W"  -> Color(0xFFFFEBEE)
        else       -> Color(0xFFF0F0F0)
    }
    val fg = when (grade) {
        "A", "A+" -> Green
        "B", "B+" -> Color(0xFF1565C0)
        "C", "C+" -> Color(0xFFF9A825)
        "D", "D+" -> Color(0xFFE65100)
        "F", "W"  -> Red
        else       -> Grey600
    }
    Surface(shape = RoundedCornerShape(4.dp), color = bg) {
        Text(
            text = grade,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun EmptyStateCard() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
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
                color = Grey400
            )
            Text(
                text = "Analyze a transcript to see it here",
                fontSize = 12.sp,
                color = Color(0xFFBDBDBD)
            )
        }
    }
}
