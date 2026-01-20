package com.example.mobile_dev_project.data.db.dao

import androidx.room.*
import com.example.mobile_dev_project.data.models.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the "reading_progress" table.
 *
 * Provides methods to:
 *  - Retrieve a user's progress for a specific book
 *  - Insert or update progress records
 *
 * Uses Flow so the Reading screen can reactively update
 * when progress is saved (for example auto-resuming position).
 */
@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId LIMIT 1")
    fun getProgress(bookId: String): Flow<ReadingProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: ReadingProgressEntity)
}