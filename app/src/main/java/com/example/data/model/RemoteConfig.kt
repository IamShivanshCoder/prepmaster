package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemotePdfItem(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "subject") val subject: String,
    @Json(name = "category") val category: String,
    @Json(name = "exam_type") val examType: String = "pdf",
    @Json(name = "year") val year: String,
    @Json(name = "url") val url: String,
    @Json(name = "size") val size: String = "Unknown Size"
)

@JsonClass(generateAdapter = true)
data class RemoteDailyChallenge(
    @Json(name = "id") val id: String,
    @Json(name = "date") val date: String,
    @Json(name = "subject") val subject: String,
    @Json(name = "topic") val topic: String,
    @Json(name = "question") val question: String,
    @Json(name = "options") val options: List<String>,
    @Json(name = "correct_index") val correctIndex: Int,
    @Json(name = "explanation") val explanation: String,
    @Json(name = "avg_time_minutes") val avgTimeMinutes: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class RemoteConfig(
    @Json(name = "whitelist") val whitelist: List<String> = emptyList(),
    @Json(name = "users") val users: Map<String, String> = emptyMap(),
    @Json(name = "pdfs") val pdfs: List<RemotePdfItem> = emptyList(),
    @Json(name = "daily_challenges") val dailyChallenges: List<RemoteDailyChallenge> = emptyList()
)
