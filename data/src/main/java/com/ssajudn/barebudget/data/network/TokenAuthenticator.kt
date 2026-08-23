package com.ssajudn.barebudget.data.network

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retries a single time with a force-refreshed Firebase ID token when the API
 * responds 401 (cached token expired). Gives up after one retry to avoid loops.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val user = firebaseAuth.currentUser ?: return null
        val token = runCatching {
            Tasks.await(user.getIdToken(true), 15, TimeUnit.SECONDS).token
        }.getOrNull() ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
