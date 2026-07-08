package com.sapabps.ui.guestbook

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sapabps.R
import com.sapabps.data.local.AppDatabase
import com.sapabps.data.repository.GuestBookRepository
import com.sapabps.model.GuestBook
import com.sapabps.utils.QrCodeUtils
import com.sapabps.model.QueueStatus
import com.sapabps.security.AuthGuardActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailGuestBookActivity : AuthGuardActivity() {

    private lateinit var tvQueueNumberBig: TextView
    private lateinit var tvStatusBadge: TextView
    private lateinit var tvAgencyName: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvRemarks: TextView
    private lateinit var tvTimestamp: TextView
    private lateinit var ivQrCode: ImageView
    
    private lateinit var btnCancel: Button
    private lateinit var layoutAdminActions: LinearLayout
    private lateinit var btnServe: Button
    private lateinit var btnComplete: Button
    private lateinit var btnDelete: Button

    private lateinit var repository: GuestBookRepository
    private var ticketId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionManager.isLoggedIn()) return
        
        setContentView(R.layout.activity_detail_guest_book)

        ticketId = intent.getIntExtra("GUESTBOOK_ID", -1)
        if (ticketId == -1) {
            finish()
            return
        }

        val db = AppDatabase.getDatabase(this)
        repository = GuestBookRepository(db.guestBookDao())

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        initViews()
        loadData()
    }

    private fun initViews() {
        tvQueueNumberBig = findViewById(R.id.tvQueueNumberBig)
        tvStatusBadge = findViewById(R.id.tvStatusBadge)
        tvAgencyName = findViewById(R.id.tvAgencyName)
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber)
        tvRemarks = findViewById(R.id.tvRemarks)
        tvTimestamp = findViewById(R.id.tvTimestamp)
        ivQrCode = findViewById(R.id.ivQrCode)
        
        btnCancel = findViewById(R.id.btnCancel)
        layoutAdminActions = findViewById(R.id.layoutAdminActions)
        btnServe = findViewById(R.id.btnServe)
        btnComplete = findViewById(R.id.btnComplete)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val ticket = repository.getTicketById(ticketId)
            withContext(Dispatchers.Main) {
                if (ticket != null) {
                    populateUI(ticket)
                } else {
                    Toast.makeText(this@DetailGuestBookActivity, "Tiket tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun populateUI(ticket: GuestBook) {
        tvQueueNumberBig.text = ticket.queueNumber
        tvStatusBadge.text = ticket.status
        tvAgencyName.text = ticket.agencyName
        tvPhoneNumber.text = ticket.phoneNumber
        tvRemarks.text = ticket.remarks

        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale.getDefault())
        tvTimestamp.text = sdf.format(Date(ticket.timestamp))

        // Generate QR Code
        val qrContent = QrCodeUtils.encodeTicketData(ticket.id, ticket.queueNumber)
        val qrBitmap = QrCodeUtils.generateQrBitmap(qrContent)
        ivQrCode.setImageBitmap(qrBitmap)

        // Set status badge color
        val bg = tvStatusBadge.background.mutate() as GradientDrawable
        val colorRes = when (ticket.status) {
            QueueStatus.MENUNGGU.name -> R.color.statusMenunggu
            QueueStatus.DILAYANI.name -> R.color.statusDilayani
            QueueStatus.SELESAI.name -> R.color.statusSelesai
            QueueStatus.BATAL.name -> R.color.statusBatal
            else -> R.color.statusMenunggu
        }
        bg.setColor(getColor(colorRes))

        setupActionButtons(ticket)
    }

    private fun setupActionButtons(ticket: GuestBook) {
        val user = currentUser() ?: return
        
        // Reset visibility
        btnCancel.visibility = View.GONE
        layoutAdminActions.visibility = View.GONE
        btnServe.visibility = View.GONE
        btnComplete.visibility = View.GONE
        btnDelete.visibility = View.GONE

        if (user.role == "admin") {
            layoutAdminActions.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE
            
            if (ticket.status == QueueStatus.MENUNGGU.name) {
                btnServe.visibility = View.VISIBLE
            } else if (ticket.status == QueueStatus.DILAYANI.name) {
                btnComplete.visibility = View.VISIBLE
            }

            btnServe.setOnClickListener {
                showConfirmDialog(getString(R.string.confirm_status_change, "DILAYANI")) {
                    updateStatus(QueueStatus.DILAYANI.name)
                }
            }
            btnComplete.setOnClickListener {
                showConfirmDialog(getString(R.string.confirm_status_change, "SELESAI")) {
                    updateStatus(QueueStatus.SELESAI.name)
                }
            }
            btnDelete.setOnClickListener {
                showConfirmDialog(getString(R.string.confirm_delete)) {
                    deleteTicket()
                }
            }
        } else {
            if (ticket.userId == user.id && ticket.status == QueueStatus.MENUNGGU.name) {
                btnCancel.visibility = View.VISIBLE
                btnCancel.setOnClickListener {
                    showConfirmDialog(getString(R.string.confirm_cancel)) {
                        cancelTicket()
                    }
                }
            }
        }
    }

    private fun showConfirmDialog(message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("Ya") { _, _ -> onConfirm() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun updateStatus(newStatus: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = repository.updateTicketStatus(ticketId, newStatus)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    loadData()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), result.exceptionOrNull()?.message ?: "Error", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cancelTicket() {
        val user = currentUser() ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val result = repository.cancelTicket(ticketId, user.id)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    loadData()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), result.exceptionOrNull()?.message ?: "Error", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteTicket() {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = repository.deleteTicket(ticketId)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    Toast.makeText(this@DetailGuestBookActivity, "Tiket dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), result.exceptionOrNull()?.message ?: "Error", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}
