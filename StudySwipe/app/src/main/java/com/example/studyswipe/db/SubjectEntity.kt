package com.example.studyswipe.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey
    val id: String, // Matches Subject enum name, e.g., "MATHEMATICS"
    val name: String,
    val displayName: String
)
