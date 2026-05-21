package com.example.data.repo

import android.util.Log
import com.example.data.db.SessionDao
import com.example.data.db.UserSessionEntity
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val sessionDao: SessionDao,
    private val pdfRepository: PdfRepository
) {
    // Flow observing the currently active session
    val activeSessionFlow: Flow<UserSessionEntity?> = sessionDao.getActiveSessionFlow()

    suspend fun getActiveSession(): UserSessionEntity? = sessionDao.getActiveSession()

    // Key Login validation function: validates against the synced Whitelist
    suspend fun tryLoginWithGoogleEmail(email: String, name: String = "Shivansh"): Result<UserSessionEntity> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty()) {
            return Result.failure(Exception("Email cannot be empty"))
        }

        // Get synced allowed whitelist emails
        val allowedEmails = pdfRepository.getWhitelistedEmails()
        
        Log.d("AuthRepository", "Verifying login for $trimmedEmail against whitelist entries: $allowedEmails")

        val role = getWhitelistedEmailRole(trimmedEmail, allowedEmails)

        if (role != null) {
            val session = UserSessionEntity(
                email = trimmedEmail,
                displayName = if (trimmedEmail == "spam.iamshivanshcoder@gmail.com") "Shivansh" else name,
                loginTime = System.currentTimeMillis(),
                role = role
            )
            // Save inside local Room database so the login is saved offline
            sessionDao.saveSession(session)
            return Result.success(session)
        } else {
            return Result.failure(
                Exception("The Google Account '$trimmedEmail' is not authorized to use the PrepPapers study portal. Please contact your administrator to whitelist your email address.")
            )
        }
    }

    // Determine role of email from whitelist set
    private fun getWhitelistedEmailRole(email: String, whitelistedSet: Set<String>): String? {
        val search = email.trim().lowercase()
        
        // Failsafe for the creator/developer
        if (search == "spam.iamshivanshcoder@gmail.com") {
            return "admin"
        }

        for (entry in whitelistedSet) {
            val cleaned = entry.trim().lowercase()
            if (cleaned == search) {
                return "user" // Exact matches without role suffix default to "user"
            }
            if (cleaned.startsWith("$search:")) {
                val parsedRole = cleaned.substringAfter(":", "user")
                return if (parsedRole.isNotBlank()) parsedRole else "user"
            }
        }
        return null
    }

    // Terminate user session
    suspend fun logout() {
        sessionDao.clearSession()
    }
}
