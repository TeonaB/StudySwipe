package com.example.studyswipe.model

enum class UserRole {
    STUDENT,
    TUTOR,
    BOTH
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
    // Set în loc de List — nu poți adăuga aceeași materie de două ori
    val subjects: Set<Subject> = emptySet(),
    val bio: String = "",
    // Flag care ne spune dacă utilizatorul a trecut prin ProfileSetupPage
    val isProfileComplete: Boolean = false
)
