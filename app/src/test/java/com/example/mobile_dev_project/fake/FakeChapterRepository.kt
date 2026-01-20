package com.example.mobile_dev_project.fake

import com.example.mobile_dev_project.data.models.ChapterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake implementation of ChapterRepository for testing purposes.
 * 
 * This allows us to test HtmlParserRepository without needing a real database.
 * All data is stored in memory using MutableLists.
 * 
 * This mimics the same interface so it can be injected in tests.
 */
class FakeChapterRepository {
    
    // Store chapters in memory for verification in tests
    val insertedChapters = mutableListOf<ChapterEntity>()
    private val chaptersFlow = MutableStateFlow<List<ChapterEntity>>(emptyList())
    
    suspend fun insertAll(chapters: List<ChapterEntity>) {
        insertedChapters.addAll(chapters)
        chaptersFlow.value = insertedChapters.toList()
    }
    
    fun getChaptersForBook(bookId: String): Flow<List<ChapterEntity>> {
        return MutableStateFlow(insertedChapters.filter { it.bookId == bookId })
    }
    
    // Helper method for tests to clear state between tests
    fun clear() {
        insertedChapters.clear()
        chaptersFlow.value = emptyList()
    }
    
    // Helper method to get chapters for a specific book
    fun getChaptersForBookSync(bookId: String): List<ChapterEntity> {
        return insertedChapters.filter { it.bookId == bookId }
    }
    
    // Helper method to get a specific chapter
    fun getChapterByNumber(bookId: String, chapterNumber: Int): ChapterEntity? {
        return insertedChapters.find { it.bookId == bookId && it.chapterNumber == chapterNumber }
    }
}