package com.example.studyswipe.network.dto

import com.example.studyswipe.model.Subject
import com.example.studyswipe.model.User
import com.example.studyswipe.model.UserRole
import com.google.gson.annotations.SerializedName

data class UserDTO(
    val id: Int,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val avatar: String
)

/**
 * Extension function to convert a remote UserDTO into the local domain User model,
 * setting the specified role and some mock values for bio/subjects to keep it complete.
 */
fun UserDTO.toUser(role: UserRole): User {
    return User(
        id = this.id.toString(),
        name = "${this.firstName} ${this.lastName}",
        email = this.email,
        password = "password123", // Default password for details view
        role = role,
        subjects = setOf(Subject.MATHEMATICS, Subject.INFORMATICS), // Default subjects
        bio = "Utilizator importat din ReqRes API. Avatar URL: ${this.avatar}",
        isProfileComplete = true
    )
}