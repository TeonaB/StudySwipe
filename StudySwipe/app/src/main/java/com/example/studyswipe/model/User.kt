package com.example.studyswipe.model

enum class UserRole {
    STUDENT,
    TUTOR,
    BOTH
}

data class User(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.STUDENT
)
