# STEP 2: Google OAuth Setup - Complete Detailed Guide

## Overview
This step enables Google Sign-In for your Android app. This is a critical step that allows users to sign in with their NSU Google accounts.

**Total Time: 15-20 minutes**

---

## Part A: Understanding SHA-1 Fingerprint

### What is SHA-1 Fingerprint?

SHA-1 is a unique identifier (like a digital fingerprint) for your Android app. Think of it like:
- **Real Fingerprint**: Unique to each person
- **SHA-1 Fingerprint**: Unique to your app

Google uses this to verify that API requests are coming from YOUR legitimate app, not a fake one.

### Why Do We Need It?

```
Security Chain:
┌─────────────────────────────────────────────┐
│ Your Android App                            │
│ (unique SHA-1 fingerprint)                  │
│         │                                   │
│         ▼ (send to Google)                  │
│ Google Cloud Console                        │
│ (verifies SHA-1 matches)                    │
│         │                                   │
│         ▼ (if valid)                        │
│ Allows Google Sign-In                       │
│ Returns authentication token                │
└─────────────────────────────────────────────┘
```

### Important Notes:
- **Different for Debug and Release**: Your debug app has one SHA-1, your release app has another
- **We're using Debug**: For testing/development
- **Must be exact**: Even one character different won't work

---

## Part A.1: Getting SHA-1 Fingerprint - Method 1 (Using Android Studio Terminal) ⭐ RECOMMENDED

This is the easiest method.

### Step 1: Open Android Studio

1. On your computer, open **Android Studio**
2. You should see the welcome screen or a project open
3. If no project is open, open: `D:\CSE 226\Project 3 Android APP`

**Screenshot mental model:**
```
┌─────────────────────────────────────────────┐
│  Android Studio                             │
│  ┌─────────────────────────────────────┐   │
│  │ File  Edit  View  Build  Run  Tools │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  [Project view on left]  [Code editor]     │
│                                             │
└─────────────────────────────────────────────┘
```

### Step 2: Open the Terminal

1. Look at the **bottom of Android Studio**
2. You should see tabs like: "Problems", "Terminal", "Logcat", etc.
3. Click on the **"Terminal"** tab
4. A command prompt window opens at the bottom

**What you should see:**
```
Terminal
─────────────────────────────────────────
D:\CSE 226\Project 3 Android APP>
```

The cursor (blinking line) is ready for you to type.

### Step 3: Type the Command

In the terminal, type this exact command:
```bash
./gradlew signingReport
```

**Important:**
- Copy exactly as shown (including the dot and slash at the beginning)
- Don't add anything extra
- Press Enter after typing

**What it looks like:**
```
Terminal
─────────────────────────────────────────
D:\CSE 226\Project 3 Android APP> ./gradlew signingReport
```

### Step 4: Wait for Results

After pressing Enter, the terminal will start working. You'll see:
```
> Task :app:signingReport
Variant: debug
Config: debug
Store: C:\Users\[YourUsername]\.android\debug.keystore
Alias: AndroidDebugKey
...
```

**Wait time**: 30-60 seconds

You might see lots of text scrolling. **DON'T interrupt it!**

### Step 5: Find the SHA-1 Value

Look for text that says:
```
sha1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
```

**Example of what to look for:**
```
Terminal
─────────────────────────────────────────
> Task :app:signingReport
Variant: debug
Config: debug
Store: C:\Users\YourName\.android\debug.keystore
Alias: AndroidDebugKey
MD5: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99
SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12  ← THIS ONE!
SHA-256: ABC123...

BUILD SUCCESSFUL in 45s
```

### Step 6: Copy the SHA-1 Value

The SHA-1 value looks like:
```
AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
```

**How to copy:**
1. Use your mouse to highlight/select the SHA-1 value
2. Or triple-click on the line to select entire line
3. Right-click and select "Copy"
4. Or use Ctrl+C

**Important:** 
- Copy the ENTIRE value including colons (:)
- Don't copy "sha1: " part, just the value itself

### Step 7: Save It Somewhere

Paste it into a **text file** or **notepad** for safekeeping:

1. Open Notepad (search for "Notepad" on your computer)
2. Paste the SHA-1 value
3. Save the file as: `SHA1_BACKUP.txt`

