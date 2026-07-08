package com.sapabps.security

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sapabps.ui.auth.LoginActivity

abstract class AuthGuardActivity : AppCompatActivity() {
    protected open val requiredRole: String? = null
    protected lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin()
            return
        }

        if (requiredRole != null && !sessionManager.hasRole(requiredRole!!)) {
            Toast.makeText(this, "Akses ditolak. Anda tidak memiliki izin.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    protected fun currentUser(): SessionUser? {
        return sessionManager.getCurrentUser()
    }

    protected fun performLogout() {
        sessionManager.logout()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
