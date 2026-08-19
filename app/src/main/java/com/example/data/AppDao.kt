package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Chat Session Queries ---
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<ChatSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    // --- Chat Message Queries ---
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSessionFlow(sessionId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSession(sessionId: String): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    // --- Productivity Doc Queries ---
    @Query("SELECT * FROM productivity_docs ORDER BY timestamp DESC")
    fun getAllProductivityDocsFlow(): Flow<List<ProductivityDoc>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductivityDoc(doc: ProductivityDoc)

    @Query("DELETE FROM productivity_docs WHERE id = :id")
    suspend fun deleteProductivityDoc(id: String)

    // --- User Schedule Queries ---
    @Query("SELECT * FROM user_schedules WHERE id = 'current' LIMIT 1")
    fun getUserScheduleFlow(): Flow<UserSchedule?>

    @Query("SELECT * FROM user_schedules WHERE id = 'current' LIMIT 1")
    suspend fun getUserSchedule(): UserSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSchedule(schedule: UserSchedule)

    @Query("DELETE FROM user_schedules WHERE id = 'current'")
    suspend fun deleteUserSchedule()

    // --- Quran Record Queries ---
    @Query("SELECT * FROM quran_records ORDER BY timestamp DESC")
    fun getAllQuranRecordsFlow(): Flow<List<QuranRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuranRecord(record: QuranRecord)

    @Query("DELETE FROM quran_records WHERE id = :id")
    suspend fun deleteQuranRecord(id: String)
}
