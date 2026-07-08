package com.sapabps.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guest_books")
data class GuestBook(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val message: String
)