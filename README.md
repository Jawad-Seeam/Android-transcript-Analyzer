# NSU Transcript Analyzer - Android App

A fully functional Android application for analyzing North South University student transcripts. This app integrates seamlessly with the Project 2 Flask backend to provide comprehensive degree auditing and CGPA calculation.

## Features

### 🔐 Authentication
- **Google OAuth 2.0** - Secure login with NSU accounts using modern Android Credential Manager
- **Email Authentication** - Alternative sign-in with NSU email and name
- **Token-based Sessions** - Secure Bearer token management with automatic storage

### 📊 Transcript Analysis
- **4 Input Modes**:
  - **Manual Input**: Add courses one by one with dynamic form builder
  - **CSV Upload**: Paste CSV data in standard format
  - **PDF Upload**: Extract transcripts from PDF files (backend OCR)
  - **Image OCR**: Capture or upload photos for OCR processing (backend Tesseract)

### 📈 Analysis Results
- **CGPA Calculation**: Precise grade-point computation with quality points
- **Degree Audit**: Verify course completion against program requirements (CSE, BBA)
- **Eligibility Status**: Check graduation eligibility with detailed audit trail
- **Issue Detection**: Comprehensive reporting of missing/failed courses

### 📝 History Management
- **Run History**: View all past transcript analyses
- **Run Details**: Examine individual analysis results
- **Export Ready**: All data prepared for future export functionality

### 🎨 Modern UI/UX
- **Jetpack Compose**: Beautiful, responsive material design
- **Smooth Animations**: Fade-ins, expand/collapse cards, slide-ins for lists
- **Dark/Light Support**: Theme-aware design (ready for dark mode)
- **Mobile-Optimized**: Adaptive layouts for all screen sizes

## Project Structure

```
Project 3 Android APP/
├── build.gradle.kts                          # Build configuration
├── settings.gradle.kts                       # Project settings
├── gradle.properties                         # Gradle properties
│
├── src/main/
│   ├── AndroidManifest.xml                  # App manifest with permissions
│   ├── java/com/nsu/transcriptanalyzer/
│   │   ├── MainActivity.kt                  # Entry point with OAuth
│   │   │
│   │   ├── data/
│   │   │   ├── model/
│   │   │   │   └── Models.kt               # Data classes (User, Analysis, History)
│   │   │   ├── api/
│   │   │   │   ├── TranscriptApiService.kt # Retrofit API interface
│   │   │   │   └── RetrofitClient.kt       # HTTP client setup
│   │   │   ├── prefs/
│   │   │   │   └── PreferencesManager.kt   # DataStore for token storage
│   │   │   └── repository/
│   │   │       └── TranscriptRepository.kt # API abstraction layer
│   │   │
│   │   ├── ui/
│   │   │   ├── theme/
│   │   │   │   └── Theme.kt                # Material Design theme
│   │   │   ├── viewmodel/
│   │   │   │   ├── AuthViewModel.kt        # Authentication state
│   │   │   │   ├── AnalysisViewModel.kt    # Analysis form state
│   │   │   │   └── HistoryViewModel.kt     # History list state
│   │   │   ├── screen/
│   │   │   │   ├── LoginScreen.kt          # Authentication UI
│   │   │   │   ├── DashboardScreen.kt      # Main analysis UI
│   │   │   │   ├── ResultsScreen.kt        # Results display
│   │   │   │   └── HistoryScreen.kt        # History & details
│   │   │   ├── navigation/
│   │   │   │   └── NavGraph.kt             # Navigation setup
│   │   │   └── MainScreen.kt               # Root composable with bottom nav
│   │   │
│   │   └── ui/theme/
│   │       └── Color.kt                    # Color definitions
│   │
│   └── res/
│       ├── values/
│       │   └── strings.xml                 # App strings
│       └── drawable/                       # Icons (to be added)
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+ (Min API level 26)
- Kotlin 1.9+
- Gradle 8.0+

### 1. Clone/Setup Project
```bash
cd "D:\CSE 226\Project 3 Android APP"
```

### 2. Configure Google OAuth

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select existing
3. Enable Google Sign-In API
4. Create OAuth 2.0 credentials:
   - Application type: Android
   - Package name: `com.nsu.transcriptanalyzer`
   - SHA-1 fingerprint: (Get from Android Studio: Build > Generate Signed Bundle)
5. Copy the **Client ID**

### 3. Update Configuration Files

**Update `build.gradle.kts`:**
```kotlin
resValue("string", "google_client_id", "YOUR_CLIENT_ID.apps.googleusercontent.com")
resValue("string", "backend_url", "http://your-backend-url.com")
```

**Update `src/main/res/values/strings.xml`:**
```xml
<string name="backend_url">http://your-backend-url.com</string>
<string name="google_client_id">YOUR_CLIENT_ID.apps.googleusercontent.com</string>
```

### 4. Sync & Build

```bash
# In Android Studio terminal or:
./gradlew build
```

### 5. Run on Emulator/Device

```bash
./gradlew installDebug
```

## API Integration

### Backend Endpoints Used

The app communicates with the Project 2 Flask backend via REST API:

```
POST   /api/mobile/auth/google          # OAuth authentication
POST   /api/mobile/auth/email           # Email authentication
GET    /api/mobile/auth/me              # Get current user
POST   /api/mobile/analyze              # Analyze transcript
GET    /api/mobile/history              # Get analysis history
GET    /api/mobile/history/{run_id}     # Get specific run details
```

### Authentication Flow

1. User signs in via Google OAuth or Email
2. Backend returns Bearer token + User data
3. Token stored locally in DataStore (encrypted)
4. All API requests include `Authorization: Bearer {token}` header
5. Token auto-refreshed on expiry or logout clears it

### Request/Response Examples

**Analyze Transcript (Manual Mode):**
```json
POST /api/mobile/analyze
Authorization: Bearer {token}
Content-Type: application/json

