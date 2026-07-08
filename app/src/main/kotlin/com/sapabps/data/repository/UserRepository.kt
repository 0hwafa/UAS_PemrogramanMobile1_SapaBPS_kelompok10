package com.sapabps.data.repository

import com.sapabps.data.local.UserDao
import com.sapabps.model.User
import com.sapabps.security.PasswordHasher

class UserRepository(private val userDao: UserDao) {

    suspend fun register(email: String, fullName: String, password: String): Result<User> {
        try {
            if (isEmailTaken(email)) {
                return Result.failure(Exception("Email sudah terdaftar"))
            }

            val passwordHash = PasswordHasher.hashPassword(password)
            val user = User(
                fullName = fullName,
                email = email,
                passwordHash = passwordHash,
                role = "user" // Role hardcoded to user
            )
            
            val id = userDao.insert(user)
            val newUser = user.copy(id = id.toInt())
            return Result.success(newUser)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val user = userDao.getUserByEmail(email)
                ?: return Result.failure(Exception("Email atau password salah"))

            if (PasswordHasher.verifyPassword(password, user.passwordHash)) {
                Result.success(user)
            } else {
                Result.failure(Exception("Email atau password salah"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isEmailTaken(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }
}
