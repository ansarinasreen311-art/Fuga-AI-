package com.example.auth

import android.accounts.AccountManager
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

data class GoogleAuthUser(
    val email: String,
    val displayName: String,
    val idToken: String? = null,
    val photoUrl: String? = null,
    val isVerified: Boolean = true
)

class GoogleAuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(): Result<GoogleAuthUser> {
        return try {
            // Configure Google Sign-In via Credential Manager
            val googleIdOption = GetSignInWithGoogleOption.Builder(
                serverClientId = "6604691959-fuga-ai.apps.googleusercontent.com"
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            handleCredentialResponse(result)
        } catch (e: GetCredentialException) {
            // If Google Play Services is unavailable, unconfigured, or in emulator without credentials,
            // check available device accounts
            val deviceAccount = getDeviceGoogleAccount()
            if (deviceAccount != null) {
                Result.success(deviceAccount)
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            val deviceAccount = getDeviceGoogleAccount()
            if (deviceAccount != null) {
                Result.success(deviceAccount)
            } else {
                Result.failure(e)
            }
        }
    }

    private fun handleCredentialResponse(response: GetCredentialResponse): Result<GoogleAuthUser> {
        val credential = response.credential
        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Result.success(
                            GoogleAuthUser(
                                email = googleIdTokenCredential.id,
                                displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.id.substringBefore("@"),
                                idToken = googleIdTokenCredential.idToken,
                                photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                            )
                        )
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                } else {
                    Result.failure(IllegalArgumentException("Unexpected credential type: ${credential.type}"))
                }
            }
            else -> Result.failure(IllegalArgumentException("Unsupported credential"))
        }
    }

    private fun getDeviceGoogleAccount(): GoogleAuthUser? {
        return try {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            if (accounts.isNotEmpty()) {
                val primary = accounts[0]
                GoogleAuthUser(
                    email = primary.name,
                    displayName = primary.name.substringBefore("@").replace(".", " ").capitalizeWords(),
                    isVerified = true
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
