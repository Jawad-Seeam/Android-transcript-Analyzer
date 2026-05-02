# Android App Implementation Notes

## Architecture Overview

This Android app follows the **MVVM (Model-View-ViewModel)** architecture pattern with clean separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                    Jetpack Compose UI                    │
│  (LoginScreen, DashboardScreen, ResultsScreen, etc.)    │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│              ViewModel Layer (StateFlow)                 │
│  (AuthViewModel, AnalysisViewModel, HistoryViewModel)   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│           Repository (API Abstraction)                   │
│         TranscriptRepository                             │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│         API Service (Retrofit + Network)                 │
│  (TranscriptApiService, RetrofitClient)                 │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│    Local Storage (DataStore)                             │
│         PreferencesManager                               │
└─────────────────────────────────────────────────────────┘
                            ↓
                    Flask Backend
```

## State Management Strategy

### 1. Local UI State (Compose)
Use `remember { mutableStateOf(...) }` for temporary UI state:
```kotlin
var showEmailInput by remember { mutableStateOf(false) }
var expandedSectionId by remember { mutableStateOf("") }
```

### 2. ViewModel State (StateFlow)
Use `StateFlow` in ViewModels for app-level state:
```kotlin
private val _uiState = MutableStateFlow(AnalysisUiState())
val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

