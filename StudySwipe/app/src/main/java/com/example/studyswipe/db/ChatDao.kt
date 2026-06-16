package com.example.studyswipe.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("SELECT * FROM matches WHERE user1Id = :userId OR user2Id = :userId ORDER BY timestamp DESC")
    fun getMatchesForUser(userId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM messages WHERE matchId = :matchId ORDER BY timestamp ASC")
    fun getMessagesForMatch(matchId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM matches WHERE (user1Id = :user1Id AND user2Id = :user2Id) OR (user1Id = :user2Id AND user2Id = :user1Id) LIMIT 1")
    suspend fun getMatchBetweenUsers(user1Id: String, user2Id: String): MatchEntity?
    
    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatch(matchId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity): Long

    @Query("DELETE FROM likes WHERE likerId = :likerId AND likedId = :likedId")
    suspend fun deleteLike(likerId: String, likedId: String): Int

    @Query("SELECT * FROM likes WHERE likerId = :likerId")
    fun getLikesForLiker(likerId: String): Flow<List<LikeEntity>>

    @Query("SELECT * FROM likes WHERE likedId = :likedId")
    fun getLikesForLiked(likedId: String): Flow<List<LikeEntity>>

    @Query("SELECT * FROM likes WHERE likerId = :likerId AND likedId = :likedId LIMIT 1")
    suspend fun getLike(likerId: String, likedId: String): LikeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDislike(dislike: DislikeEntity): Long

    @Query("SELECT * FROM dislikes WHERE dislikerId = :dislikerId")
    fun getDislikesForDisliker(dislikerId: String): Flow<List<DislikeEntity>>

    @Query("DELETE FROM likes WHERE likerId = :userId OR likedId = :userId")
    suspend fun deleteLikesForUser(userId: String): Int

    @Query("DELETE FROM dislikes WHERE dislikerId = :userId OR dislikedId = :userId")
    suspend fun deleteDislikesForUser(userId: String): Int

    @Query("DELETE FROM matches WHERE user1Id = :userId OR user2Id = :userId")
    suspend fun deleteMatchesForUser(userId: String): Int
}
