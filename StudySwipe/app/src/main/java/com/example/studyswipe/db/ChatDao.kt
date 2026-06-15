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
}
