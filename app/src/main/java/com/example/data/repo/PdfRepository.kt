package com.example.data.repo

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.db.*
import com.example.data.net.UpdateService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class PdfRepository(
    private val context: Context,
    private val pdfDao: PdfDao
) {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("prep_master_prefs", Context.MODE_PRIVATE)

    private val updateService = UpdateService.create()

    // Retrieve active remote configuration URL (admin setting)
    fun getRemoteConfigUrl(): String {
        return sharedPrefs.getString(
            "remote_config_url",
            "https://raw.githubusercontent.com/aistudio-templates/mock-data/main/exam_prep_config.json" // Default URL showing instructions
        ) ?: ""
    }

    // Update active remote configuration URL
    fun saveRemoteConfigUrl(url: String) {
        sharedPrefs.edit().putString("remote_config_url", url).apply()
    }

    // Get cached whitelist emails (from whitelist list + users map keys)
    fun getWhitelistedEmails(): Set<String> {
        val fromWhitelist = sharedPrefs.getStringSet("whitelisted_emails", setOf()) ?: setOf()
        val fromUsers = getSyncedUsers().keys
        val merged = fromWhitelist + fromUsers
        if (merged.isEmpty()) {
            return setOf(
                "spam.iamshivanshcoder@gmail.com",
                "exammanager@gmail.com",
                "student@school.edu",
                "testuser@gmail.com"
            )
        }
        return merged
    }

    fun saveWhitelistedEmails(emails: Set<String>) {
        sharedPrefs.edit().putStringSet("whitelisted_emails", emails).apply()
    }

    // Get synced user credentials map (email -> password_hash)
    fun getSyncedUsers(): Map<String, String> {
        val json = sharedPrefs.getString("synced_users", "") ?: ""
        if (json.isBlank()) return emptyMap()
        return try {
            val pairs = json.split(";;").mapNotNull { entry ->
                val parts = entry.split("::", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            pairs.toMap()
        } catch (e: Exception) { emptyMap() }
    }

    fun saveSyncedUsers(users: Map<String, String>) {
        val json = users.entries.joinToString(";;") { "${it.key}::${it.value}" }
        sharedPrefs.edit().putString("synced_users", json).apply()
    }

    // Observables from Local Database
    val allPdfsFlow: Flow<List<PdfItemDetail>> = pdfDao.getAllPdfItemDetailsFlow()
    val bookmarkedPdfsFlow: Flow<List<PdfItemDetail>> = pdfDao.getBookmarkedPdfsFlow()
    val recentlyStudiedPdfsFlow: Flow<List<PdfItemDetail>> = pdfDao.getRecentlyStudiedPdfsFlow()

    fun getPdfsBySubjectFlow(subject: String): Flow<List<PdfItemDetail>> = 
        pdfDao.getPdfDetailsBySubjectFlow(subject)

    fun getPdfItemDetailFlow(id: String): Flow<PdfItemDetail?> = 
        pdfDao.getPdfItemDetailFlow(id)

    // Synchronization engine
    suspend fun syncRemoteConfig(customUrl: String? = null): Result<Unit> {
        return try {
            val endpoint = customUrl ?: getRemoteConfigUrl()
            if (endpoint.isBlank()) {
                return Result.failure(Exception("Configurations database URL is empty"))
            }

            Log.d("PdfRepository", "Syncing remote config from: $endpoint")
            val remoteConfig = updateService.fetchRemoteConfig(endpoint)

            // 1. Process updated PDFs database
            if (remoteConfig.pdfs.isNotEmpty()) {
                val entities = remoteConfig.pdfs.map { remotePdf ->
                    PdfItemEntity(
                        id = remotePdf.id,
                        title = remotePdf.title,
                        subject = remotePdf.subject,
                        category = remotePdf.category,
                        examType = remotePdf.examType,
                        year = remotePdf.year,
                        url = remotePdf.url,
                        size = remotePdf.size
                    )
                }
                
                // Clear and cache new inputs in Room
                pdfDao.clearPdfItems()
                pdfDao.insertPdfItems(entities)
                Log.d("PdfRepository", "Synced ${entities.size} PDFs from online source")
            }

            // 2. Process updated Access Whitelist
            if (remoteConfig.whitelist.isNotEmpty()) {
                saveWhitelistedEmails(remoteConfig.whitelist.toSet())
                Log.d("PdfRepository", "Synced ${remoteConfig.whitelist.size} allowed accounts from online whitelist")
            }

            // 2.5 Process user credentials (email -> hashed password)
            if (remoteConfig.users.isNotEmpty()) {
                saveSyncedUsers(remoteConfig.users)
                Log.d("PdfRepository", "Synced ${remoteConfig.users.size} user credentials from online source")
            }

            // 3. Process daily challenges
            if (remoteConfig.dailyChallenges.isNotEmpty()) {
                val statsDao = AppDatabase.getDatabase(context).statsDao()
                val challengeEntities = remoteConfig.dailyChallenges.map { rc ->
                    DailyChallengeQuestionEntity(
                        id = rc.id,
                        dateKey = rc.date,
                        subject = rc.subject,
                        topic = rc.topic,
                        question = rc.question,
                        optionsList = rc.options.joinToString("||"),
                        correctIndex = rc.correctIndex,
                        explanation = rc.explanation,
                        avgTimeMinutes = rc.avgTimeMinutes
                    )
                }
                statsDao.insertDailyChallengeQuestions(challengeEntities)
                Log.d("PdfRepository", "Synced ${challengeEntities.size} Daily Challenges into database")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PdfRepository", "Sync failed: ", e)
            Result.failure(e)
        }
    }

    // Toggle custom bookmarks
    suspend fun toggleBookmark(pdfId: String, isCurrentlyBookmarked: Boolean) {
        if (isCurrentlyBookmarked) {
            pdfDao.deleteBookmark(pdfId)
        } else {
            pdfDao.insertBookmark(BookmarkEntity(pdfId = pdfId))
        }
    }

    // Update ongoing reading progress
    suspend fun updateReadingProgress(pdfId: String, page: Int, totalPages: Int) {
        pdfDao.insertProgress(
            ProgressEntity(
                pdfId = pdfId,
                lastPageRead = page,
                totalPages = totalPages,
                lastStudiedAt = System.currentTimeMillis()
            )
        )
    }

    // Clear reading session statistics
    suspend fun clearProgress(pdfId: String) {
        pdfDao.deleteProgress(pdfId)
    }

    // Seeds the database with highly realistic initial prep material so first launch is spectacular!
    suspend fun seedDatabaseIfEmpty() {
        // Collect existing list
        val currentPdfs = pdfDao.getAllPdfItemsFlow().firstOrNull() ?: emptyList()
        if (currentPdfs.isNotEmpty()) {
            return // Database already seeded
        }

        Log.d("PdfRepository", "Seeding initial local database with prep materials")
        val sampleItems = listOf(
            PdfItemEntity(
                id = "math_2025_board",
                title = "2025 National Mathematics Mock Exam Prep",
                subject = "Mathematics",
                category = "Past Papers",
                examType = "pdf",
                year = "2025",
                url = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                size = "1.4 MB"
            ),
            PdfItemEntity(
                id = "math_calculus_notes",
                title = "Advanced Integral Calculus Mastery Notes",
                subject = "Mathematics",
                category = "Notes",
                examType = "pdf",
                year = "2024",
                url = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                size = "3.2 MB"
            ),
            PdfItemEntity(
                id = "math_formulas_sheet",
                title = "Essential Trigonometry & Algebra Cheat Sheet",
                subject = "Mathematics",
                category = "Formula Sheets",
                examType = "pdf",
                year = "2025",
                url = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                size = "750 KB"
            ),
            PdfItemEntity(
                id = "phys_electrodynamics",
                title = "Electrostatics & Electrodynamics Lecture Guide",
                subject = "Physics",
                category = "Notes",
                examType = "pdf",
                year = "2025",
                url = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                size = "4.5 MB"
            ),
            PdfItemEntity(
                id = "phys_2024_paper",
                title = "2024 Advanced Physics Exam Paper with Key",
                subject = "Physics",
                category = "Past Papers",
                examType = "pdf",
                year = "2024",
                url = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                size = "2.1 MB"
            ),
            PdfItemEntity(
                id = "chem_organic_mech",
                title = "Organic Reactions & Mechanisms Summary Sheet",
                subject = "Chemistry",
                category = "Formula Sheets",
                examType = "pdf",
                year = "2025",
                url = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                size = "1.8 MB"
            ),
            PdfItemEntity(
                id = "chem_thermodynamics_notes",
                title = "Chemical Kinetics and Equilibrium Prep Notes",
                subject = "Chemistry",
                category = "Notes",
                examType = "pdf",
                year = "2024",
                url = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                size = "2.9 MB"
            ),
            PdfItemEntity(
                id = "cs_dsa_cheatsheet",
                title = "Data Structures & Essential Algorithms Cheat Sheet",
                subject = "Computer Science",
                category = "Formula Sheets",
                examType = "pdf",
                year = "2025",
                url = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                size = "1.1 MB"
            ),
            PdfItemEntity(
                id = "cs_2025_prep",
                title = "2025 Object-Oriented Design & SQL board prep",
                subject = "Computer Science",
                category = "Past Papers",
                examType = "pdf",
                year = "2025",
                url = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                size = "1.5 MB"
            ),
            PdfItemEntity(
                id = "daily_challenge_mock",
                title = "Daily Challenge — Mixed Subject Quiz",
                subject = "All Subjects",
                category = "Daily Challenge",
                examType = "mcq",
                year = "2025",
                url = "",
                size = "N/A"
            )
        )

        pdfDao.insertPdfItems(sampleItems)

        // Seed default user credentials so login works out of the box without remote sync
        val existingUsers = getSyncedUsers()
        if (existingUsers.isEmpty()) {
            saveSyncedUsers(
                mapOf(
                    "spam.iamshivanshcoder@gmail.com" to "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
                    "exammanager@gmail.com" to "6ee4a469cd4e91053847f5d3fcb61dbcc91e8f0ef10be7748da4c4a1ba382d17",
                    "student@school.edu" to "264c8c381bf16c982a4e59b0dd4c6f7808c51a05f64c35db42cc78a2a72875bb",
                    "testuser@gmail.com" to "ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f"
                )
            )
        }
    }
}
