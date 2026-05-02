package com.nsu.transcriptanalyzer.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.nsu.transcriptanalyzer.ui.viewmodel.AnalysisUiState
import com.nsu.transcriptanalyzer.ui.viewmodel.AnalysisViewModel
import com.nsu.transcriptanalyzer.ui.viewmodel.Course
import java.io.File

// ── Color palette ─────────────────────────────────────────────────────────────
private val NavyDeep  = Color(0xFF0D1B4F)
private val NavyMid   = Color(0xFF1F3A93)
private val Accent    = Color(0xFF4F8EF7)
private val GreenOk   = Color(0xFF26C97C)
private val Surface1  = Color(0xFFF0F4FF)
private val CardBg    = Color(0xFFFFFFFF)
private val TextPri   = Color(0xFF0D1B4F)
private val TextSec   = Color(0xFF6B7A99)
private val ErrBg     = Color(0xFFFFEEEE)
private val ErrText   = Color(0xFFC62828)

@Composable
fun DashboardScreen(
    analysisViewModel: AnalysisViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by analysisViewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.showResults && uiState.analysisResult != null) {
        ResultsScreen(result = uiState.analysisResult!!, onBackClick = { analysisViewModel.resetAnalysis() })
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Surface1)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header gradient ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(NavyDeep, NavyMid)))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column {
                Text("Transcript Analyzer", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Select your program & upload method", fontSize = 13.sp, color = Color(0xFFB0C4FF), modifier = Modifier.padding(top = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Program selector ─────────────────────────────────────────────────
        SectionCard(title = "Program") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("CSE", "BBA").forEach { prog ->
                    val selected = uiState.selectedProgram == prog
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) NavyMid else Color(0xFFEEF2FF))
                            .clickable { analysisViewModel.setProgram(prog) }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(prog, fontWeight = FontWeight.Bold, color = if (selected) Color.White else NavyMid, fontSize = 15.sp)
                    }
                }
            }
        }

        // ── Input method selector ────────────────────────────────────────────
        SectionCard(title = "Input Method") {
            val methods = listOf(
                Triple("manual", Icons.Default.Edit,         "Enter courses manually"),
                Triple("csv",    Icons.Default.Description,  "Upload CSV file"),
                Triple("pdf",    Icons.Default.PictureAsPdf, "Upload PDF transcript"),
                Triple("image",  Icons.Default.Photo,        "Photo / Camera scan")
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                methods.forEach { (method, icon, desc) ->
                    val selected = uiState.selectedInputMethod == method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) NavyMid else Color(0xFFF5F8FF))
                            .border(
                                width = if (selected) 0.dp else 1.dp,
                                color = Color(0xFFD0D9F0),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { analysisViewModel.setInputMethod(method) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(icon, null, tint = if (selected) Color.White else Accent, modifier = Modifier.size(22.dp))
                        Column {
                            Text(method.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White else TextPri, fontSize = 14.sp)
                            Text(desc, color = if (selected) Color(0xFFB0C4FF) else TextSec, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // ── Mode-specific input panels ───────────────────────────────────────
        AnimatedContent(
            targetState = uiState.selectedInputMethod,
            transitionSpec = { fadeIn() + slideInVertically { it / 3 } togetherWith fadeOut() },
            label = "input_panel"
        ) { method ->
            when (method) {
                "manual" -> ManualInputSection(analysisViewModel, uiState)
                "csv"    -> CsvPickerSection(analysisViewModel, uiState)
                "pdf"    -> PdfPickerSection(analysisViewModel, uiState)
                "image"  -> ImagePickerSection(analysisViewModel, uiState)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Analyze button ───────────────────────────────────────────────────
        Button(
            onClick = { analysisViewModel.analyzeTranscript(context) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(54.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenOk, disabledContainerColor = Color(0xFFB2DFDB))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Analyzing…", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Analyze Transcript", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        // ── Error banner ─────────────────────────────────────────────────────
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = ErrBg),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, null, tint = ErrText, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(uiState.errorMessage ?: "", color = ErrText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { analysisViewModel.clearError() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = ErrText)
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ─── Section wrapper card ─────────────────────────────────────────────────────
@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = Accent, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}

// ─── CSV Picker ───────────────────────────────────────────────────────────────
@Composable
private fun CsvPickerSection(vm: AnalysisViewModel, state: AnalysisUiState) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val name = resolveFileName(context, it)
            vm.setSelectedFile(it, name)
        }
    }
    SectionCard("CSV File") {
        FilePickerRow(
            icon       = Icons.Default.Description,
            label      = state.selectedFileName ?: "No file selected",
            buttonText = if (state.selectedFileName != null) "Change File" else "Browse CSV",
            tint       = if (state.selectedFileName != null) GreenOk else Accent
        ) { launcher.launch(arrayOf("text/csv", "text/comma-separated-values")) }

        // OCR extract from CSV to get manual_text
        if (state.selectedFileName != null) {
            Spacer(Modifier.height(12.dp))
            OcrExtractButton(
                isLoading   = state.isOcrLoading,
                isDone      = state.ocrExtractedText != null,
                buttonText  = "Extract from CSV",
                onExtract   = { vm.extractOcr(context) }
            )
            OcrStatusRow(state)
        }
    }
}

// ─── PDF Picker ───────────────────────────────────────────────────────────────
@Composable
private fun PdfPickerSection(vm: AnalysisViewModel, state: AnalysisUiState) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val name = resolveFileName(context, it)
            vm.setSelectedFile(it, name)
        }
    }
    SectionCard("PDF Transcript") {
        FilePickerRow(
            icon       = Icons.Default.PictureAsPdf,
            label      = state.selectedFileName ?: "No file selected",
            buttonText = if (state.selectedFileName != null) "Change File" else "Browse PDF",
            tint       = if (state.selectedFileName != null) GreenOk else Accent
        ) { launcher.launch(arrayOf("application/pdf")) }

        // Step 1: Extract text via OCR before analyzing
        if (state.selectedFileName != null) {
            Spacer(Modifier.height(12.dp))
            OcrExtractButton(
                isLoading  = state.isOcrLoading,
                isDone     = state.ocrExtractedText != null,
                buttonText = "Extract Text (OCR)",
                onExtract  = { vm.extractOcr(context) }
            )
            OcrStatusRow(state)
        }
    }
}

// ─── Image Picker (Gallery + Camera) ─────────────────────────────────────────
@Composable
private fun ImagePickerSection(vm: AnalysisViewModel, state: AnalysisUiState) {
    val context = LocalContext.current

    // Temp URI for camera capture
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { vm.setSelectedImage(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { vm.setSelectedImage(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val tempFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    SectionCard("Image / Photo") {
        // Thumbnail preview
        state.selectedImageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(12.dp))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Camera button
            OutlinedButton(
                onClick = {
                    val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        val tempFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyMid)
            ) {
                Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Camera", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            // Gallery button
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Gallery", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        // Image extract
        if (state.selectedImageUri != null) {
            Spacer(Modifier.height(8.dp))
            OcrExtractButton(
                isLoading  = state.isOcrLoading,
                isDone     = state.ocrExtractedText != null,
                buttonText = "Extract Text (OCR)",
                onExtract  = { vm.extractOcr(context) }
            )
            OcrStatusRow(state)
        }

        if (state.selectedImageUri != null && state.ocrExtractedText == null && !state.isOcrLoading) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = GreenOk, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Image selected. Tap 'Extract Text' above.", color = GreenOk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── Shared file picker row ───────────────────────────────────────────────────
@Composable
private fun FilePickerRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    buttonText: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = if (label == "No file selected") TextSec else TextPri,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyMid)
        ) {
            Text(buttonText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Manual Input Section ─────────────────────────────────────────────────────
@Composable
private fun ManualInputSection(vm: AnalysisViewModel, state: AnalysisUiState) {
    SectionCard("Course List") {
        state.courses.forEachIndexed { index, course ->
            CourseInputRow(
                course         = course,
                showRemove     = state.courses.size > 1,
                onCourseChange = { vm.updateCourse(index, it) },
                onRemove       = { vm.removeCourse(index) }
            )
            if (index < state.courses.lastIndex) Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { vm.addCourse() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add Course", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CourseInputRow(
    course: Course,
    showRemove: Boolean,
    onCourseChange: (Course) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF5F8FF),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = course.code,
                    onValueChange = { onCourseChange(course.copy(code = it.uppercase())) },
                    label = { Text("Code", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1.4f),
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = course.credits,
                    onValueChange = { onCourseChange(course.copy(credits = it)) },
                    label = { Text("Cr", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(0.65f),
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = course.grade,
                    onValueChange = { onCourseChange(course.copy(grade = it.uppercase())) },
                    label = { Text("Grade (A/B+/F)", fontSize = 10.sp) },
                    placeholder = { Text("A", fontSize = 10.sp, color = TextSec) },
                    singleLine = true,
                    modifier = Modifier.weight(0.85f),
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(8.dp)
                )
                if (showRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.DeleteOutline, null, tint = ErrText, modifier = Modifier.size(18.dp))
                    }
                }
            }
            // Semester field – required by backend (format: "Spring 2024")
            OutlinedTextField(
                value = course.semester,
                onValueChange = { onCourseChange(course.copy(semester = it)) },
                label = { Text("Semester (e.g. Spring 2024)", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall,
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text("Spring 2024", fontSize = 10.sp, color = TextSec) }
            )
        }
    }
}

// ─── Utility ──────────────────────────────────────────────────────────────────
private fun resolveFileName(context: android.content.Context, uri: Uri): String? = try {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (col != -1 && cursor.moveToFirst()) cursor.getString(col) else null
    }
} catch (e: Exception) { null }

// ─── OCR Extract composables ──────────────────────────────────────────────────

@Composable
private fun OcrExtractButton(
    isLoading: Boolean,
    isDone: Boolean,
    buttonText: String,
    onExtract: () -> Unit
) {
    Button(
        onClick = onExtract,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDone) GreenOk else Accent
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text("Extracting…", fontWeight = FontWeight.SemiBold)
        } else if (isDone) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Re-extract", fontWeight = FontWeight.SemiBold)
        } else {
            Icon(Icons.Default.FindInPage, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(buttonText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun OcrStatusRow(state: AnalysisUiState) {
    if (state.ocrExtractedText != null) {
        Spacer(Modifier.height(8.dp))
        val lines = state.ocrExtractedText.lines().count { it.isNotBlank() }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = GreenOk, modifier = Modifier.size(16.dp))
            Column {
                Text(
                    "Extracted $lines course rows · confidence: ${state.ocrConfidence ?: "?"}",
                    color = GreenOk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
                state.ocrWarning?.let {
                    Text(it, color = Color(0xFFF57C00), fontSize = 11.sp)
                }
            }
        }
    }
}

