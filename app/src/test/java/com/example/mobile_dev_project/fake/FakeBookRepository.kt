package com.example.mobile_dev_project.fake

import com.example.mobile_dev_project.data.models.BookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake implementation of BookRepository for testing purposes.
 * 
 * This allows us to test HtmlParserRepository without needing a real database.
 * All data is stored in memory using MutableLists.
 * 
 * This mimics the same interface so it can be injected in tests.
 */
class FakeBookRepository {
    
    // Store books in memory for verification in tests
    val insertedBooks = mutableListOf<BookEntity>()
    private val booksFlow = MutableStateFlow<List<BookEntity>>(emptyList())
    
    suspend fun insert(book: BookEntity) {
        insertedBooks.add(book)
        booksFlow.value = insertedBooks.toList()
    }
    
    suspend fun updateLastAccessed(bookId: String, timestamp: Long) {
        val index = insertedBooks.indexOfFirst { it.id == bookId }
        if (index >= 0) {
            val book = insertedBooks[index]
            insertedBooks[index] = book.copy(
                id = book.id,
                title = book.title,
                author = book.author,
                coverImagePath = book.coverImagePath,
                totalChapters = book.totalChapters,
                isDownloaded = book.isDownloaded,
                dateAdded = book.dateAdded
            )
            booksFlow.value = insertedBooks.toList()
        }
    }
    
    suspend fun existsDownloadedByTitle(title: String): Boolean {
        return insertedBooks.any { it.title == title && it.isDownloaded }
    }
    
    suspend fun deleteById(bookId: String) {
        insertedBooks.removeIf { it.id == bookId }
        booksFlow.value = insertedBooks.toList()
    }
    
    suspend fun deleteAllDownloaded() {
        insertedBooks.removeIf { it.isDownloaded }
        booksFlow.value = insertedBooks.toList()
    }
    
    fun getAllBooks(): Flow<List<BookEntity>> {
        return booksFlow
    }
    
    // Helper method for tests to clear state between tests
    fun clear() {
        insertedBooks.clear()
        booksFlow.value = emptyList()
    }
    
    // Helper method to verify specific book was inserted
    fun getBookById(bookId: String): BookEntity? {
        return insertedBooks.find { it.id == bookId }
    }
}

