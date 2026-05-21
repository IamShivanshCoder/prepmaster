package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Combined view-helper for PDF items with bookmark status and study progress
data class PdfItemDetail(
    @Embedded val pdfItem: PdfItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "pdfId"
    )
    val bookmark: BookmarkEntity?,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "pdfId"
    )
    val progress: ProgressEntity?
)

@Dao
interface PdfDao {
    @Query("SELECT * FROM pdf_items")
    fun getAllPdfItemsFlow(): Flow<List<PdfItemEntity>>

    @Transaction
    @Query("SELECT * FROM pdf_items WHERE id = :id LIMIT 1")
    fun getPdfItemDetailFlow(id: String): Flow<PdfItemDetail?>

    @Transaction
    @Query("SELECT * FROM pdf_items")
    fun getAllPdfItemDetailsFlow(): Flow<List<PdfItemDetail>>

    @Transaction
    @Query("SELECT * FROM pdf_items WHERE subject = :subject")
    fun getPdfDetailsBySubjectFlow(subject: String): Flow<List<PdfItemDetail>>

    @Transaction
    @Query("SELECT * FROM pdf_items io JOIN bookmarks b ON io.id = b.pdfId")
    fun getBookmarkedPdfsFlow(): Flow<List<PdfItemDetail>>

    @Transaction
    @Query("SELECT * FROM pdf_items io JOIN study_progress sp ON io.id = sp.pdfId ORDER BY sp.lastStudiedAt DESC")
    fun getRecentlyStudiedPdfsFlow(): Flow<List<PdfItemDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdfItems(items: List<PdfItemEntity>)

    @Query("DELETE FROM pdf_items")
    suspend fun clearPdfItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE pdfId = :pdfId")
    suspend fun deleteBookmark(pdfId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Query("DELETE FROM study_progress WHERE pdfId = :pdfId")
    suspend fun deleteProgress(pdfId: String)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM user_session LIMIT 1")
    fun getActiveSessionFlow(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_session LIMIT 1")
    suspend fun getActiveSession(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}

@Dao
interface StatsDao {
    // Attempt history queries
    @Query("SELECT * FROM attempt_history ORDER BY completedAt DESC")
    fun getAllAttemptsFlow(): Flow<List<AttemptHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: AttemptHistoryEntity)

    @Query("DELETE FROM attempt_history")
    suspend fun clearAllAttempts()

    // Daily Challenge completion queries
    @Query("SELECT * FROM daily_challenges WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyChallengeByDate(dateKey: String): DailyChallengeEntity?

    @Query("SELECT * FROM daily_challenges WHERE dateKey = :dateKey LIMIT 1")
    fun getDailyChallengeFlow(dateKey: String): Flow<DailyChallengeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyChallenge(challenge: DailyChallengeEntity)

    @Query("SELECT COUNT(*) FROM daily_challenges")
    fun getChallengesCompletedCountFlow(): Flow<Int>

    // Daily Challenge cached questions queries
    @Query("SELECT * FROM daily_challenge_questions WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyChallengeQuestionByDate(dateKey: String): DailyChallengeQuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyChallengeQuestions(questions: List<DailyChallengeQuestionEntity>)

    // Streak stats
    @Query("SELECT * FROM streak_stats WHERE id = 1 LIMIT 1")
    fun getStreakStatsFlow(): Flow<StreakStatsEntity?>

    @Query("SELECT * FROM streak_stats WHERE id = 1 LIMIT 1")
    suspend fun getStreakStats(): StreakStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStreakStats(stats: StreakStatsEntity)
}
