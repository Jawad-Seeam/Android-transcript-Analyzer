package com.nsu.transcriptanalyzer.ui.screen

import androidx.compose.animation.*
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
import com.nsu.transcriptanalyzer.data.model.AnalysisResult
import com.nsu.transcriptanalyzer.data.model.CourseAudit
import kotlin.math.round

@Composable
fun ResultsScreen(
    result: AnalysisResult,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedSectionId by remember { mutableStateOf("") }

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
                            text = "Analysis Complete",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            // latestRows.size = number of unique courses after retake handling
                            text = "${result.latestRows.size} courses analyzed",
                            fontSize = 12.sp,
                            color = Color(0xFFBDBDBD)
                        )
                    }
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // CGPA Card with Animation
        item {
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedCgpaCard(result.cgpa, result.eligible)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Credits Summary
        item {
            CreditsCard(result.earnedCredits, result.requiredCredits)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Eligibility Status
        item {
            EligibilityCard(result.eligible, result.issues)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Course Audit
        item {
            SectionCard(
                title = "Degree Audit",
                isExpanded = expandedSectionId == "audit",
                onExpandChange = {
                    expandedSectionId = if (it) "audit" else ""
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.courseAudit.forEach { course ->
                        CourseStatusCard(course)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Waived Courses
        if (result.waived.isNotEmpty()) {
            item {
                SectionCard(
                    title = "Waived Courses",
                    subtitle = "${result.waived.size} courses",
                    isExpanded = expandedSectionId == "waived",
                    onExpandChange = {
                        expandedSectionId = if (it) "waived" else ""
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.waived.forEach { course ->
                            WaivedCourseItem(course)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Issues
        if (result.issues.isNotEmpty()) {
            item {
                SectionCard(
                    title = "Issues & Alerts",
                    subtitle = "${result.issues.size} issues found",
                    isExpanded = expandedSectionId == "issues",
                    onExpandChange = {
                        expandedSectionId = if (it) "issues" else ""
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.issues.forEach { issue ->
                            IssueItem(issue)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color.Black
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Analysis")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AnimatedCgpaCard(cgpa: Double, isEligible: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEligible)
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
                    text = String.format("%.2f", cgpa),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isEligible) "✓ Eligible for Graduation" else "✗ Not Eligible",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CreditsCard(earned: Int, required: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            CreditStat("Earned", earned, Color(0xFF4CAF50))
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(80.dp),
                color = Color(0xFFE0E0E0)
            )
            CreditStat("Required", required, Color(0xFF1F3A93))
        }
    }
}

@Composable
private fun CreditStat(label: String, value: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF999999))
        Text(
            text = "$value",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text("credits", fontSize = 10.sp, color = Color(0xFF999999))
    }
}

@Composable
private fun EligibilityCard(isEligible: Boolean, issues: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEligible) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isEligible) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isEligible) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = if (isEligible) "Graduation Eligible" else "Graduation Not Eligible",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEligible) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                if (issues.isNotEmpty()) {
                    Text(
                        text = "${issues.size} issues to resolve",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    isExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange(!isExpanded) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF999999)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    content()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CourseStatusCard(course: CourseAudit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    // course.course = course code (e.g. "CSE115")
                    text = course.course,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F3A93)
                )
                Text(
                    // course.category = requirement category
                    text = course.category,
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
                if (course.details.isNotBlank()) {
                    Text(
                        text = course.details,
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusBadge(course.status)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
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

@Composable
private fun WaivedCourseItem(course: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE3F2FD))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF1F3A93),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = course,
            fontSize = 13.sp,
            color = Color(0xFF1F3A93),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun IssueItem(issue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFFEBEE))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = Color(0xFFC62828),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = issue,
            fontSize = 13.sp,
            color = Color(0xFFC62828)
        )
    }
}
