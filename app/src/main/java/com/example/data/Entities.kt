package com.example.data

data class ChatSession(
    val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val personaId: String? = null
)

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProductivityDoc(
    val id: String,
    val type: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserSchedule(
    val id: String = "current",
    val age: Int,
    val occupation: String,
    val schoolOrWorkTimings: String,
    val lessonTimings: String,
    val scheduleText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuranRecord(
    val id: String,
    val surah: String,
    val userTranscription: String,
    val aiFeedback: String,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)
