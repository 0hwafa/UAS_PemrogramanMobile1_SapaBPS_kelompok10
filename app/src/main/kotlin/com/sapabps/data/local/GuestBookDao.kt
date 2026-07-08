package com.sapabps.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sapabps.model.GuestBook

@Dao
interface GuestBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(guestBook: GuestBook): Long

    @Query("SELECT * FROM guest_books WHERE is_deleted = 0 ORDER BY timestamp DESC")
    suspend fun getAll(): List<GuestBook>

    @Query("SELECT * FROM guest_books WHERE user_id = :userId AND is_deleted = 0 ORDER BY timestamp DESC")
    suspend fun getByUserId(userId: Int): List<GuestBook>

    @Query("SELECT * FROM guest_books WHERE id = :id")
    suspend fun getById(id: Int): GuestBook?

    @Query("SELECT COUNT(*) FROM guest_books WHERE timestamp >= :startOfDay")
    suspend fun getCountToday(startOfDay: Long): Int

    @Query("UPDATE guest_books SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("UPDATE guest_books SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)

    @Update
    suspend fun update(guestBook: GuestBook)

    @Delete
    suspend fun delete(guestBook: GuestBook)
}