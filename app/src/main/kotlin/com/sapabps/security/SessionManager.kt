package com.sapabps.security

import android.content.Context
import android.content.SharedPreferences
import com.sapabps.model.User

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "SapaBpsSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_FULL_NAME = "userFullName"
        private const val KEY_USER_ROLE = "userRole"
        private const val KEY_LOGIN_TIMESTAMP = "loginTimestamp"
        private const val SESSION_EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    fun createSession(user: User) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_FULL_NAME, user.fullName)
            putString(KEY_USER_ROLE, user.role)
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && !isSessionExpired()
    }

    fun isSessionExpired(): Boolean {
        val loginTimestamp = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0)
        val isExpired = System.currentTimeMillis() - loginTimestamp > SESSION_EXPIRY_MS
        if (isExpired && prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            logout() // Auto logout if expired
        }
        return isExpired
    }

    fun getCurrentUser(): SessionUser? {
        if (!isLoggedIn()) return null
        
        return SessionUser(
            id = prefs.getInt(KEY_USER_ID, 0),
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            fullName = prefs.getString(KEY_USER_FULL_NAME, "") ?: "",
            role = prefs.getString(KEY_USER_ROLE, "user") ?: "user"
        )
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun hasRole(role: String): Boolean {
        return getCurrentUser()?.role == role
    }
}
