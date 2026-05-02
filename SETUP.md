# Android App Configuration Guide

## Step-by-Step Setup Instructions

### Step 1: Backend URL Configuration

The app needs to connect to your Flask backend. Update the following files:

**File: `build.gradle.kts` (Line ~20)**
```kotlin
resValue("string", "backend_url", "http://10.0.2.2:5000")  // For emulator
// OR
resValue("string", "backend_url", "http://your-ip:5000")   // For physical device
// OR
resValue("string", "backend_url", "https://your-domain.com")  // For production
```

**File: `src/main/res/values/strings.xml`**
```xml
<string name="backend_url">http://10.0.2.2:5000</string>
```

### Step 2: Google OAuth Configuration

#### 2a. Get SHA-1 Fingerprint

In Android Studio:
1. Open Terminal (View > Tool Windows > Terminal)
2. Run:
```bash
./gradlew signingReport
```

3. Look for `SHA1` under `debug` configuration
4. Copy the SHA1 value (e.g., `AB:CD:EF:12:34:56:...`)

#### 2b. Create OAuth Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create/Select a project
3. Enable APIs:
   - Search for "Google Sign-In API"
   - Click "Enable"
4. Create OAuth 2.0 Credentials:
   - Go to "Credentials" > "Create Credentials" > "OAuth 2.0 Client IDs"
   - Choose "Android"
   - Fill in:
     - **Package name**: `com.nsu.transcriptanalyzer`
     - **SHA-1 certificate fingerprint**: (paste from Step 2a)
   - Click "Create"
5. Copy the **Client ID** (format: `xxx.apps.googleusercontent.com`)

#### 2c. Update App Configuration

**File: `build.gradle.kts` (Line ~18)**
```kotlin
resValue("string", "google_client_id", "YOUR_CLIENT_ID.apps.googleusercontent.com")
```

**File: `src/main/res/values/strings.xml`**
```xml
<string name="google_client_id">YOUR_CLIENT_ID.apps.googleusercontent.com</string>
```

### Step 3: Backend Configuration

Ensure your Flask backend (Project 2) is properly configured:

**Flask `.env` file should have:**
```
GOOGLE_CLIENT_ID=YOUR_CLIENT_ID.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=YOUR_CLIENT_SECRET
MOBILE_TOKEN_SECRET=your-secret-key-for-tokens
DATABASE_URL=postgresql://user:password@localhost/transcripts  # or sqlite
SECRET_KEY=your-flask-secret-key
```

### Step 4: Test the Connection

1. Start your Flask backend:
```bash
cd "D:\CSE 226\Project 2 web app"
python app.py
```

2. Open Android Studio and run the app:
```bash
./gradlew installDebug
```

3. Test the health check:
   - In the app, if you see the login screen, the connection is likely working
   - Backend will handle authentication token exchange

## Environment-Specific Configuration

### Development (Emulator)
```kotlin
// build.gradle.kts
resValue("string", "backend_url", "http://10.0.2.2:5000")
```

**Note**: `10.0.2.2` is the special IP to reach localhost from Android emulator.

### Development (Physical Device on Same Network)
```kotlin
// build.gradle.kts
resValue("string", "backend_url", "http://192.168.x.x:5000")  // Replace with your machine IP
```

To find your machine IP:
- Windows: Run `ipconfig` in Command Prompt, look for IPv4 Address
- Mac/Linux: Run `ifconfig` in terminal

### Production (Cloud Deployment)
```kotlin
// build.gradle.kts
resValue("string", "backend_url", "https://your-app.render.com")
resValue("string", "backend_url", "https://your-app.herokuapp.com")
// Or your custom domain
```

## API Endpoints Available

The app uses these endpoints from Flask backend:

```
Authentication:
POST /api/mobile/auth/google              # Google OAuth token exchange
POST /api/mobile/auth/email               # Email-based auth
GET  /api/mobile/auth/me                  # Get current user

Analysis:
POST /api/mobile/analyze                  # Analyze transcript

History:
GET  /api/mobile/history                  # Get all past analyses
GET  /api/mobile/history/{run_id}         # Get specific analysis

Health:
GET  /api/health                          # API health check
```

## Common Issues & Solutions

### Issue: Cannot connect to backend
**Solution**:
1. Verify backend is running: `python app.py`
2. Check URL in strings.xml matches your backend
3. For emulator: Use `10.0.2.2` instead of `localhost`
4. For device: Use `http://` not `https://` in development

### Issue: Google Sign-In fails
**Solution**:
1. Verify SHA-1 fingerprint matches Google Cloud Console
2. Check package name: `com.nsu.transcriptanalyzer`
3. Verify Client ID is correct in strings.xml
4. Wait 5-10 minutes after creating OAuth credentials

### Issue: "Only North South University accounts are allowed"
**Solution**:
1. Use an account ending with `@northsouth.edu`
2. For testing, use the email authentication method as fallback

### Issue: App crashes on launch
**Solution**:
1. Check Logcat (View > Tool Windows > Logcat)
2. Look for errors related to:
   - Missing strings.xml values
   - Network connection
   - JSON parsing errors
3. Ensure all dependencies are properly synced

## Advanced Configuration

### Custom HTTP Timeout
Edit `src/main/java/com/nsu/transcriptanalyzer/data/api/RetrofitClient.kt`:
```kotlin
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)    // Change from 30
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()
```

### Enable/Disable HTTP Logging
In production, disable HTTP logging. Edit `RetrofitClient.kt`:
```kotlin
// Development (enabled):
level = HttpLoggingInterceptor.Level.BODY

// Production (disabled):
level = HttpLoggingInterceptor.Level.NONE
```

### Certificate Pinning (Production)
For production, implement certificate pinning:
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("yourdomain.com", "sha256/...")
    .build()

val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

## Running the App

### Option 1: Android Studio GUI
1. Open Android Studio
2. File > Open > Select "Project 3 Android APP"
3. Click "Run" button (▶️)
4. Select target (emulator or device)

### Option 2: Command Line
```bash
cd "D:\CSE 226\Project 3 Android APP"

# Build
./gradlew build

# Install and run on connected device/emulator
./gradlew installDebug

# Or run directly (builds + installs + starts):
./gradlew runDebug
```

## Debugging Tips

1. **Check Logcat for errors**:
   - View > Tool Windows > Logcat
   - Filter by "transcriptanalyzer"

2. **Enable verbose logging**:
   - In Logcat, change filter level to "Verbose"

3. **Test API directly** (before UI):
   - Use Postman or curl to test backend endpoints
   - Verify backend returns expected JSON

4. **Check network in dev tools**:
   - Android Studio > Profiler > Network
   - Monitor API calls in real-time

## Next Steps

1. ✅ Configure backend URL
2. ✅ Setup Google OAuth
3. ✅ Run the app
4. ✅ Test login flow
5. ✅ Test transcript analysis
6. ✅ Check history functionality

**Ready to analyze! 🚀**
