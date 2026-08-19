package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val personaId: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "productivity_docs")
data class ProductivityDoc(
    @PrimaryKey val id: String,
    val type: String, // "research", "summary", "report", "presentation"
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_schedules")
data class UserSchedule(
    @PrimaryKey val id: String = "current",
    val age: Int,
    val occupation: String, // "study" or "work" or "both"
    val schoolOrWorkTimings: String,
    val lessonTimings: String,
    val scheduleText: String, // Generated schedule Markdown from Gemini
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quran_records")
data class QuranRecord(
    @PrimaryKey val id: String,
    val surah: String,
    val userTranscription: String,
    val aiFeedback: String, // Analysis of voice, corrections
    val score: Int, // Out of 100
    val timestamp: Long = System.currentTimeMillis()
)