// Collect in UI:
val uiState by viewModel.uiState.collectAsState()
```

### 3. Persistent State (DataStore)
Use DataStore for user preferences and tokens:
```kotlin
preferencesManager.saveAccessToken(token)
val token: Flow<String?> = preferencesManager.accessToken
```

## Adding New Features

### Example: Adding PDF Upload Mode

**1. Update AnalysisViewModel:**
```kotlin
fun setPdfContent(content: String) {
    _uiState.value = _uiState.value.copy(pdfContent = content)
}
```

**2. Add to DashboardScreen:**
```kotlin
"pdf" -> PdfInputSection(analysisViewModel, uiState)
```

**3. Create PdfInputSection Composable:**
```kotlin
@Composable
private fun PdfInputSection(
    analysisViewModel: AnalysisViewModel,
    uiState: AnalysisUiState
) {
    // File picker integration
    val launcher = rememberLauncherForActivityResult(...) { uri ->
        // Read PDF content
        analysisViewModel.setPdfContent(pdfText)
    }
    
    Button(onClick = { launcher.launch(...) }) {
        Text("Select PDF")
    }
}
```

**4. Update analyzeTranscript:**
```kotlin
"pdf" -> {
    repository.analyzeTranscript(
        inputMethod = "pdf",
        program = state.selectedProgram,
        csvText = state.pdfContent  // Or add new field
    )
}
```

### Example: Adding Image OCR Mode

**1. Update AnalysisViewModel:**
```kotlin
fun setImageContent(imageUri: String) {
    _uiState.value = _uiState.value.copy(imageUri = imageUri)
}
```

**2. Create ImageInputSection:**
```kotlin
@Composable
private fun ImageInputSection(...) {
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Get image from intent
            analysisViewModel.setImageContent(imageUri)
        }
    }
    
    Button(onClick = { cameraLauncher.launch(...) }) {
        Text("Take Photo")
    }
}
```

## Animations Explained

### Card Expansion Animation
```kotlin
AnimatedVisibility(
    visible = expandedSectionId == "audit",
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut()
) {
    // Content animates in/out
}
```

### Slide-in Animation for Lists
```kotlin
items(
    items = uiState.runs,
    key = { it.runId }
) { run ->
    HistoryRunCard(
        run = run,
        modifier = Modifier.animateItemPlacement()  // Slide animation
    )
}
```

### CGPA Card Animation
```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    shape = RoundedCornerShape(16.dp)
) {
    // Card automatically animates when state changes
}
```

## Error Handling Patterns

### API Error Handling
```kotlin
when (result) {
    is ApiResult.Success -> {
        // Update UI with result
        _uiState.value = state.copy(data = result.data)
    }
    is ApiResult.Error -> {
        // Show error message
        _uiState.value = state.copy(
            errorMessage = result.message,
            isLoading = false
        )
    }
    is ApiResult.Loading -> {
        _uiState.value = state.copy(isLoading = true)
    }
}
```

### Network Error Messages
```kotlin
try {
    val response = apiService.analyzeTranscript(...)
    // Process response
} catch (e: TimeoutException) {
    ApiResult.Error("Request timed out. Please check your connection.")
} catch (e: SocketTimeoutException) {
    ApiResult.Error("Connection lost. Please try again.")
} catch (e: UnknownHostException) {
    ApiResult.Error("Cannot reach server. Check your internet.")
} catch (e: Exception) {
    ApiResult.Error(e.message ?: "Unknown error occurred")
}
```

## Testing Strategies

### Unit Testing ViewModels
```kotlin
@Test
fun testAnalysisStateUpdate() {
    val viewModel = AnalysisViewModel(repository)
    viewModel.setProgram("BBA")
    
    val state = viewModel.uiState.value
    assertEquals("BBA", state.selectedProgram)
}
```

### Integration Testing API
```kotlin
@Test
fun testAnalyzeTranscript() = runTest {
    val result = repository.analyzeTranscript(
        inputMethod = "manual",
        program = "CSE",
        manualText = "CSE115,3,A,Spring 2023"
    )
    
    assertTrue(result is ApiResult.Success)
    assertEquals(4.0, (result as ApiResult.Success).data.cgpa)
}
```

### UI Testing Composables
```kotlin
@Test
fun testDashboardScreenLoads() {
    composeTestRule.setContent {
        DashboardScreen(analysisViewModel = mockViewModel)
    }
    
    composeTestRule.onNodeWithText("Transcript Analysis").assertExists()
    composeTestRule.onNodeWithText("Select Program").assertExists()
}
```

## Performance Optimization

### 1. Lazy Loading Lists
```kotlin
LazyColumn {
    items(1000) { index ->
        // Only composables visible on screen are composed
        ExpensiveItem(index)
    }
}
```

### 2. Key Stability in Lists
```kotlin
items(
    items = uiState.runs,
    key = { it.runId }  // Stable key for recomposition
) { run ->
    HistoryRunCard(run)
}
```

### 3. Remember with Key
```kotlin
remember(uiState.selectedProgram) {
    // Recompute only when program changes
    calculateRequirements(uiState.selectedProgram)
}
```

### 4. Image Optimization
```kotlin
// Use Coil for lazy loading images
AsyncImage(
    model = avatarUrl,
    contentDescription = "Avatar",
    modifier = Modifier
        .size(48.dp)
        .clip(CircleShape),
    contentScale = ContentScale.Crop
)
```

## Debugging Tips

### 1. Recomposition Tracking
```kotlin
// Add to any Composable to see recompositions:
LaunchedEffect(Unit) {
    Log.d("Recompose", "DashboardScreen recomposed")
}
```

### 2. State Value Logging
```kotlin
val uiState by analysisViewModel.uiState.collectAsState()
LaunchedEffect(uiState) {
    Log.d("State", "New state: $uiState")
}
```

### 3. Network Logging
Check Android Studio Logcat, filter by:
```
Package: com.nsu.transcriptanalyzer
Level: Verbose
Search: "http" or "OkHttp"
```

### 4. Compose Preview Testing
```kotlin
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(
        analysisViewModel = AnalysisViewModel(
            MockTranscriptRepository()
        )
    )
}
```

## Migration Path for New Android Studio Versions

- **Jetpack Compose BOM**: Update in build.gradle regularly
- **Material3**: Already using latest, follows Material Design 3
- **Gradle**: Keep Gradle plugin updated (currently 8.x)
- **Kotlin**: Update kotlin-gradle-plugin regularly

## Security Best Practices

### 1. Token Storage
```kotlin
// DO: Use encrypted DataStore
preferencesManager.saveAccessToken(token)

// DON'T: Use SharedPreferences
// DON'T: Save in plain text
```

### 2. API Calls
```kotlin
// DO: Add token to Authorization header
@Header("Authorization") bearerToken: String

// DON'T: Pass token as query parameter
// DON'T: Send over HTTP (use HTTPS)
```

### 3. Logout Cleanup
```kotlin
suspend fun logout() {
    preferencesManager.clearAll()  // Clear token
    googleSignInClient.signOut()   // Clear OAuth session
}
```

## Useful Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Android Architecture Components](https://developer.android.com/topic/architecture)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [Material Design 3](https://m3.material.io/)

---

**Happy coding! 🚀**
