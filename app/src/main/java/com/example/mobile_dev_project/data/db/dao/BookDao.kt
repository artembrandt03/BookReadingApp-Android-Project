package com.example.mobile_dev_project.data.db.dao

import androidx.room.*
import com.example.mobile_dev_project.data.models.BookEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for interacting with the "books" table.
 *
 * Each DAO defines how the app communicates with the database layer.
 * The BookDao provides suspend functions and Flow queries to:
 *  - Insert new or updated books
 *  - Observe the entire bookshelf in real time (live updates with Flow like the teacher wants)
 */
@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity)

    @Query("SELECT * FROM books ORDER BY COALESCE(lastAccessed, dateAdded) DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("UPDATE books SET lastAccessed = :timestamp WHERE id = :bookId")
    suspend fun updateLastAccessed(bookId: String, timestamp: Long)

    @Query("SELECT COUNT(*) > 0 FROM books WHERE title = :title AND isDownloaded = 1")
    suspend fun existsDownloadedByTitle(title: String): Boolean

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteById(bookId: String)

    @Query("DELETE FROM books WHERE isDownloaded = 1")
    suspend fun deleteAllDownloaded()
}