{
  "input_method": "manual",
  "program": "CSE",
  "manual_text": "CSE115,3,A,Spring 2023\nCSE116,4,B+,Spring 2023",
  "waived": []
}

Response:
{
  "ok": true,
  "result": {
    "cgpa": 3.67,
    "earned_credits": 7,
    "required_credits": 130,
    "eligible": false,
    "issues": ["Missing core courses"],
    "audit": [...],
    "courses_analyzed": 2
  }
}
```

## Architecture

### MVVM Pattern
- **Model**: Data classes, API models
- **View**: Jetpack Compose UI screens
- **ViewModel**: State management with StateFlow
- **Repository**: API abstraction and data flow

### Data Flow
```
UI (Compose Screens)
    ↓
ViewModel (State & Logic)
    ↓
Repository (API + Cache)
    ↓
API Service (Retrofit)
    ↓
Flask Backend
```

### State Management
- **Compose State**: Local UI state with `remember`
- **ViewModel StateFlow**: App-level state
- **DataStore**: Persistent user preferences (token, settings)

## Features Deep Dive

### Manual Input Mode
- Dynamic course form with add/remove capability
- Real-time form validation
- Course code auto-uppercase
- Animated card expansion

### CSV Mode
- Paste CSV data directly
- Expected format: `Code,Credits,Grade,Semester`
- Backend validates and parses

### Results Display
- Animated CGPA card with color coding
- Expandable audit sections
- Status badges for each course
- Issue alerts with icons
- Waived courses highlighting

### History Screen
- Scrollable list of past analyses
- Quick stats per analysis (CGPA, credits, status)
- Tap to view detailed results
- Timestamp for each run
- Input method badge

## Error Handling

The app gracefully handles:
- Network timeouts (30s default)
- Invalid credentials
- Expired tokens (auto-logout)
- Server errors with user-friendly messages
- Malformed API responses
- Missing required fields

## Animations & Polish

- **Login Screen**: Smooth email input reveal/hide
- **Dashboard**: Card expand animations on input method selection
- **Results**: CGPA card fade-in on completion
- **History**: List item slide-in animations
- **Status Changes**: Smooth state transitions with Material transitions

## Security Considerations

✅ **Implemented:**
- HTTPS enforcement (cleartext disabled in production)
- Bearer token in Authorization headers
- Local token encryption via DataStore
- Automatic logout on token expiry
- NSU domain validation (@northsouth.edu)

⚠️ **Production Notes:**
- Use environment variables for backend URL
- Implement certificate pinning for production
- Enable ProGuard/R8 code shrinking
- Sign APK with production keystore

## Testing

### Manual Testing Checklist
- [ ] Google Sign-In flow
- [ ] Email authentication fallback
- [ ] Manual course entry with add/remove
- [ ] CSV paste and validation
- [ ] Analysis submission and response
- [ ] Results display with animations
- [ ] History loading and details view
- [ ] Logout functionality
- [ ] Network error handling
- [ ] Token expiry handling

### Debug Features
- **Logging**: HTTP request/response logging enabled (disable in production)
- **Error Messages**: Detailed error toast notifications
- **Status Indicators**: Loading spinners and progress indicators

## Future Enhancements

- [ ] PDF upload and parsing (client-side or server-side)
- [ ] Image OCR (client-side camera integration)
- [ ] Dark mode theme
- [ ] Offline caching of history
- [ ] Export to PDF/CSV
- [ ] Notification for analysis completion
- [ ] Biometric authentication (fingerprint/face)
- [ ] Multiple user accounts
- [ ] Course recommendations
- [ ] GPA prediction calculator

## Troubleshooting

### Google Sign-In Issues
1. Verify SHA-1 fingerprint matches in Google Cloud Console
2. Check package name: `com.nsu.transcriptanalyzer`
3. Ensure `REQUEST_ID_TOKEN` scope is requested

### Network Errors
1. Check backend URL in strings.xml/build.gradle
2. Ensure backend is running and accessible
3. Check firewall/proxy settings
4. Verify HTTPS certificate (if applicable)

### Build Issues
```bash
# Clean build
./gradlew clean build

# Update dependencies
./gradlew dependencies --refresh-dependencies

# Clear Android Studio cache
File > Invalidate Caches > Invalidate and Restart
```

## Dependencies

- **Kotlin**: 1.9.x
- **Jetpack Compose**: 2024.01.00
- **Retrofit**: 2.10.0
- **OkHttp**: 4.11.0
- **Google OAuth**: 20.7.0
- **Credentials**: 1.2.0
- **DataStore**: 1.0.0
- **Coroutines**: 1.7.3

## License

This project is part of CSE 226 (North South University).

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review backend logs (Project 2)
3. Check Logcat in Android Studio
4. Verify API configuration

---

**Happy analyzing! 📚📊**
