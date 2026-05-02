# Complete Step-by-Step Setup Guide for Android App

## STEP 1: Configure Backend URL

### What This Does
This tells your Android app where your Flask backend is running so it can send API requests.

### Step 1.1: Understand Backend URLs

Different URLs for different scenarios:

| Scenario | URL | Usage |
|----------|-----|-------|
| Emulator (Android Studio) | `http://10.0.2.2:5000` | Testing on emulator |
| Physical Device (same WiFi) | `http://192.168.x.x:5000` | Testing on real phone |
| Production/Cloud | `https://your-domain.com` | Production server |

**Why 10.0.2.2 for emulator?** 
- Android emulator runs in a virtual machine
- `10.0.2.2` is a special alias that points to your computer's localhost
- From emulator's perspective, your computer is at `10.0.2.2:5000`

### Step 1.2: Find Your Machine's IP (for physical device)

If you want to test on a physical phone connected to same WiFi:

**On Windows:**
1. Open Command Prompt (press `Win + R`, type `cmd`, press Enter)
2. Type: `ipconfig`
3. Press Enter
4. Look for "IPv4 Address" - it should be something like `192.168.1.100`
5. Copy this number

Example output:
```
Ethernet adapter:
    IPv4 Address . . . . . . . . . . . : 192.168.1.100
```

**On Mac/Linux:**
1. Open Terminal
2. Type: `ifconfig`
3. Look for `inet` address (not starting with 127)
4. Usually looks like `192.168.x.x` or `10.0.0.x`

### Step 1.3: Open the build.gradle.kts File

1. Navigate to your project:
   ```
   D:\CSE 226\Project 3 Android APP
   ```

2. Open the file: `build.gradle.kts`

3. Find this section (around line 18-20):
   ```kotlin
   defaultConfig {
       applicationId = "com.nsu.transcriptanalyzer"
       minSdk = 26
       targetSdk = 34
       versionCode = 1
       versionName = "1.0.0"

       testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

       // Google OAuth configuration
       resValue("string", "google_client_id", "YOUR_GOOGLE_CLIENT_ID")
       resValue("string", "backend_url", "http://your-backend-url.com")  // ← THIS LINE
   }
   ```

### Step 1.4: Update the Backend URL

Replace `"http://your-backend-url.com"` with the correct URL based on your scenario:

**For Android Emulator (Most Common):**
```kotlin
resValue("string", "backend_url", "http://10.0.2.2:5000")
```

**For Physical Device on Same WiFi:**
```kotlin
resValue("string", "backend_url", "http://192.168.1.100:5000")
// Replace 192.168.1.100 with YOUR IP from Step 1.2
```

**For Production/Cloud Server:**
```kotlin
resValue("string", "backend_url", "https://nsu-app.render.com")
// Or whatever your domain is
```

### Step 1.5: Verify Your Flask Backend is Ready

Before proceeding, make sure your Flask app is running:

1. Open Command Prompt/Terminal
2. Navigate to your Flask project:
   ```bash
   cd "D:\CSE 226\Project 2 web app"
   ```

3. Start the Flask app:
   ```bash
   python app.py
   ```

4. You should see output like:
   ```
   * Running on http://127.0.0.1:5000
   * Press CTRL+C to quit
   ```

5. **Keep this terminal open** - your backend must be running for the app to work

### Step 1.6: Verify the Configuration

After updating the build.gradle.kts file:

