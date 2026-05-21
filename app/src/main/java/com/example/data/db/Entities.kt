package com.example.data.db

import androidx.room.*

// Main PDF Item Entity
@Entity(tableName = "pdf_items")
data class PdfItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val category: String,
    val examType: String = "pdf",
    val year: String,
    val url: String,
    val size: String
)

// Bookmarks Entity
@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = PdfItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["pdfId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pdfId")]
)
data class BookmarkEntity(
    @PrimaryKey val pdfId: String,
    val bookmarkedAt: Long = System.currentTimeMillis()
)

// Progress tracking Entity
@Entity(
    tableName = "study_progress",
    foreignKeys = [
        ForeignKey(
            entity = PdfItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["pdfId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pdfId")]
)
data class ProgressEntity(
    @PrimaryKey val pdfId: String,
    val lastPageRead: Int,
    val totalPages: Int,
    val lastStudiedAt: Long = System.currentTimeMillis()
)

// Active user session with whitelist verification & role control (admin vs user)
@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val email: String,
    val displayName: String,
    val loginTime: Long = System.currentTimeMillis(),
    val allowedByRemoteWhitelist: Boolean = true,
    val role: String = "user" // "admin" or "user"
)

// Attempt History Entity for Past Papers and Exams
@Entity(tableName = "attempt_history")
data class AttemptHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pdfId: String,
    val examName: String,
    val subject: String,
    val score: Int,             // Correct answers
    val totalQuestions: Int,     // Total MCQs in test
    val timeSpentSeconds: Long,  // Duration of attempt
    val completedAt: Long = System.currentTimeMillis()
)

// Daily Challenge solves
@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey val dateKey: String, // format e.g. "2026-05-21"
    val completed: Boolean = true,
    val correct: Boolean,
    val completedAt: Long = System.currentTimeMillis(),
    val timeTakenSeconds: Long = 0L,
    val selectedOptionIndex: Int = -1
)

// Daily Challenge Cached Questions
@Entity(tableName = "daily_challenge_questions")
data class DailyChallengeQuestionEntity(
    @PrimaryKey val id: String,
    val dateKey: String, // format e.g. "2026-05-21"
    val subject: String,
    val topic: String,
    val question: String,
    val optionsList: String, // optionA||optionB||optionC||optionD joined
    val correctIndex: Int,
    val explanation: String,
    val avgTimeMinutes: Double
)

// Streak tracking state
@Entity(tableName = "streak_stats")
data class StreakStatsEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val lastActiveDate: String = "", // e.g. "2026-05-21"
    val mon: Boolean = false,
    val tue: Boolean = false,
    val wed: Boolean = false,
    val thu: Boolean = false,
    val fri: Boolean = false,
    val sat: Boolean = false,
    val sun: Boolean = false
)
