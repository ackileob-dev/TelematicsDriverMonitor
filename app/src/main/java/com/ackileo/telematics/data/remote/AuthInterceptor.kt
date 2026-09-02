package com.ackileo.telematics.data.remote

import com.ackileo.telematics.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Adds JWT bearer tokens to protected requests and clears auth state on 401.
 * This interceptor does not perform network refresh calls, which avoids recursive auth loops.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath

        val requestBuilder = originalRequest.newBuilder()

        // Avoid attaching auth headers to auth bootstrap endpoints.
        if (!isAuthBootstrapEndpoint(requestPath)) {
            tokenManager.getAccessToken()?.let { token ->
                if (originalRequest.header(HEADER_AUTHORIZATION).isNullOrBlank()) {
                    requestBuilder.header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$token")
                }
            }
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == HTTP_UNAUTHORIZED) {
            tokenManager.clearAuthState()
        }

        return response
    }

    private fun isAuthBootstrapEndpoint(path: String): Boolean {
        return path.endsWith("/auth/login") || path.endsWith("/auth/register")
    }

    private companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        const val BEARER_PREFIX = "Bearer "
        const val HTTP_UNAUTHORIZED = 401
    }
}

