package com.sapabps.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sapabps.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Mengaktifkan tampilan Edge-to-Edge (Full Screen)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        // 2. Mengatur padding agar tidak tertutup status bar / navigation bar
        val mainView = findViewById<android.view.View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // 3. Inisialisasi tombol dari XML
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        val btnRegister = findViewById<AppCompatButton>(R.id.btnRegister)

        // 4. Aksi ketika tombol Login diklik
        btnLogin?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // 5. Aksi ketika tombol Register diklik
        btnRegister?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}