package com.nsu.transcriptanalyzer.data.api

import android.content.Context
import com.google.gson.GsonBuilder
import com.nsu.transcriptanalyzer.data.prefs.SecurePreferencesManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client.
 *
 * Call [init] once from [MainActivity] before using [apiService].
 * The [AuthTokenInterceptor] is automatically wired in so every request
 * gets a `Authorization: Bearer <token>` header when a token exists.
 */
object RetrofitClient {

    private var retrofit: Retrofit? = null
    lateinit var apiService: TranscriptApiService
        private set

    /**
     * Must be called once – typically in MainActivity.onCreate().
     *
     * @param context     Application context (not activity) for long-lived objects.
     * @param baseUrl     The backend URL (e.g. "https://your-app.onrender.com/").
     *                    **Must** end with a trailing slash.
     * @param securePrefs The [SecurePreferencesManager] used by the auth interceptor.
     */
    fun init(
        context: Context,
        baseUrl: String,
        securePrefs: SecurePreferencesManager
    ) {
        val gson = GsonBuilder().setLenient().create()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = AuthTokenInterceptor(securePrefs)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)   // attach token first …
            .addInterceptor(loggingInterceptor) // … then log the final request
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)  // generous – PDF analysis can be slow
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit!!.create(TranscriptApiService::class.java)
    }

    /**
     * Call this when the BASE_URL changes (e.g. user updates it in settings)
     * to force a fresh Retrofit instance on next [init].
     */
    fun reset() {
        retrofit = null
    }

    // ── Legacy helper kept for backwards-compat during migration ───────────────
    /** @deprecated Use [RetrofitClient.init] + [RetrofitClient.apiService] instead. */
    fun createApiService(
        context: Context,
        baseUrl: String,
        securePrefs: SecurePreferencesManager? = null
    ): TranscriptApiService {
        val prefs = securePrefs ?: SecurePreferencesManager(context)
        if (retrofit == null) init(context, baseUrl, prefs)
        return apiService
    }
}
