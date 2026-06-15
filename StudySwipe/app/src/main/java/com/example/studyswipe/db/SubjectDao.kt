package com.example.studyswipe.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjects: List<SubjectEntity>): List<Long>

    @Query("SELECT * FROM subjects ORDER BY displayName ASC")
    fun getAll(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSubject(userSubject: UserSubjectEntity): Long

    @Query("DELETE FROM user_subjects WHERE userId = :userId")
    suspend fun deleteUserSubjects(userId: String): Int

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserWithSubjects(userId: String): Flow<UserWithSubjects?>

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserWithSubjectsById(userId: String): UserWithSubjects?

    @Transaction
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserWithSubjectsByEmail(email: String): UserWithSubjects?

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithSubjects(): Flow<List<UserWithSubjects>>
}
