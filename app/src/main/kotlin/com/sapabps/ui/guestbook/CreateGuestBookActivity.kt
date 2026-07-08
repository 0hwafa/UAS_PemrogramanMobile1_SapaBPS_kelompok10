package com.sapabps.ui.guestbook

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.sapabps.R
import com.sapabps.data.local.AppDatabase
import com.sapabps.data.repository.GuestBookRepository
import com.sapabps.model.RemarkType
import com.sapabps.security.AuthGuardActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateGuestBookActivity : AuthGuardActivity() {

    private lateinit var etAgencyName: TextInputEditText
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var actvRemarks: AutoCompleteTextView
    private lateinit var btnSubmit: Button
    private lateinit var toolbar: Toolbar

    private lateinit var repository: GuestBookRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionManager.isLoggedIn()) return
        
        setContentView(R.layout.activity_create_guest_book)

        val db = AppDatabase.getDatabase(this)
        repository = GuestBookRepository(db.guestBookDao())

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        etAgencyName = findViewById(R.id.etAgencyName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        actvRemarks = findViewById(R.id.actvRemarks)
        btnSubmit = findViewById(R.id.btnSubmit)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, RemarkType.labels())
        actvRemarks.setAdapter(adapter)

        btnSubmit.setOnClickListener { submitForm() }
    }

    private fun submitForm() {
        val agencyName = etAgencyName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val remarks = actvRemarks.text.toString().trim()

        if (agencyName.isEmpty() || phoneNumber.isEmpty() || remarks.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Semua field harus diisi", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        if (!RemarkType.labels().contains(remarks)) {
            Snackbar.make(findViewById(android.R.id.content), "Pilih tujuan kunjungan yang valid", Snackbar.LENGTH_SHORT).show()
            return
        }

        val user = currentUser() ?: return
        btnSubmit.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            val result = repository.createTicket(user.id, agencyName, phoneNumber, remarks)
            
            withContext(Dispatchers.Main) {
                btnSubmit.isEnabled = true
                result.onSuccess { ticket ->
                    AlertDialog.Builder(this@CreateGuestBookActivity)
                        .setTitle("Berhasil")
                        .setMessage(getString(R.string.queue_number_format, ticket.queueNumber))
                        .setPositiveButton("OK") { _, _ -> finish() }
                        .setCancelable(false)
                        .show()
                }.onFailure { e ->
                    Snackbar.make(findViewById(android.R.id.content), e.message ?: "Terjadi kesalahan", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}
