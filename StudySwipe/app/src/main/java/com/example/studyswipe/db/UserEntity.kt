package com.example.studyswipe.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.studyswipe.model.Subject
import com.example.studyswipe.model.UserRole


class Converters {

    @TypeConverter
    fun subjectsToString(subjects: Set<Subject>): String {
        return subjects.joinToString(",") { it.name }
    }

    @TypeConverter
    fun stringToSubjects(value: String): Set<Subject> {
        if (value.isBlank()) return emptySet()
        return value.split(",").mapNotNull { name ->
            Subject.entries.find { it.name == name }
        }.toSet()
    }

    // UserRole → String
    @TypeConverter
    fun roleToString(role: UserRole): String = role.name

    // String → UserRole
    @TypeConverter
    fun stringToRole(value: String): UserRole =
        UserRole.valueOf(value)
}

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class UserEntity(
    // @PrimaryKey = cheia primara (identificator unic, ca ID-ul dintr-un tabel SQL)
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole,
    // Set<Subject> se stocheaza ca String prin TypeConverter-ul de mai sus
    val subjects: Set<Subject>,
    val bio: String,
    val isProfileComplete: Boolean
)
