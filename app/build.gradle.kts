plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

// ── Compose version alignment ──────────────────────────────────────────────────
// BOM 2023.08.00  →  compose-runtime/ui/animation/material3 all at 1.5.4
// compose-compiler 1.5.4  →  requires exactly Kotlin 1.9.0
//
// Compatibility table: https://developer.android.com/jetpack/androidx/releases/compose-kotlin
//   Kotlin 1.9.0  →  compose-compiler 1.5.4  →  BOM 2023.08.00 ✓
//
// Root cause of the NoSuchMethodError / KeyframesSpec crash:
//   BOM 2024.01.00 ships compose 1.6.1 which added a new overload of
//   KeyframesSpec.at(). When the compiler extension was 1.5.x, material3
//   was compiled against the old API. At runtime compose 1.6.1 was loaded,
//   which no longer had the old at() signature → NoSuchMethodError.
// ─────────────────────────────────────────────────────────────────────────────

android {
    namespace = "com.nsu.transcriptanalyzer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nsu.transcriptanalyzer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ── Google OAuth – Web Client ID
        resValue("string", "google_client_id",
            "516187756859-mt03oj1718igjf3dkq039u2i48iu3coe.apps.googleusercontent.com")
        // ── Production Render backend – trailing slash required by Retrofit
        resValue("string", "backend_url",
            "https://android-transcript-analyzer.onrender.com/")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        // Must match the compose runtime in the BOM exactly.
        // BOM 2023.08.00 → compose 1.5.4 → compiler extension 1.5.4
        // See: https://developer.android.com/jetpack/androidx/releases/compose-kotlin
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // ── Core Android ──────────────────────────────────────────────────────────
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.0")

    // ── Compose BOM – single source of truth for ALL compose versions ─────────
    // BOM 2023.08.00 maps every compose artifact to 1.5.4.
    // NO explicit versions on any androidx.compose.* dependency below.
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")

    // ── Navigation ────────────────────────────────────────────────────────────
    // 2.7.3 ships with compose 1.5.x lifecycle; 2.7.5 introduced a dependency
    // on compose 1.6.x internals that triggers the same crash.
    implementation("androidx.navigation:navigation-compose:2.7.3")

    // ── Google OAuth (Credential Manager) ────────────────────────────────────
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // ── Secure storage ────────────────────────────────────────────────────────
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ── Retrofit & OkHttp ────────────────────────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // ── Coroutines ────────────────────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ── JSON ──────────────────────────────────────────────────────────────────
    implementation("com.google.code.gson:gson:2.10.1")

    // ── Images (Coil) ─────────────────────────────────────────────────────────
    // Coil 2.5.0 supports compose 1.5.x
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ── Local storage ─────────────────────────────────────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ── Camera ────────────────────────────────────────────────────────────────
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // ── Testing ───────────────────────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // ── Debug ─────────────────────────────────────────────────────────────────
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kotlin {
    jvmToolchain(17)
}
