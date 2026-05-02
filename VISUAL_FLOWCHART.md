# Visual Setup Flowchart and Diagrams

## Complete Setup Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    ANDROID APP SETUP PROCESS                            │
└─────────────────────────────────────────────────────────────────────────┘

                          START HERE
                             │
                             ▼
                ┌──────────────────────────┐
                │  STEP 1: Backend URL     │
                │  ✓ Edit build.gradle     │
                │  ✓ Set backend_url       │
                │  ✓ Sync Gradle           │
                └──────────────────────────┘
                             │
                             ▼
                ┌──────────────────────────┐
                │  STEP 2: Google OAuth    │
                │  ✓ Get SHA-1 (CLI)       │
                │  ✓ Create OAuth in GCC   │
                │  ✓ Get Client ID         │
                │  ✓ Add to build.gradle   │
                └──────────────────────────┘
                             │
                             ▼
                ┌──────────────────────────┐
                │  STEP 3: Android Project │
                │  ✓ Open in Android S.    │
                │  ✓ Sync Gradle           │
                │  ✓ Download deps (5min)  │
                │  ✓ No errors             │
                └──────────────────────────┘
                             │
                             ▼
                ┌──────────────────────────┐
                │  STEP 4: Build & Run     │
                │  ✓ Start Flask backend   │
                │  ✓ Start emulator        │
                │  ✓ Click Run button      │
                │  ✓ App launches          │
                └──────────────────────────┘
                             │
                             ▼
                ┌──────────────────────────┐
                │  ✅ APP IS WORKING!      │
                │  You can now:            │
                │  • Sign in with Google   │
                │  • Analyze transcripts   │
                │  • View history          │
                │  • Test all features     │
                └──────────────────────────┘
```

---

## Step 1: Backend URL Configuration

```
┌─────────────────────────────────────────────────────────────┐
│                    STEP 1 DETAILED FLOW                      │
└─────────────────────────────────────────────────────────────┘

SCENARIO 1: Android Emulator (Most Common)
┌──────────────────────────┐
│ Your Computer            │
│ ┌────────────────────┐   │
│ │ Flask Backend      │   │
│ │ localhost:5000     │   │
│ └────────────────────┘   │
│          ▲               │
│          │ maps to       │
│          │               │
│ ┌────────────────────┐   │
│ │ Android Emulator   │   │
│ │ 10.0.2.2:5000      │   │
│ └────────────────────┘   │
└──────────────────────────┘

ACTION: Edit build.gradle.kts
resValue("string", "backend_url", "http://10.0.2.2:5000")


SCENARIO 2: Physical Device (Same WiFi)
┌──────────────────────────────────────────┐
│ Your Computer (WiFi)                     │
│ IP: 192.168.1.100:5000                  │
│                                          │
│ ┌────────────────────┐                  │
│ │ Flask Backend      │ ──────WiFi─────► │
│ │ localhost:5000     │                  │ Your Phone
│ └────────────────────┘                  │ Android App
└──────────────────────────────────────────┘

ACTION: Edit build.gradle.kts
resValue("string", "backend_url", "http://192.168.1.100:5000")
         (replace 192.168.1.100 with YOUR IP from ipconfig)


SCENARIO 3: Production/Cloud
┌──────────────────┐
│ Your Computer    │ ───Internet──► 🌐 Cloud Server
│ (irrelevant)     │                (render.com, heroku, etc)
└──────────────────┘

ACTION: Edit build.gradle.kts
resValue("string", "backend_url", "https://your-app.render.com")
```

---

## Step 2: Google OAuth Setup

```
┌─────────────────────────────────────────────────────────────┐
│              STEP 2: GOOGLE OAUTH DETAILED FLOW              │
└─────────────────────────────────────────────────────────────┘

PART A: Get SHA-1 Fingerprint
┌─────────────────────────────┐
│  Android Studio             │
│  ┌───────────────────────┐  │
│  │ Terminal              │  │
│  │ ./gradlew             │  │
│  │ signingReport         │  │
│  └───────────────────────┘  │
│          │                  │
│          ▼ (wait 30-60s)    │
│  ┌───────────────────────┐  │
│  │ Output: sha1: AB:CD:  │  │
│  │ EF:12:34:56:...      │  │
│  └───────────────────────┘  │
│          │                  │
│          ▼ (copy this)      │
│  Clipboard: SHA-1 value     │
└─────────────────────────────┘


