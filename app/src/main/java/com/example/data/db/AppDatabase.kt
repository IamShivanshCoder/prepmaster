package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PdfItemEntity::class,
        BookmarkEntity::class,
        ProgressEntity::class,
        UserSessionEntity::class,
        AttemptHistoryEntity::class,
        DailyChallengeEntity::class,
        StreakStatsEntity::class,
        DailyChallengeQuestionEntity::class
    ],
    version = 4, // Incremented version since we added new entities
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pdfDao(): PdfDao
    abstract fun sessionDao(): SessionDao
    abstract fun statsDao(): StatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prep_master_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
