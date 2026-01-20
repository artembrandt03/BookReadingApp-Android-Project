package com.example.mobile_dev_project.data.models
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a user's reading progress for a specific book.
 *
 * Each entry tracks:
 *  - Which book the user is reading (bookId)
 *  - The current chapter they are on
 *  - How far down they have scrolled in that chapter
 *  - When this progress was last updated
 *
 * This table has a UNIQUE index on bookId so each book only has one progress record.
 * It helps implement the "resume reading where you left off" required feature for Milestone 2
 */
@Entity(
    tableName = "reading_progress",
    indices = [Index(value = ["bookId"], unique = true)]
)
data class ReadingProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bookId: String,
    val currentChapterIndex: Int = 0,
    val scrollPosition: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)