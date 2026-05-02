package com.nsu.transcriptanalyzer.data.prefs

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.nsu.transcriptanalyzer.data.model.User

/**
 * Stores the JWT/session token and user profile securely using EncryptedSharedPreferences
 * (AES-256-GCM for values, AES-256-SIV for keys – both backed by the Android Keystore).
 *
 * If key corruption is detected (e.g. after a factory reset or Keystore wipe), the prefs
 * file is deleted and the user will simply have to sign in again.
 */
class SecurePreferencesManager(private val context: Context) {

    companion object {
        private const val TAG = "SecurePrefs"
        private const val PREFS_FILENAME = "nsu_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_JSON   = "user_json"
    }

    private val gson = Gson()

    private val prefs by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "EncryptedSharedPreferences init failed – wiping prefs file", e)
            // If keystore is corrupted, delete the file so the next launch succeeds
            context.deleteSharedPreferences(PREFS_FILENAME)
            // Return plain SharedPreferences as a last-resort fallback
            context.getSharedPreferences("${PREFS_FILENAME}_fallback", Context.MODE_PRIVATE)
        }
    }

    // ──────────────────────────── Read ────────────────────────────────────────

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER_JSON, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize user", e)
            null
        }
    }

    val isLoggedIn: Boolean
        get() = getAccessToken() != null

    // ──────────────────────────── Write ───────────────────────────────────────

    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun saveUser(user: User) {
        prefs.edit().putString(KEY_USER_JSON, gson.toJson(user)).apply()
    }

    // ──────────────────────────── Clear ───────────────────────────────────────

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
