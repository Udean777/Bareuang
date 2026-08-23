package com.ssajudn.barebudget.data.network

import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Tasks
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches a real, cryptographically verified Firebase ID token as the
 * `Authorization: Bearer` credential on every request.
 *
 * - Signed-in user (incl. anonymous guests): token comes from
 *   [com.google.firebase.auth.FirebaseUser.getIdToken] (cached by the Firebase
 *   SDK; refreshed automatically when expired).
 * - No session (offline guest / onboarding): no auth header is sent; the app
 *   is local-first so requests simply fail gracefully until authenticated.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val user = firebaseAuth.currentUser
            ?: return chain.proceed(
                original.newBuilder().header("Accept", "application/json").build()
            )

        val token = runCatching {
            Tasks.await(user.getIdToken(false), 10, TimeUnit.SECONDS).token
        }.getOrNull()

        val builder = original.newBuilder().header("Accept", "application/json")
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}
