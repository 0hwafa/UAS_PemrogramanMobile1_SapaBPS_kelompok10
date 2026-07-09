package com.sapabps.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sapabps.R
import com.sapabps.data.local.AppDatabase
import com.sapabps.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    // Ubah tipe data di sini menjadi EditText
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginLink: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val db = AppDatabase.getDatabase(this)
        userRepository = UserRepository(db.userDao())

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)
        progressBar = findViewById(R.id.progressBar)

        btnRegister.setOnClickListener {
            performRegister()
        }

        tvLoginLink.setOnClickListener {
            finish() // Kembali ke halaman login
        }
    }

    private fun performRegister() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Semua field harus diisi", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Snackbar.make(findViewById(android.R.id.content), "Format email tidak valid", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Snackbar.make(findViewById(android.R.id.content), "Konfirmasi password tidak cocok", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (password.length < 8) {
            Snackbar.make(findViewById(android.R.id.content), "Password minimal 8 karakter", Snackbar.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch(Dispatchers.IO) {
            val result = userRepository.register(email, fullName, password)

            withContext(Dispatchers.Main) {
                setLoading(false)
                result.onSuccess {
                    Toast.makeText(this@RegisterActivity, "Registrasi berhasil, silakan login", Toast.LENGTH_LONG).show()
                    finish() // Kembali ke login
                }.onFailure { error ->
                    Snackbar.make(findViewById(android.R.id.content), error.message ?: "Terjadi kesalahan", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (::progressBar.isInitialized) {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        if (::btnRegister.isInitialized) {
            btnRegister.isEnabled = !isLoading
        }
    }
}