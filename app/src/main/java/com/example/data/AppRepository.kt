package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    // Sessions
    val allSessions: Flow<List<ChatSession>> = appDao.getAllSessionsFlow()

    suspend fun insertSession(session: ChatSession) {
        appDao.insertSession(session)
    }

    suspend fun deleteSession(sessionId: String) {
        appDao.deleteSession(sessionId)
        appDao.deleteMessagesForSession(sessionId)
    }

    // Messages
    fun getMessagesForSessionFlow(sessionId: String): Flow<List<ChatMessage>> {
        return appDao.getMessagesForSessionFlow(sessionId)
    }

    suspend fun getMessagesForSession(sessionId: String): List<ChatMessage> {
        return appDao.getMessagesForSession(sessionId)
    }

    suspend fun insertMessage(message: ChatMessage) {
        appDao.insertMessage(message)
    }

    // Productivity Documents
    val allProductivityDocs: Flow<List<ProductivityDoc>> = appDao.getAllProductivityDocsFlow()

    suspend fun insertProductivityDoc(doc: ProductivityDoc) {
        appDao.insertProductivityDoc(doc)
    }

    suspend fun deleteProductivityDoc(id: String) {
        appDao.deleteProductivityDoc(id)
    }

    // User Schedules
    val userSchedule: Flow<UserSchedule?> = appDao.getUserScheduleFlow()

    suspend fun getUserSchedule(): UserSchedule? {
        return appDao.getUserSchedule()
    }

    suspend fun insertUserSchedule(schedule: UserSchedule) {
        appDao.insertUserSchedule(schedule)
    }

    suspend fun deleteUserSchedule() {
        appDao.deleteUserSchedule()
    }

    // Quran Records
    val allQuranRecords: Flow<List<QuranRecord>> = appDao.getAllQuranRecordsFlow()

    suspend fun insertQuranRecord(record: QuranRecord) {
        appDao.insertQuranRecord(record)
    }

    suspend fun deleteQuranRecord(id: String) {
        appDao.deleteQuranRecord(id)
    }
}
