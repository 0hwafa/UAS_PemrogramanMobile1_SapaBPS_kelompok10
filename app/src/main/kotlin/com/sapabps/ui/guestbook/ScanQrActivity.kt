package com.sapabps.ui.guestbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.sapabps.R
import com.sapabps.data.local.AppDatabase
import com.sapabps.data.repository.GuestBookRepository
import com.sapabps.security.AuthGuardActivity
import com.sapabps.utils.QrCodeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

class ScanQrActivity : AuthGuardActivity() {

    override val requiredRole: String = "admin"

    private lateinit var barcodeScanner: DecoratedBarcodeView
    private lateinit var repository: GuestBookRepository
    private var hasNavigated = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startScanning()
        } else {
            Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionManager.isLoggedIn()) return

        setContentView(R.layout.activity_scan_qr)

        val db = AppDatabase.getDatabase(this)
        repository = GuestBookRepository(db.guestBookDao())

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        barcodeScanner = findViewById(R.id.barcodeScanner)
        barcodeScanner.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        barcodeScanner.setStatusText("")

        checkCameraPermissionAndStart()
    }

    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startScanning()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScanning() {
        barcodeScanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result == null || hasNavigated) return
                handleScanResult(result.text)
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                // No-op
            }
        })
    }

    private fun handleScanResult(qrContent: String) {
        val parsed = QrCodeUtils.parseTicketData(qrContent)
        if (parsed == null) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.qr_invalid), Snackbar.LENGTH_SHORT).show()
            // Resume scanning after showing error
            barcodeScanner.resume()
            return
        }

        val (ticketId, _) = parsed
        hasNavigated = true

        // Verify ticket exists in database before navigating
        lifecycleScope.launch(Dispatchers.IO) {
            val ticket = repository.getTicketById(ticketId)
            withContext(Dispatchers.Main) {
                if (ticket != null) {
                    val intent = Intent(this@ScanQrActivity, DetailGuestBookActivity::class.java)
                    intent.putExtra("GUESTBOOK_ID", ticketId)
                    startActivity(intent)
                    finish()
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.qr_ticket_not_found),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    hasNavigated = false
                    barcodeScanner.resume()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasNavigated = false
        barcodeScanner.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeScanner.pause()
    }
}
