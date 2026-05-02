package com.nsu.transcriptanalyzer.data.api

import com.nsu.transcriptanalyzer.data.prefs.SecurePreferencesManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that automatically reads the JWT from [SecurePreferencesManager]
 * and attaches it as an `Authorization: Bearer <token>` header on every outgoing request.
 *
 * The individual API service methods no longer need to accept a `@Header("Authorization")`
 * parameter – this interceptor handles it transparently.
 */
class AuthTokenInterceptor(
    private val securePrefs: SecurePreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = securePrefs.getAccessToken()

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
