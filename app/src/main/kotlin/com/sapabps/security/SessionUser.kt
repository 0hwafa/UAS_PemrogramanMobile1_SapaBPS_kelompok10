package com.sapabps.security

data class SessionUser(
    val id: Int,
    val email: String,
    val fullName: String,
    val role: String
)
