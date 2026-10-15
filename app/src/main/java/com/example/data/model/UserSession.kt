package com.example.data.model

data class UserSession(
    val isSignedIn: Boolean = false,
    val displayName: String = "Explorer",
    val email: String = "",
    val photoUrl: String? = null,
    val fugaGmail: String? = null,
    val authMethod: String = "Google OAuth 2.0",
    val isAuthenticatedWithGoogle: Boolean = false,
    val grantedScopes: String = "email, profile, gmail.readonly",
    val lastAuthenticatedAt: Long = System.currentTimeMillis()
)
