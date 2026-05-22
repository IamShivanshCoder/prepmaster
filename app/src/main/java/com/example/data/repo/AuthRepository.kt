package com.example.data.repo

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.db.SessionDao
import com.example.data.db.UserSessionEntity
import com.example.data.net.FirebaseAuthService
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val context: Context,
    private val sessionDao: SessionDao,
    private val pdfRepository: PdfRepository
) {
    private val firebaseAuthService = FirebaseAuthService.create()
    private val firebaseApiKey: String? = try {
        BuildConfig.FIREBASE_API_KEY.takeIf { it.isNotBlank() && !it.startsWith("YOUR_") }
    } catch (e: Exception) { null }

    // Flow observing the currently active session
    val activeSessionFlow: Flow<UserSessionEntity?> = sessionDao.getActiveSessionFlow()

    suspend fun getActiveSession(): UserSessionEntity? = sessionDao.getActiveSession()

    // Verify credentials using Firebase Auth REST API (online)
    // Returns true if Firebase is configured and auth succeeds
    suspend fun verifyWithFirebase(email: String, password: String): Result<Boolean> {
        val apiKey = firebaseApiKey ?: return Result.failure(Exception("Firebase not configured"))

        return try {
            val response = firebaseAuthService.signInWithPassword(
                apiKey = apiKey,
                email = email,
                password = password
            )
            if (response.idToken != null) {
                Log.d("AuthRepository", "Firebase auth succeeded for $email")
                Result.success(true)
            } else {
                val msg = response.error?.message ?: "Authentication failed"
                Log.d("AuthRepository", "Firebase auth failed for $email: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Firebase network error: ${e.message}")
            Result.failure(e)
        }
    }

    // Key Login validation function: validates against the synced Whitelist
    suspend fun tryLoginWithGoogleEmail(email: String, name: String = "Shivansh"): Result<UserSessionEntity> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty()) {
            return Result.failure(Exception("Email cannot be empty"))
        }

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
            sessionDao.saveSession(session)
            return Result.success(session)
        } else {
            return Result.failure(
                Exception("The account '$trimmedEmail' is not authorized to use the PrepPapers study portal.")
            )
        }
    }

    private fun getWhitelistedEmailRole(email: String, whitelistedSet: Set<String>): String? {
        val search = email.trim().lowercase()
        
        if (search == "spam.iamshivanshcoder@gmail.com") {
            return "admin"
        }

        for (entry in whitelistedSet) {
            val cleaned = entry.trim().lowercase()
            if (cleaned == search) return "user"
            if (cleaned.startsWith("$search:")) {
                val parsedRole = cleaned.substringAfter(":", "user")
                return if (parsedRole.isNotBlank()) parsedRole else "user"
            }
        }
        return null
    }

    suspend fun logout() {
        sessionDao.clearSession()
    }
}
