package com.sapabps.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeUtils {

    private const val PREFIX = "SAPABPS-TICKET"

    /**
     * Encode ticket data ke format string QR.
     * Format: SAPABPS-TICKET:<ticket_id>:<queue_number>
     */
    fun encodeTicketData(ticketId: Int, queueNumber: String): String {
        return "$PREFIX:$ticketId:$queueNumber"
    }

    /**
     * Parse string QR kembali ke Pair(ticketId, queueNumber).
     * Returns null jika format tidak valid.
     */
    fun parseTicketData(qrContent: String): Pair<Int, String>? {
        val parts = qrContent.split(":")
        if (parts.size != 3 || parts[0] != PREFIX) return null
        val ticketId = parts[1].toIntOrNull() ?: return null
        val queueNumber = parts[2]
        if (queueNumber.isBlank()) return null
        return Pair(ticketId, queueNumber)
    }

    /**
     * Generate QR Code Bitmap dari string content.
     */
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
