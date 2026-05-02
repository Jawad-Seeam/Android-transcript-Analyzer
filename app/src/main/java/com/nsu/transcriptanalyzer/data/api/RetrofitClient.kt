package com.nsu.transcriptanalyzer.data.api

import android.content.Context
import com.google.gson.GsonBuilder
import com.nsu.transcriptanalyzer.BuildConfig
import com.nsu.transcriptanalyzer.data.prefs.SecurePreferencesManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for the NSU Transcript Analyzer.
 *
 * BASE_URL: https://android-transcript-analyzer.onrender.com/
 *           Set via resValue("string", "backend_url", "...") in build.gradle.kts
 *
 * Architecture:
 *   1. [AuthTokenInterceptor]   → injects "Authorization: Bearer <token>"
 *   2. [HttpLoggingInterceptor] → logs requests (BODY in debug, NONE in release)
 *
 * Call [init] once from MainActivity.onCreate() before accessing [apiService].
 */
object RetrofitClient {

    /** The confirmed production backend URL */
    const val BASE_URL = "https://android-transcript-analyzer.onrender.com/"

    private var retrofit: Retrofit? = null

    lateinit var apiService: TranscriptApiService
        private set

    fun init(
        context: Context,
        baseUrl: String = BASE_URL,
        securePrefs: SecurePreferencesManager
    ) {
        val gson = GsonBuilder().setLenient().create()

        // Only log full request/response bodies in debug builds
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        val authInterceptor = AuthTokenInterceptor(securePrefs)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)    // 1st: attach Bearer token
            .addInterceptor(loggingInterceptor) // 2nd: log the final request
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)  // OCR/analysis can be slow on Render
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit!!.create(TranscriptApiService::class.java)
    }

    fun reset() {
        retrofit = null
    }
}
