package com.example.studyswipe.db

import androidx.room.Entity

@Entity(
    tableName = "dislikes",
    primaryKeys = ["dislikerId", "dislikedId"]
)
data class DislikeEntity(
    val dislikerId: String,
    val dislikedId: String,
    val timestamp: Long = System.currentTimeMillis()
)
