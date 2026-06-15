package com.example.studyswipe.db

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "user_subjects",
    primaryKeys = ["userId", "subjectId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserSubjectEntity(
    val userId: String,
    val subjectId: String
)
