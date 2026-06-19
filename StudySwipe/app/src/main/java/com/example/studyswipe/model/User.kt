package com.example.studyswipe.model

//Email: admin@studyswipe.com
//Parolă (Password): admin1

enum class UserRole {
    STUDENT,
    TUTOR,
    BOTH,
    ADMIN
}

// Enum cu toate materiile disponibile în aplicație.
// Fiecare valoare are un "displayName" — textul care apare pe ecran.
// Folosim enum ca să avem o listă fixă, fără greșeli de scriere.
enum class Subject(val displayName: String) {
    MATHEMATICS("Matematică"),
    PHYSICS("Fizică"),
    CHEMISTRY("Chimie"),
    BIOLOGY("Biologie"),
    INFORMATICS("Informatică"),
    HISTORY("Istorie"),
    GEOGRAPHY("Geografie"),
    ROMANIAN("Limba Română"),
    ENGLISH("Limba Engleză"),
    FRENCH("Limba Franceză")
}

data class User(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.STUDENT,
    val subjects: Set<Subject> = emptySet(),
    val bio: String = "",
    val avatarUrl: String = "",
    val isProfileComplete: Boolean = false
)
