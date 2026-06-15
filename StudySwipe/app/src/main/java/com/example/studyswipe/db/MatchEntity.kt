package com.example.studyswipe.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "matches",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user1Id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user2Id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MatchEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val user1Id: String,
    val user2Id: String,
    val timestamp: Long = System.currentTimeMillis()
)