PART B: Create OAuth in Google Cloud Console
┌──────────────────────────────────────────┐
│  Google Cloud Console                    │
│  https://console.cloud.google.com        │
│                                          │
│  1. Create Project "NSU Transcript"      │
│     ┌──────────────────────────────────┐ │
│     │ NSU Transcript Analyzer          │ │
│     │ [Create]                         │ │
│     └──────────────────────────────────┘ │
│                                          │
│  2. Enable "Google Sign-In API"          │
│     ┌──────────────────────────────────┐ │
│     │ Search: Google Sign-In           │ │
│     │ [Enable]                         │ │
│     └──────────────────────────────────┘ │
│                                          │
│  3. Create OAuth Credentials             │
│     ┌──────────────────────────────────┐ │
│     │ + Create Credentials             │ │
│     │ Application type: Android        │ │
│     │ Package: com.nsu.transcriptana..│ │
│     │ SHA-1: [paste from Part A]       │ │
│     │ [Create]                         │ │
│     └──────────────────────────────────┘ │
│                                          │
│  4. Copy Client ID                       │
│     ┌──────────────────────────────────┐ │
│     │ 1234567890-                      │ │
│     │ abcdefg.apps.googleusercontent. │ │
│     │ com                              │ │
│     └──────────────────────────────────┘ │
└──────────────────────────────────────────┘


