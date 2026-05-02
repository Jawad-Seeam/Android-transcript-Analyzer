package com.nsu.transcriptanalyzer.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.nsu.transcriptanalyzer.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nsu_transcript_prefs")

class PreferencesManager(private val context: Context) {
    private val gson = Gson()

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val USER_DATA = stringPreferencesKey("user_data")
        private val BACKEND_URL = stringPreferencesKey("backend_url")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACCESS_TOKEN]
    }

    val userData: Flow<User?> = context.dataStore.data.map { prefs ->
        prefs[USER_DATA]?.let {
            try {
                gson.fromJson(it, User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    val backendUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[BACKEND_URL]
    }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = token
        }
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit { prefs ->
            prefs[USER_DATA] = gson.toJson(user)
        }
    }

    suspend fun saveBackendUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[BACKEND_URL] = url
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
