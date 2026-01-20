package com.example.mobile_dev_project.data.repository

import com.example.mobile_dev_project.data.db.dao.ReadingProgressDao
import com.example.mobile_dev_project.data.models.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

class ReadingProgressRepository(private val dao: ReadingProgressDao) {
    fun getProgress(bookId: String): Flow<ReadingProgressEntity?> = dao.getProgress(bookId)
    suspend fun saveProgress(progress: ReadingProgressEntity) = dao.insertOrUpdate(progress)
}