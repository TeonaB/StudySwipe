package com.example.studyswipe.db

import androidx.room.Entity

@Entity(
    tableName = "likes",
    primaryKeys = ["likerId", "likedId"]
)
data class LikeEntity(
    val likerId: String,
    val likedId: String,
    val timestamp: Long = System.currentTimeMillis()
)
