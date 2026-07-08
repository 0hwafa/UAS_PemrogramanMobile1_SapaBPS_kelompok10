package com.sapabps.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sapabps.model.GuestBook
import com.sapabps.model.User
import com.sapabps.security.PasswordHasher

@Database(entities = [GuestBook::class, User::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun guestBookDao(): GuestBookDao
    abstract fun userDao(): UserDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "storage_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        insertAdmin(db)
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        insertAdmin(db)
                    }

                    private fun insertAdmin(db: SupportSQLiteDatabase) {
                        val passwordHash = PasswordHasher.hashPassword("admin123")
                        val timestamp = System.currentTimeMillis()
                        db.execSQL("""
                            INSERT INTO users (full_name, email, password_hash, role, timestamp) 
                            VALUES ('Administrator', 'admin@sapabps.com', '$passwordHash', 'admin', $timestamp)
                        """.trimIndent())
                    }
                })
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}