PART C: Add Client ID to Android App
┌──────────────────────────────────┐
│  build.gradle.kts                │
│                                  │
│  Before:                         │
│  resValue("string",              │
│    "google_client_id",           │
│    "YOUR_GOOGLE_CLIENT_ID")      │
│                                  │
│  After:                          │
│  resValue("string",              │
│    "google_client_id",           │
│    "1234567890-abcdefg.         │
│     apps.googleusercontent.com") │
│                                  │
│  Sync Gradle                     │
│  ✓ Configuration complete!       │
└──────────────────────────────────┘
```

---

## Step 3: Android Project Setup

```
┌─────────────────────────────────────────────────────────────┐
│           STEP 3: ANDROID PROJECT SETUP FLOW                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ 1. Open Project in Android Studio       │
│                                         │
│    File → Open                          │
│    Select: D:\CSE 226\Project 3...      │
│    │                                    │
│    ▼ (Android Studio analyzes)          │
│    ⏳ Wait 1-2 minutes...               │
└─────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ 2. Gradle Sync                          │
│                                         │
│    ⏳ Syncing Gradle (in progress...)    │
│    • Gradle sync finished ✓             │
│                                         │
│    If you see errors:                   │
│    → Check build.gradle.kts             │
│    → Check Client ID and Backend URL    │
│    → File → Sync Now                    │
└─────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ 3. Download Dependencies                │
│                                         │
│    ⏳ Downloading libs (3-5 minutes)    │
│    • Kotlin                             │
│    • Jetpack Compose                    │
│    • Retrofit                           │
│    • Google Auth                        │
│    • 20+ other libraries                │
│                                         │
│    This is automatic - just wait!       │
└─────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ 4. Setup Android Emulator               │
│                                         │
│    Tools → Device Manager               │
│    ┌─────────────────────────────────┐  │
│    │ Create Virtual Device           │  │
│    │ Select: Pixel 4a                │  │
│    │ Next → API 34 → Next            │  │
│    │ Name: Pixel4a_API34             │  │
│    │ Finish                          │  │
│    └─────────────────────────────────┘  │
│                                         │
│    Now you have emulator ready!         │
└─────────────────────────────────────────┘
             │
             ▼
        ✅ READY FOR STEP 4
```

---

## Step 4: Build and Run

```
┌─────────────────────────────────────────────────────────────┐
│              STEP 4: BUILD AND RUN FLOW                     │
└─────────────────────────────────────────────────────────────┘

PREPARATION
┌────────────────────────────────────────┐
│ Your Computer (Terminal 1)              │
│ ┌──────────────────────────────────┐   │
│ │ cd D:\CSE 226\Project 2 web app  │   │
│ │ python app.py                    │   │
│ │                                  │   │
│ │ ✓ Running on localhost:5000 ✓    │   │
│ │ KEEP THIS TERMINAL OPEN!         │   │
│ └──────────────────────────────────┘   │
└────────────────────────────────────────┘
             │
             ▼ (separately)
┌────────────────────────────────────────┐
│ Android Studio                          │
│ ┌──────────────────────────────────┐   │
│ │ Tools → Device Manager           │   │
│ │ Click Play (▶️) on Pixel4a_API34 │   │
│ │ Wait 30-60 seconds...            │   │
│ │ ✓ Emulator boots up              │   │
│ └──────────────────────────────────┘   │
└────────────────────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ Android Studio (build & run)            │
│ ┌──────────────────────────────────┐   │
│ │ Click Green Run Button (▶️)       │   │
│ │ OR: Shift + F10                  │   │
│ │ Select Deployment Target:        │   │
│ │  Pixel4a_API34                   │   │
│ │ [OK]                             │   │
│ └──────────────────────────────────┘   │
│ ⏳ Building... (30-60 seconds)         │
│ ⏳ Installing...                       │
│ ⏳ Launching...                        │
│ ✓ App appears on emulator!            │
└────────────────────────────────────────┘
             │
             ▼
        🎉 SUCCESS! 🎉
    Login Screen appears!
    
┌────────────────────────────────────────┐
│ What You Should See                     │
├────────────────────────────────────────┤
│                                        │
│   NSU Transcript Analyzer              │
│                                        │
│   [Sign in with Google]                │
│   [Sign in with Email]                 │
│                                        │
└────────────────────────────────────────┘
```

---

## File Changes Checklist

### File 1: build.gradle.kts

**Change #1: Backend URL (Line ~20)**
```kotlin
BEFORE:
resValue("string", "backend_url", "http://your-backend-url.com")

AFTER:
resValue("string", "backend_url", "http://10.0.2.2:5000")
```

**Change #2: Google Client ID (Line ~18)**
```kotlin
BEFORE:
resValue("string", "google_client_id", "YOUR_GOOGLE_CLIENT_ID")

AFTER:
resValue("string", "google_client_id", "1234567890-abcdefg.apps.googleusercontent.com")
```

**No other files need to be changed!**

---

## Command Reference

### Getting SHA-1 Fingerprint
```bash
cd D:\CSE 226\Project 3 Android APP
./gradlew signingReport

# Look for: sha1: AB:CD:EF:...
```

### Building the App
```bash
cd D:\CSE 226\Project 3 Android APP
./gradlew build
```

### Running the App
```bash
cd D:\CSE 226\Project 3 Android APP
./gradlew installDebug
```

### Cleaning the Project
```bash
cd D:\CSE 226\Project 3 Android APP
./gradlew clean
./gradlew build
```

### Starting Flask Backend
```bash
cd "D:\CSE 226\Project 2 web app"
python app.py
```

### Getting Your Machine IP
```bash
# Windows
ipconfig
# Look for IPv4 Address

# Mac/Linux
ifconfig
# Look for inet address
```

---

## Verification Checklist

### After Step 1: Backend URL
- [ ] build.gradle.kts has been edited
- [ ] Backend URL is set correctly (10.0.2.2:5000 or your IP)
- [ ] File is saved
- [ ] Gradle synced successfully

### After Step 2: Google OAuth
- [ ] SHA-1 fingerprint obtained
- [ ] Google Cloud project created
- [ ] OAuth credentials created
- [ ] Client ID obtained
- [ ] Client ID added to build.gradle.kts
- [ ] Gradle synced successfully

### After Step 3: Android Project
- [ ] Project opened in Android Studio
- [ ] Gradle sync completed
- [ ] No error messages
- [ ] Dependencies downloaded (3-5 minutes)
- [ ] Emulator created and running

### After Step 4: Build & Run
- [ ] Flask backend is running (localhost:5000)
- [ ] Emulator is started and running
- [ ] Build completed successfully
- [ ] App installed on emulator
- [ ] App launched and showing login screen
- [ ] Can tap buttons on screen

---

## Success Criteria

### ✅ Setup is SUCCESSFUL if:

1. **You see the Login Screen**
   - Title: "NSU Transcript Analyzer"
   - Two buttons visible: "Sign in with Google" and "Sign in with Email"

2. **You can sign in**
   - Click "Sign in with Google"
   - Choose your NSU account
   - Successfully logs in

3. **You see the Dashboard**
   - Program selector (CSE/BBA)
   - Input method tabs (Manual, CSV, PDF, Image)
   - Course input form or CSV text area

4. **You can analyze a transcript**
   - Add a course (e.g., CSE115, 3, A)
   - Click "Analyze Transcript"
   - See results with CGPA and audit

### ❌ Setup FAILED if:

1. App crashes on launch
   - Check: build.gradle.kts syntax
   - Check: Backend URL configuration

2. Cannot sign in
   - Check: Backend is running (python app.py)
   - Check: Google Client ID is correct
   - Check: Internet connection

3. Analyze fails
   - Check: Backend URL uses 10.0.2.2:5000 for emulator
   - Check: Backend terminal shows API requests

4. No results displayed
   - Check: Flask backend responses in terminal
   - Check: Logcat for error messages

---

**Ready to start? Begin with STEP 1!** 🚀
