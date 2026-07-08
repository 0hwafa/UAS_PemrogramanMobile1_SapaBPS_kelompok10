package com.sapabps.data.repository

import com.sapabps.data.local.GuestBookDao
import com.sapabps.model.GuestBook
import com.sapabps.model.QueueStatus
import java.time.LocalDate
import java.time.ZoneId

class GuestBookRepository(private val guestBookDao: GuestBookDao) {

    suspend fun getQueueHistory(userId: Int, role: String): List<GuestBook> {
        return if (role == "admin") {
            guestBookDao.getAll()
        } else {
            guestBookDao.getByUserId(userId)
        }
    }

    suspend fun createTicket(
        userId: Int,
        agencyName: String,
        phoneNumber: String,
        remarks: String
    ): Result<GuestBook> {
        return try {
            val queueNumber = generateQueueNumber()
            val ticket = GuestBook(
                userId = userId,
                queueNumber = queueNumber,
                agencyName = agencyName,
                phoneNumber = phoneNumber,
                remarks = remarks
            )
            val id = guestBookDao.insert(ticket)
            Result.success(ticket.copy(id = id.toInt()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTicketById(id: Int): GuestBook? {
        return guestBookDao.getById(id)
    }

    suspend fun cancelTicket(ticketId: Int, userId: Int): Result<Unit> {
        return try {
            val ticket = guestBookDao.getById(ticketId)
                ?: return Result.failure(Exception("Tiket tidak ditemukan"))
            
            if (ticket.userId != userId) {
                return Result.failure(Exception("Akses ditolak: Tiket bukan milik Anda"))
            }
            if (ticket.status == QueueStatus.BATAL.name) {
                return Result.failure(Exception("Tiket sudah dibatalkan sebelumnya"))
            }
            if (ticket.status != QueueStatus.MENUNGGU.name) {
                return Result.failure(Exception("Hanya tiket berstatus MENUNGGU yang bisa dibatalkan"))
            }
            
            guestBookDao.updateStatus(ticketId, QueueStatus.BATAL.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTicketStatus(ticketId: Int, newStatus: String): Result<Unit> {
        return try {
            guestBookDao.updateStatus(ticketId, newStatus)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTicket(ticketId: Int): Result<Unit> {
        return try {
            guestBookDao.softDelete(ticketId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateQueueNumber(): String {
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val count = guestBookDao.getCountToday(startOfDay)
        val nextNumber = count + 1
        return String.format("A-%03d", nextNumber)
    }
}