**Example content:**
```
My Android App SHA-1 Fingerprint:
AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
```

✅ **You now have your SHA-1 fingerprint!**

---

## Part A.2: Getting SHA-1 - Method 2 (Using Command Prompt) - Alternative

If the terminal in Android Studio doesn't work, try this method:

### Step 1: Open Command Prompt

1. On Windows, press: **Windows Key + R**
2. Type: `cmd`
3. Press Enter
4. A black terminal window opens

```
C:\Users\YourName>
```

### Step 2: Navigate to Project Folder

Type this command:
```bash
cd "D:\CSE 226\Project 3 Android APP"
```

Press Enter. You should see:
```
D:\CSE 226\Project 3 Android APP>
```

### Step 3: Run the Command

Type:
```bash
gradlew signingReport
```

Press Enter and wait 30-60 seconds.

### Step 4: Find SHA-1

Look for the line with `sha1:` and copy that value (not including "sha1: " prefix).

---

## Part B: Creating Google Cloud Project

### Step 1: Go to Google Cloud Console

1. Open your **web browser** (Chrome, Firefox, Edge, etc.)
2. Go to: https://console.cloud.google.com
3. Sign in with your **personal Google account** (not your NSU account)

**Note:** Use a personal account like:
- yourname@gmail.com
- yourpersonal@outlook.com

