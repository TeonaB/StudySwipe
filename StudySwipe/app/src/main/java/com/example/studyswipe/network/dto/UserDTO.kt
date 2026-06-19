package com.example.studyswipe.network.dto

import com.example.studyswipe.model.Subject
import com.example.studyswipe.model.User
import com.example.studyswipe.model.UserRole
import com.google.gson.annotations.SerializedName

data class UserDTO(
    val id: Long,
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
    val rolePrefix = role.name.lowercase()
    val uniqueId = "$rolePrefix-${this.id}"
    // Make email unique to prevent conflict between same user in different roles
    val uniqueEmail = "$rolePrefix.${this.email}"
    
    // Custom subjects based on role to make profiles look realistic
    val customSubjects = when (role) {
        UserRole.STUDENT -> setOf(Subject.MATHEMATICS, Subject.ROMANIAN, Subject.ENGLISH)
        UserRole.TUTOR -> setOf(Subject.MATHEMATICS, Subject.INFORMATICS, Subject.PHYSICS)
        UserRole.BOTH -> setOf(Subject.MATHEMATICS, Subject.INFORMATICS, Subject.ROMANIAN)
        UserRole.ADMIN -> emptySet()
    }

    val customBio = when (role) {
        UserRole.STUDENT -> "Student în căutare de ajutor la materii. Importat din ReqRes API."
        UserRole.TUTOR -> "Tutor experimentat oferind meditații. Importat din ReqRes API."
        UserRole.BOTH -> "Cont dublu: atât student cât și tutor în domeniu. Importat din ReqRes API."
        UserRole.ADMIN -> "Administrator"
    }

    return User(
        id = uniqueId,
        name = "${this.firstName} ${this.lastName}",
        email = uniqueEmail,
        password = "password123", // Default password for details view
        role = role,
        subjects = customSubjects,
        bio = customBio,
        avatarUrl = this.avatar,
        isProfileComplete = true
    )
}