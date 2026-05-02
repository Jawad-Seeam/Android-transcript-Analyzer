# Quick Reference Guide

## 🚀 Quick Start (2 minutes)

```bash
# 1. Navigate to project
cd "D:\CSE 226\Project 3 Android APP"

# 2. Update backend URL in build.gradle.kts:
resValue("string", "backend_url", "http://10.0.2.2:5000")

# 3. Get Google OAuth credentials from Google Cloud Console
# Add Client ID to build.gradle.kts:
resValue("string", "google_client_id", "YOUR_CLIENT_ID.apps.googleusercontent.com")

# 4. Build & run
./gradlew build
./gradlew installDebug
```

## 📁 Key Files

| File | Purpose | Lines |
|------|---------|-------|
| `MainActivity.kt` | Entry point + OAuth setup | 80 |
| `data/model/Models.kt` | Data classes | 170 |
| `data/api/TranscriptApiService.kt` | API interface | 35 |
| `data/repository/TranscriptRepository.kt` | API abstraction | 140 |
| `ui/viewmodel/AuthViewModel.kt` | Auth state | 70 |
| `ui/viewmodel/AnalysisViewModel.kt` | Analysis state | 95 |
| `ui/viewmodel/HistoryViewModel.kt` | History state | 60 |
| `ui/screen/LoginScreen.kt` | Login UI | 180 |
| `ui/screen/DashboardScreen.kt` | Main UI | 450 |
| `ui/screen/ResultsScreen.kt` | Results UI | 620 |
| `ui/screen/HistoryScreen.kt` | History UI | 420 |

## 🔧 Configuration

### Backend URL
```kotlin
// File: build.gradle.kts (line ~20)
resValue("string", "backend_url", "http://10.0.2.2:5000")
```

### Google OAuth
```kotlin
// File: build.gradle.kts (line ~18)
resValue("string", "google_client_id", "YOUR_CLIENT_ID.apps.googleusercontent.com")
```

### Get SHA-1
```bash
./gradlew signingReport
# Look for SHA1 value under "debug"
```

## 🎯 Main Screens

| Screen | Purpose | File |
|--------|---------|------|
| Login | OAuth + Email auth | `LoginScreen.kt` |
| Dashboard | Add courses, select input method | `DashboardScreen.kt` |
| Results | Show CGPA, audit, issues | `ResultsScreen.kt` |
| History | List past analyses | `HistoryScreen.kt` |

## 🔌 API Endpoints

```
POST   /api/mobile/auth/google      → Authenticate with Google
POST   /api/mobile/auth/email       → Authenticate with email
GET    /api/mobile/auth/me          → Get current user
POST   /api/mobile/analyze          → Analyze transcript
GET    /api/mobile/history          → Get all analyses
GET    /api/mobile/history/{id}     → Get specific analysis
```

## 📊 Input Methods

| Method | Format | File |
|--------|--------|------|
| Manual | Form entries | `ManualInputSection` in `DashboardScreen.kt` |
| CSV | Paste text | `CsvInputSection` in `DashboardScreen.kt` |
| PDF | (Extension ready) | TODO |
| Image | (Extension ready) | TODO |

## 🎨 UI Components

```kotlin
// Reusable Components
LoginScreen(...)              // Login page
DashboardScreen(...)          // Main form
ResultsScreen(...)            // Results display
HistoryScreen(...)            // History list
InputMethodCard(...)          // Input method selector
CourseInputCard(...)          // Course form field
StatusBadge(...)              // Status display
```

## 💾 State Management

```kotlin
// ViewModel + StateFlow pattern
val uiState by viewModel.uiState.collectAsState()

// Update state
viewModel.setProgram("CSE")
viewModel.updateCourse(0, course)
viewModel.analyzeTranscript()
```

## 🔒 Authentication

```kotlin
// Google OAuth
authViewModel.authenticateWithGoogle(idToken)

// Email Auth
authViewModel.authenticateWithEmail(email, name)

// Logout
authViewModel.logout()

// Token stored in DataStore (encrypted)
preferencesManager.accessToken
```

## 🧪 Testing

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Install on device
./gradlew installDebug

# Run with logging
./gradlew runDebug
```

## 🐛 Debugging

```kotlin
// Check Logcat
View > Tool Windows > Logcat
Filter: "com.nsu.transcriptanalyzer"

// Log recompositions
LaunchedEffect(Unit) {
    Log.d("Recompose", "Screen recomposed")
}

// Log state changes
LaunchedEffect(uiState) {
    Log.d("State", "New: $uiState")
}
```

## 🚨 Common Issues

| Issue | Solution |
|-------|----------|
| Cannot connect to backend | Check URL in build.gradle.kts |
| Google Sign-In fails | Verify SHA-1 in Google Cloud Console |
| Build errors | Run `./gradlew clean build` |
| No internet | Check emulator network settings |
| Token expired | App auto-logs out, user must sign in again |

## 📱 Device/Emulator Settings

```
// For emulator to reach localhost (Project 2)
Backend URL: http://10.0.2.2:5000

// For physical device on same network
Backend URL: http://192.168.x.x:5000  // Replace x.x with your IP

// For production/cloud
Backend URL: https://yourdomain.com
```

## 🎯 Development Workflow

```
1. Modify code
2. Sync Gradle (Tools > Android > Sync Now)
3. Build (./gradlew build)
4. Run (./gradlew installDebug or click Run in Android Studio)
5. Test on emulator/device
6. Check Logcat for errors
7. Repeat
```

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| `README.md` | Full feature documentation |
| `SETUP.md` | Detailed setup instructions |
| `IMPLEMENTATION.md` | Architecture & code patterns |
| `PROJECT_SUMMARY.md` | This project overview |
| `QUICK_REFERENCE.md` | This quick guide |

## 🔗 Important Links

- [Android Developer Docs](https://developer.android.com)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [Google Cloud Console](https://console.cloud.google.com)

## ✅ Before Submitting

- [ ] Backend URL configured
- [ ] Google OAuth credentials set up
- [ ] App builds without errors
- [ ] App runs on emulator/device
- [ ] Can sign in with Google/Email
- [ ] Can analyze transcripts
- [ ] Results display correctly
- [ ] History loads past analyses
- [ ] No crashes or errors
- [ ] Documentation reviewed

## 🎓 Next Steps

1. **Immediate**: Configure and run the app
2. **Short-term**: Test all features with backend
3. **Medium-term**: Add PDF/Image OCR modes
4. **Long-term**: Deploy to Play Store, add more features

---

**Questions? Check README.md, SETUP.md, or IMPLEMENTATION.md**
