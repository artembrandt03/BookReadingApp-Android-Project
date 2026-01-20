package com.example.mobile_dev_project.data.repository

import com.example.mobile_dev_project.data.db.dao.ChapterDao
import com.example.mobile_dev_project.data.models.ChapterEntity
import kotlinx.coroutines.flow.Flow

class ChapterRepository(private val dao: ChapterDao) {
    suspend fun insertAll(chapters: List<ChapterEntity>) = dao.insertAll(chapters)
    fun getChaptersForBook(bookId: String): Flow<List<ChapterEntity>> = dao.getChaptersForBook(bookId)
}