**NOT:**
- yourname@northsouth.edu (don't use NSU account here)

### Step 2: Create a New Project

**If you see the welcome screen:**
1. Look for a button like **"Select a project"** or **"New Project"**
2. Click on it

**If you see a dropdown at the top:**
1. At the very top of Google Cloud Console, you'll see a dropdown showing a project name
2. Click on that dropdown
3. You'll see a list of projects
4. At the top of that list, look for **"NEW PROJECT"** button
5. Click it

**Creating the project:**
1. A form appears titled "New Project"
2. In the **"Project name"** field, type:
   ```
   NSU Transcript Analyzer
   ```
3. Leave other fields as default
4. Click the **"CREATE"** button

**Wait time:** 30-60 seconds

You'll see a notification:
```
✓ Project created successfully
Project ID: nsu-transcript-analyzer-xxxxx
```

✅ **Project created!**

---

## Part C: Enable Google Sign-In API

### Step 1: Make Sure You're in Your Project

At the top of Google Cloud Console, you should see:
```
Google Cloud
[Your Project Name] ▼
```

If you don't, click the dropdown and select "NSU Transcript Analyzer"

### Step 2: Search for the API

In the search box at the top (where it says "Search products and resources"), type:
```
Google Sign-In
```

Or search for:
```
Identity Services
```

### Step 3: Enable the API

1. You'll see results appear
2. Click on **"Google Identity Services API"** or **"Google Sign-In"**
3. A details page opens
4. Look for a blue **"ENABLE"** button
5. Click it

**Wait time:** A few seconds

You'll see:
```
✓ API enabled
```

Now the Google Sign-In API is enabled for your project!

---

## Part D: Create OAuth 2.0 Credentials

This is where you get your **Client ID** that the app will use.

### Step 1: Go to Credentials

In Google Cloud Console:
1. Look at the left sidebar menu
2. Find **"Credentials"** (or "APIs & Services" > "Credentials")
3. Click on it

You'll see a page with options like:
- Create Credentials
- API Keys
- OAuth 2.0 Client IDs
- Service Accounts

### Step 2: Create New Credentials

1. At the top of the page, click **"+ Create Credentials"**
2. A dropdown menu appears with options:
   - API key
   - OAuth 2.0 Client IDs ← Select this one
   - Service Account key
3. Click on **"OAuth 2.0 Client IDs"**

**Note:** If you see a message saying "To create an OAuth 2.0 Client ID, you must first set up the OAuth consent screen", follow this:

1. Click **"Configure Consent Screen"** button
2. Select **"External"** (if asked to choose)
3. Click **"Create"**
4. Fill in the consent screen form:
   - **App name**: `NSU Transcript Analyzer`
   - **User support email**: Your email address
   - **Developer contact information**: Your email address
5. Click **"Save and Continue"**
6. Click **"Save and Continue"** again (skip scopes section)
7. Click **"Save and Continue"** once more (skip test users section)
8. Click **"Back to Dashboard"**

Then try creating credentials again.

### Step 3: Fill in the OAuth Credentials Form

A form appears asking for:

**Application type:**
- Look for a dropdown
- Select **"Android"**

**Name:**
- Type: `NSU Transcript Analyzer`

**Package name:**
- Type: `com.nsu.transcriptanalyzer`
- (This must be EXACT - don't change it)

**SHA-1 certificate fingerprint:**
- Paste the SHA-1 value you copied earlier
- Should look like: `AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12`

### Step 4: Create the Credentials

1. Check that all fields are filled correctly
2. Click the **"CREATE"** button
3. A dialog appears showing your credentials:

```
OAuth 2.0 Client Created

Client ID:
1234567890-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com

Client Secret:
GOCSPX-xxxxxxxxxxxxxxxxxxxxxx
```

**Important:** Copy the **Client ID** only (the long string ending in `.apps.googleusercontent.com`)

---

## Part E: Copy Client ID

### Step 1: Find Your Client ID

You should see a popup or notification showing your Client ID. It looks like:

```
1234567890-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com
```

### Step 2: Copy It

1. Click on the Client ID to select it
2. Or use your mouse to select the entire ID
3. Right-click and copy
4. Or use Ctrl+C

**Make sure you copy the ENTIRE thing**, including:
- The numbers at the beginning
- The dash
- The letters in the middle
- `.apps.googleusercontent.com` at the end

### Step 3: Save It

Paste it into your notepad file for safekeeping:

```
My Android App OAuth Credentials:

SHA-1 Fingerprint:
AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12

Client ID:
1234567890-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com
```

✅ **You now have your Client ID!**

---

## Part F: Add Client ID to Android App

Now you need to tell your Android app about this Client ID.

### Step 1: Open build.gradle.kts File

1. On your computer, navigate to: `D:\CSE 226\Project 3 Android APP`
2. Find the file: `build.gradle.kts`
3. Open it with a text editor (like Notepad or Android Studio)

### Step 2: Find the Line to Edit

Search for this line (around line 18):

```kotlin
resValue("string", "google_client_id", "YOUR_GOOGLE_CLIENT_ID")
```

**Location in file:**
```kotlin
defaultConfig {
    applicationId = "com.nsu.transcriptanalyzer"
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "1.0.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    
    resValue("string", "google_client_id", "YOUR_GOOGLE_CLIENT_ID")  ← THIS LINE
    resValue("string", "backend_url", "http://your-backend-url.com")
}
```

### Step 3: Replace the Placeholder

Replace `YOUR_GOOGLE_CLIENT_ID` with your actual Client ID from Part E.

**BEFORE:**
```kotlin
resValue("string", "google_client_id", "YOUR_GOOGLE_CLIENT_ID")
```

**AFTER:**
```kotlin
resValue("string", "google_client_id", "1234567890-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com")
```

**Important:**
- Keep the quotes (")
- Paste the ENTIRE Client ID
- Don't include "Client ID:" label
- Just the actual ID value

### Step 4: Save the File

1. Save the file (Ctrl+S or File > Save)
2. Make sure it saved successfully

---

## Part G: Verify and Sync Gradle

### Step 1: Verify the Change

Open the file again and confirm it looks like:

```kotlin
resValue("string", "google_client_id", "1234567890-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com")
```

Not like:
```kotlin
resValue("string", "google_client_id", "YOUR_GOOGLE_CLIENT_ID")  ← WRONG!
```

### Step 2: Sync Gradle in Android Studio

1. Open the project in Android Studio
2. Go to: **File** > **Sync Now**
3. Or you might see a notification in Android Studio saying "Gradle files changed"
   - Click **"Sync Now"** button in that notification

**Wait time:** 1-2 minutes

You should see:
```
Gradle sync finished
```

If you see errors, check:
1. Your Client ID is completely correct
2. No extra spaces or characters
3. File was saved

✅ **Step 2 is complete!**

---

## Complete Step 2 Checklist

- [ ] Opened Android Studio
- [ ] Opened Terminal in Android Studio
- [ ] Ran: `./gradlew signingReport`
- [ ] Found SHA-1 value (looks like: AB:CD:EF:12:...)
- [ ] Copied SHA-1 value to notepad
- [ ] Went to Google Cloud Console (console.cloud.google.com)
- [ ] Created new project: "NSU Transcript Analyzer"
- [ ] Enabled Google Sign-In API
- [ ] Created OAuth 2.0 credentials (Android type)
- [ ] Filled in:
  - [ ] Package name: `com.nsu.transcriptanalyzer`
  - [ ] SHA-1 fingerprint: Pasted from terminal
- [ ] Copied Client ID to notepad
- [ ] Opened build.gradle.kts file
- [ ] Found line: `resValue("string", "google_client_id", "YOUR_GOOGLE_CLIENT_ID")`
- [ ] Replaced with your actual Client ID
- [ ] Saved the file
- [ ] Synced Gradle in Android Studio
- [ ] Confirmed: "Gradle sync finished"

---

## Troubleshooting Step 2

### Problem: Cannot find Terminal in Android Studio

**Solution:**
1. Go to: **View** > **Tool Windows** > **Terminal**
2. Terminal will open at bottom
3. Or use Method 2 (Command Prompt)

### Problem: "gradlew signingReport" command not found

**Solution:**
1. Make sure you're in the correct directory: `D:\CSE 226\Project 3 Android APP`
2. Check the prompt shows this path
3. If not, type: `cd "D:\CSE 226\Project 3 Android APP"`
4. Then try: `./gradlew signingReport` again

### Problem: Command is taking too long

**Solution:**
1. First time takes 30-60 seconds
2. If it takes more than 5 minutes, check internet connection
3. If still stuck, press Ctrl+C to cancel
4. Try again later

### Problem: Cannot find SHA-1 in the output

**Solution:**
1. Look for "sha1:" in the terminal output
2. It should be after "MD5:"
3. If you don't see it, scroll up in the terminal
4. The output is usually near the end
5. Make sure you ran the full command successfully

### Problem: Google Cloud Console won't load

**Solution:**
1. Try different browser (Chrome, Firefox, Edge)
2. Clear browser cache (Ctrl+Shift+Delete)
3. Try incognito/private window
4. Check internet connection

### Problem: Cannot find "Create Credentials" button

**Solution:**
1. Click on **"Credentials"** in left sidebar
2. Look at top of main area
3. You should see a blue **"+ Create Credentials"** button
4. If you see **"Configure Consent Screen"** instead, click that first

### Problem: Client ID looks wrong or too short

**Solution:**
1. Client ID should end with `.apps.googleusercontent.com`
2. Should be at least 50+ characters long
3. If too short, you copied wrong part
4. Go back to Google Cloud Console
5. Find your OAuth 2.0 Client ID (not API key)
6. Copy the complete value

### Problem: Gradle sync fails after adding Client ID

**Solution:**
1. Check for typos in the Client ID
2. Make sure it has quotes around it
3. Check no extra spaces
4. Example correct format:
   ```kotlin
   resValue("string", "google_client_id", "1234567890-abc.apps.googleusercontent.com")
   ```
5. Save file
6. Try Sync Now again

---

## Summary of Step 2

You completed:

1. ✅ Got SHA-1 fingerprint from Android Studio terminal
2. ✅ Created Google Cloud project
3. ✅ Enabled Google Sign-In API
4. ✅ Created OAuth 2.0 credentials with your SHA-1
5. ✅ Got Client ID
6. ✅ Added Client ID to build.gradle.kts
7. ✅ Synced Gradle

**Result:** Your app is now registered with Google and can perform OAuth authentication!

---

## Next Steps

Move to **STEP 3: Android Project Setup**

But first, verify:
- [ ] No errors in Android Studio
- [ ] build.gradle.kts has your Client ID
- [ ] Gradle sync successful

Then proceed to STEP 3 in STEP_BY_STEP_GUIDE.md

---

## Quick Reference

### SHA-1 Command
```bash
./gradlew signingReport
```

### Google Cloud Console URL
```
https://console.cloud.google.com
```

### OAuth Credentials Details
- **Application Type:** Android
- **Package Name:** com.nsu.transcriptanalyzer
- **SHA-1:** (Your fingerprint from terminal)

### build.gradle.kts Line to Edit
```kotlin
resValue("string", "google_client_id", "YOUR_CLIENT_ID_HERE")
```

---

**You've successfully completed STEP 2! 🎉**

**Next: Continue to STEP 3: Android Project Setup**
