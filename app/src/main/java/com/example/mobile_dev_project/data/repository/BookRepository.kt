package com.example.mobile_dev_project.data.repository

import com.example.mobile_dev_project.data.db.dao.BookDao
import com.example.mobile_dev_project.data.models.BookEntity
import kotlinx.coroutines.flow.Flow

class BookRepository(private val dao: BookDao) {
    suspend fun insert(book: BookEntity) = dao.insert(book)
    suspend fun updateLastAccessed(bookId: String, timestamp: Long) = dao.updateLastAccessed(bookId, timestamp)
    suspend fun existsDownloadedByTitle(title: String) = dao.existsDownloadedByTitle(title)
    suspend fun deleteById(bookId: String) = dao.deleteById(bookId)
    suspend fun deleteAllDownloaded() = dao.deleteAllDownloaded()
    fun getAllBooks(): Flow<List<BookEntity>> = dao.getAllBooks()
}