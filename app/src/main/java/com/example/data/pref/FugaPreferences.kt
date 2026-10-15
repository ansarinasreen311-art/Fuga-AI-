package com.example.data.pref

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.UserSession
import com.example.security.FugaKeystoreManager

class FugaPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val keystoreManager = FugaKeystoreManager(context)

    companion object {
        private const val PREF_NAME = "fuga_ai_prefs"
        private const val KEY_FIRST_LAUNCH_COMPLETED = "first_launch_completed"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHOTO = "user_photo"
        private const val KEY_FUGA_GMAIL = "fuga_gmail"
        private const val KEY_AUTH_METHOD = "auth_method"
        private const val KEY_IS_GOOGLE_AUTH = "is_google_auth"
        private const val KEY_GRANTED_SCOPES = "granted_scopes"
        private const val KEY_LAST_AUTH_TIME = "last_auth_time"
        private const val KEY_WAKE_WORD_ENABLED = "wake_word_enabled"
        private const val KEY_ALWAYS_READY_ENABLED = "always_ready_enabled"
        private const val KEY_WAKE_WORD_CHIME_ENABLED = "wake_word_chime_enabled"
        private const val KEY_WAKE_WORD_HAPTIC_ENABLED = "wake_word_haptic_enabled"
        private const val KEY_WAKE_WORD_SENSITIVITY = "wake_word_sensitivity"
        private const val KEY_DYNAMIC_ISLAND_ENABLED = "dynamic_island_enabled"
        private const val KEY_SCREEN_ACCESS_ENABLED = "screen_access_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_MEMORY_ENABLED = "memory_enabled"
        private const val KEY_VOICE_FEEDBACK_ENABLED = "voice_feedback_enabled"
        private const val KEY_BACKGROUND_SERVICE_ENABLED = "bg_service_enabled"
    }

    var isFirstLaunchCompleted: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH_COMPLETED, value).apply()

    /**
     * Retrieves the decrypted Gemini API Key securely from Android Keystore.
     */
    fun getStoredApiKey(): String {
        return keystoreManager.getDecryptedApiKey()
    }

    /**
     * Securely encrypts and stores the API Key in the Android Keystore system.
     */
    fun setApiKey(key: String) {
        keystoreManager.saveApiKey(key)
    }

    /**
     * Wipes the encrypted API Key from the Keystore vault.
     */
    fun removeApiKey() {
        keystoreManager.clearApiKey()
    }

    fun hasValidApiKey(): Boolean {
        return keystoreManager.hasValidApiKey()
    }

    fun getMaskedApiKey(): String {
        return keystoreManager.getMaskedApiKey()
    }

    fun isHardwareBackedKeystore(): Boolean {
        return keystoreManager.isHardwareBacked()
    }

    fun getUserSession(): UserSession {
        val name = prefs.getString(KEY_USER_NAME, "Explorer") ?: "Explorer"
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val photo = prefs.getString(KEY_USER_PHOTO, null)
        val fugaGmail = prefs.getString(KEY_FUGA_GMAIL, null)
        val authMethod = prefs.getString(KEY_AUTH_METHOD, "Google OAuth 2.0") ?: "Google OAuth 2.0"
        val isGoogleAuth = prefs.getBoolean(KEY_IS_GOOGLE_AUTH, email.isNotEmpty())
        val scopes = prefs.getString(KEY_GRANTED_SCOPES, "email, profile, gmail.readonly") ?: "email, profile, gmail.readonly"
        val lastAuth = prefs.getLong(KEY_LAST_AUTH_TIME, System.currentTimeMillis())
        val isSignedIn = email.isNotEmpty()

        return UserSession(
            isSignedIn = isSignedIn,
            displayName = name,
            email = email,
            photoUrl = photo,
            fugaGmail = fugaGmail,
            authMethod = authMethod,
            isAuthenticatedWithGoogle = isGoogleAuth,
            grantedScopes = scopes,
            lastAuthenticatedAt = lastAuth
        )
    }

    fun saveUserSession(
        name: String,
        email: String,
        photoUrl: String? = null,
        fugaGmail: String? = null,
        authMethod: String = "Google OAuth 2.0 (Credential Manager)",
        isGoogleAuth: Boolean = true,
        scopes: String = "email, profile, gmail.readonly"
    ) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_PHOTO, photoUrl)
            .putString(KEY_FUGA_GMAIL, fugaGmail)
            .putString(KEY_AUTH_METHOD, authMethod)
            .putBoolean(KEY_IS_GOOGLE_AUTH, isGoogleAuth)
            .putString(KEY_GRANTED_SCOPES, scopes)
            .putLong(KEY_LAST_AUTH_TIME, System.currentTimeMillis())
            .apply()
    }

    fun clearUserSession() {
        prefs.edit()
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_PHOTO)
            .remove(KEY_FUGA_GMAIL)
            .remove(KEY_AUTH_METHOD)
            .remove(KEY_IS_GOOGLE_AUTH)
            .remove(KEY_GRANTED_SCOPES)
            .remove(KEY_LAST_AUTH_TIME)
            .apply()
    }

    var isWakeWordEnabled: Boolean
        get() = prefs.getBoolean(KEY_WAKE_WORD_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, value).apply()

    var isAlwaysReadyEnabled: Boolean
        get() = prefs.getBoolean(KEY_ALWAYS_READY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ALWAYS_READY_ENABLED, value).apply()

    var isWakeWordChimeEnabled: Boolean
        get() = prefs.getBoolean(KEY_WAKE_WORD_CHIME_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_WAKE_WORD_CHIME_ENABLED, value).apply()

    var isWakeWordHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_WAKE_WORD_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_WAKE_WORD_HAPTIC_ENABLED, value).apply()

    var wakeWordSensitivity: String
        get() = prefs.getString(KEY_WAKE_WORD_SENSITIVITY, "Balanced") ?: "Balanced"
        set(value) = prefs.edit().putString(KEY_WAKE_WORD_SENSITIVITY, value).apply()

    var isDynamicIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_ENABLED, value).apply()

    var isScreenAccessEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCREEN_ACCESS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SCREEN_ACCESS_ENABLED, value).apply()

    var isBiometricSecurityEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var isMemoryEnabled: Boolean
        get() = prefs.getBoolean(KEY_MEMORY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MEMORY_ENABLED, value).apply()

    var isVoiceFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_FEEDBACK_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_FEEDBACK_ENABLED, value).apply()

    var isBackgroundServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_BACKGROUND_SERVICE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BACKGROUND_SERVICE_ENABLED, value).apply()
}
