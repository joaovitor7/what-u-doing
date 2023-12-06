package com.discordtime.whatudoing.signin

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.discordtime.whatudoing.signin.model.SignInResult
import com.discordtime.whatudoing.signin.model.User
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class GoogleAuthManager (
    private val context: Context
) {

    private val auth = Firebase.auth
    private val signInClient = Identity.getSignInClient(context)

    suspend fun signInIntentSender                                              (): IntentSender? {
        val result = try {
            signInClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInIntentSender(intent : Intent) : SignInResult {
        val credential = signInClient.getSignInCredentialFromIntent(intent)
        val googleToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    User(
                        id = uid,
                        name = displayName,
                        pictureUrl = photoUrl?.toString()
                    )
                },
                errorMsg = null
            )
        } catch (e:Exception) {
            e.printStackTrace()
            SignInResult(
                data = null,
                errorMsg = e.message
            )
        }

    }

    suspend fun signOut() {
        try {
            signInClient.signOut().await()
            auth.signOut()
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    fun getSignedUser() : User? = auth.currentUser?.run {
        User(
            id = uid,
            name = displayName,
            pictureUrl = photoUrl?.toString()
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.google_client_id))
                    .build()
            ).setAutoSelectEnabled(true)
            .build()
    }
}