1. In Android Studio, go to: `File > Sync Now`
2. This reloads the gradle configuration
3. Wait for sync to complete (you'll see "Sync successful" message)
4. If you see errors, check that:
   - The URL is in quotes
   - The syntax is correct (no typos)
   - The file was saved

✅ **STEP 1 COMPLETE** - Backend URL is configured!

---

## STEP 2: Setup Google OAuth 2.0

### What This Does
This allows users to sign in with their Google account (NSU email is powered by Google).

### Step 2.1: Get Your SHA-1 Fingerprint

The SHA-1 fingerprint is a unique identifier for your app that Google uses for security.

**Method: Using Android Studio**

1. Open Android Studio
2. Open the project: `D:\CSE 226\Project 3 Android APP`
3. At the bottom of Android Studio, find the **Terminal** tab
4. Click on it to open the terminal
5. Type this command:
   ```bash
   ./gradlew signingReport
   ```
6. Press Enter
7. Wait for it to complete (may take 30-60 seconds)
8. Look for output that shows:
   ```
   sha1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
   ```

**Copy the SHA1 value** (the part after `sha1:`)

Example of what to copy:
```
AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
```

### Step 2.2: Go to Google Cloud Console

1. Open your browser and go to: https://console.cloud.google.com
2. Sign in with your Google account (use a personal Google account, not NSU)
3. If you see a welcome screen, click "Skip this for now"

### Step 2.3: Create or Select a Project

**Option A: If you already have a project:**
1. At the top, click the project dropdown
2. Select your existing project
3. Skip to Step 2.4

**Option B: Create a new project:**
1. At the top, click "Select a project"
2. Click "NEW PROJECT"
3. Fill in the form:
   - **Project name:** `NSU Transcript Analyzer`
   - Leave other fields as default
4. Click "CREATE"
5. Wait for the project to be created (this takes 30 seconds)

### Step 2.4: Enable Google Sign-In API

1. In Google Cloud Console, search for "Google Sign-In API" in the search box
2. Click on "Google Identity Services API" or "Google Sign-In"
3. Click the blue "Enable" button
4. Wait for it to enable (takes a few seconds)

### Step 2.5: Create OAuth 2.0 Credentials

1. In the left sidebar, click **"Credentials"**
2. At the top, click **"+ Create Credentials"**
3. Select **"OAuth 2.0 Client IDs"**
4. You might see a warning "To create an OAuth client ID, you must first set up your OAuth consent screen"
   - If you see this, click "Configure Consent Screen"
   - Select "External"
   - Click "Create"
   - Fill in the form:
     - **App name:** `NSU Transcript Analyzer`
     - **User support email:** Your email
     - **Developer contact information:** Your email
   - Click "Save and Continue"
   - Skip the scopes section by clicking "Save and Continue"
   - Skip the test users section by clicking "Save and Continue"
   - Click "Back to Dashboard"

5. Now try again: **"+ Create Credentials" > "OAuth 2.0 Client IDs"**

6. In the dialog:
   - **Application type:** Select "Android"
   - **Name:** `NSU Transcript Analyzer`
   - **Package name:** `com.nsu.transcriptanalyzer`
   - **SHA-1 certificate fingerprint:** Paste the SHA-1 from Step 2.1
   - Click "Create"

7. You'll see a dialog with your **Client ID** (something like `1234567890-abcdefg.apps.googleusercontent.com`)
   - **Copy this entire Client ID**

### Step 2.6: Update Android App with Client ID

1. Open the file: `build.gradle.kts`
2. Find the line (around line 18):
   ```kotlin
   resValue("string", "google_client_id", "YOUR_GOOGLE_CLIENT_ID")
   ```
3. Replace `YOUR_GOOGLE_CLIENT_ID` with your actual Client ID from Step 2.5
4. Example:
   ```kotlin
   resValue("string", "google_client_id", "1234567890-abcdefg.apps.googleusercontent.com")
   ```

### Step 2.7: Sync Gradle

1. Save the file
2. In Android Studio: `File > Sync Now`
3. Wait for sync to complete

✅ **STEP 2 COMPLETE** - Google OAuth is configured!

---

## STEP 3: Initialize Android Project in Android Studio

### What This Does
This prepares the Android project to build and run on an emulator or device.

### Step 3.1: Open the Project in Android Studio

1. Open Android Studio
2. Click **"File"** > **"Open"**
3. Navigate to: `D:\CSE 226\Project 3 Android APP`
4. Click the folder to select it
5. Click **"Open"**
6. Android Studio will analyze the project (this takes 1-2 minutes)
7. Wait for it to complete

### Step 3.2: Wait for Gradle Sync

After opening, you'll see a message at the bottom:
```
Gradle sync in progress...
```

Wait for it to complete. You should see:
```
Gradle sync finished
```

**If you see errors:**
1. Click **"File" > "Sync Now"** again
2. If it still fails, check that:
   - `build.gradle.kts` has correct Client ID
   - `build.gradle.kts` has correct Backend URL
   - No syntax errors in the file

### Step 3.3: Download Dependencies

Android Studio will automatically download all dependencies (libraries needed for the app).

This will take 3-5 minutes on first sync. You'll see a progress bar at the bottom.

**What's being downloaded:**
- Kotlin libraries
- Jetpack Compose
- Retrofit (networking)
- Google Auth libraries
- And 20+ other libraries

Let it complete - don't interrupt it.

### Step 3.4: Check for Build Errors

After sync completes:

1. Look at the bottom of Android Studio
2. Click on **"Problems"** tab (if there's one)
3. Look for any red error messages
4. If you see errors:
   - Read the error message
   - Fix the issue in `build.gradle.kts`
   - Run `Sync Now` again

Common errors and fixes:
| Error | Fix |
|-------|-----|
| "Unknown variable" | Check Client ID syntax in build.gradle.kts |
| "Dependency not found" | Run Sync Now again |
| "Plugin not found" | Update Android Studio to latest version |

### Step 3.5: Setup Android Emulator

You need an Android Emulator (virtual phone) to test the app.

**Method: Using Android Studio Emulator Manager**

1. In Android Studio, go to: **"Tools" > "Device Manager"**
2. Click **"Create Virtual Device"** (if you see one already, skip to Step 3.6)
3. Select **"Pixel 4a"** (popular phone size)
4. Click **"Next"**
5. Select **"API 30"** or **"API 34"** (preferably 34)
6. Click **"Next"**
7. Configure:
   - **AVD Name:** `Pixel4a_API34`
   - Leave other settings as default
8. Click **"Finish"**
9. The emulator is now created

### Step 3.6: Alternative - Use Physical Device

If you have an Android phone:

1. **Enable Developer Mode:**
   - Open Settings on your phone
   - Search for "Build number"
   - Tap "Build number" 7 times
   - You'll see "Developer mode enabled"

2. **Enable USB Debugging:**
   - Go back to Settings
   - Search for "USB debugging"
   - Turn it ON
   - Click "Allow" when you see the prompt

3. **Connect to Computer:**
   - Plug your phone into your computer with USB cable
   - You should see a prompt on your phone "Allow USB debugging?"
   - Tap "Allow"

Now you can run the app on your real phone!

✅ **STEP 3 COMPLETE** - Android project is ready!

---

## STEP 4: Build and Run the App

### What This Does
This compiles your Kotlin code into an Android app and runs it on an emulator or device.

### Step 4.1: Start Your Backend

Before running the app, make sure Flask backend is running:

1. Open Command Prompt/Terminal (new window from Step 1.5)
2. Navigate to:
   ```bash
   cd "D:\CSE 226\Project 2 web app"
   ```
3. Start Flask:
   ```bash
   python app.py
   ```
4. Confirm you see:
   ```
   * Running on http://127.0.0.1:5000
   ```

✅ Backend must be running!

### Step 4.2: Start the Android Emulator (or connect device)

**Option A: Android Emulator**
1. In Android Studio: **"Tools" > "Device Manager"**
2. Find your emulator (e.g., "Pixel4a_API34")
3. Click the play button (▶️) to start it
4. Wait 30-60 seconds for the emulator to boot up
5. You should see a virtual phone screen

**Option B: Physical Device**
1. Plug your phone into your computer
2. It should appear in Android Studio automatically

### Step 4.3: Build the App

1. In Android Studio, go to: **"Build" > "Make Project"**
2. This compiles your Kotlin code
3. Wait for it to complete (you'll see "Build completed successfully")
4. If you see errors, check:
   - `build.gradle.kts` has correct syntax
   - All dependencies are downloaded
   - Google Client ID is correct

### Step 4.4: Run the App

**Method: Click the Run Button**

1. At the top of Android Studio, find the green **"Run"** button (▶️)
2. Or use keyboard shortcut: **Shift + F10**
3. A dialog appears asking "Select deployment target"
4. Select your emulator or device
5. Click **"OK"**

Android Studio will:
1. Compile the app
2. Install it on the emulator/device
3. Launch the app automatically

This takes 30-60 seconds first time.

### Step 4.5: View the App

After running, you should see your Android app appear on the emulator/device screen!

**First screen:** Login page with:
- NSU Transcript Analyzer title
- Google Sign In button
- Email login option

### Step 4.6: Test Google Sign-In

1. Tap **"Sign in with Google"** button
2. Select your NSU email account
3. You should be redirected to the main Dashboard screen
4. If you see an error about token, check:
   - Backend is running
   - Backend URL in build.gradle.kts is correct (10.0.2.2:5000 for emulator)

### Step 4.7: Test the App Features

**Try Manual Input:**
1. On Dashboard screen, you should see "Select Program" section
2. Select "CSE" or "BBA"
3. Select "Manual" input method
4. Enter a course:
   - Code: `CSE115`
   - Credits: `3`
   - Grade: `A`
5. Click "Add Course" to add more
6. Click "Analyze Transcript" button
7. Wait for results to load
8. You should see CGPA and audit results!

**Try CSV Input:**
1. Select "CSV" input method
2. Paste this text:
   ```
   CSE115,3,A,Spring 2023
   CSE116,4,B+,Spring 2023
   ```
3. Click "Analyze Transcript"
4. Wait for results

**Try History:**
1. After analyzing, click the "History" tab at the bottom
2. You should see your past analyses listed
3. Click on one to see details

✅ **STEP 4 COMPLETE** - App is running!

---

## Troubleshooting During Setup

### Issue: Gradle Sync Failed

**Problem:**
```
Gradle sync failed
```

**Solution:**
1. Click **"Try Again"** button
2. If still fails:
   - Close Android Studio
   - Delete folder: `D:\CSE 226\Project 3 Android APP\.gradle`
   - Reopen Android Studio
   - Try Sync again

### Issue: "Cannot connect to backend"

**Problem:**
App starts but shows error when signing in

**Solution:**
1. Check Flask backend is running:
   ```bash
   python app.py
   # Should show: Running on http://127.0.0.1:5000
   ```
2. Check backend URL in build.gradle.kts:
   - Should be: `http://10.0.2.2:5000` for emulator
   - Or your machine IP for device
3. Rebuild: **"Build" > "Clean Project"** then **"Build" > "Make Project"**

### Issue: Google Sign-In shows blank screen

**Problem:**
After clicking "Sign in with Google", nothing happens

**Solution:**
1. Check Google Client ID in build.gradle.kts:
   - Should be: `xxx.apps.googleusercontent.com`
   - Not: `YOUR_GOOGLE_CLIENT_ID`
2. Check SHA-1 fingerprint matches in Google Cloud Console
3. Rebuild and run again

### Issue: "NSU email domain required"

**Problem:**
Error: "Only North South University accounts are allowed"

**Solution:**
Use an email ending with `@northsouth.edu`
- Or use Email authentication instead of Google
- Email authentication doesn't require NSU domain for testing

### Issue: Emulator is slow

**Problem:**
Emulator takes very long to boot or is laggy

**Solution:**
1. Close other apps on your computer
2. Try smaller emulator (Pixel 3 instead of Pixel 4)
3. Or use physical device instead

### Issue: "Gradle build failed"

**Problem:**
```
BUILD FAILED
```

**Solution:**
1. Click **"View Details"** to see error message
2. Common fixes:
   - `File > Sync Now`
   - `Build > Clean Project`
   - `Build > Make Project`
3. If still fails:
   - Check internet connection (downloading libraries)
   - Restart Android Studio

---

## Summary: Complete Workflow

Here's what should happen:

```
Step 1: Backend URL ✅
  - Edit build.gradle.kts
  - Set backend_url to http://10.0.2.2:5000
  - Sync Gradle

Step 2: Google OAuth ✅
  - Get SHA-1 fingerprint
  - Create OAuth credentials in Google Cloud
  - Get Client ID
  - Add Client ID to build.gradle.kts
  - Sync Gradle

Step 3: Android Project ✅
  - Open project in Android Studio
  - Wait for Gradle sync
  - Download dependencies (3-5 minutes)
  - No errors should appear

Step 4: Build & Run ✅
  - Start Flask backend
  - Start Android Emulator
  - Click Run button
  - App appears on emulator
  - Test login and features

Done! 🎉
```

---

## Next Steps After Setup Works

1. **Explore the App:**
   - Try all input methods
   - Check history
   - Test logout and login again

2. **Modify Backend URL:**
   - Test with physical device using your machine IP
   - Test with production server URL

3. **Extend Features:**
   - Add PDF upload mode
   - Add Image OCR mode
   - Add dark mode
   - See IMPLEMENTATION.md for examples

4. **Prepare for Submission:**
   - Document your changes
   - Test on multiple devices
   - Check for errors
   - Review code quality

---

**You're all set! If you get stuck on any step, check the Troubleshooting section.** 🚀
