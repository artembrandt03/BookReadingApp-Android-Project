package com.example.mobile_dev_project.data.db.dao

import androidx.room.*
import com.example.mobile_dev_project.data.models.ChapterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for interacting with the "chapters" table.
 *
 * Each chapter belongs to a book (foreign key to BookEntity).
 * This DAO lets the app:
 *  - Insert a list of chapters after parsing a book
 *  - Observe all chapters for a specific book using Flow
 */
@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterNumber ASC")
    fun getChaptersForBook(bookId: String): Flow<List<ChapterEntity>>
}