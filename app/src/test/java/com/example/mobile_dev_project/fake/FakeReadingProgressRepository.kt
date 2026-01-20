package com.example.mobile_dev_project.fake

import com.example.mobile_dev_project.data.models.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeReadingProgressRepository {
    private val flow = MutableStateFlow<ReadingProgressEntity?>(null)
    var lastSaved: ReadingProgressEntity? = null

    fun getProgress(bookId: String): Flow<ReadingProgressEntity?> = flow

    suspend fun insertOrUpdate(progress: ReadingProgressEntity) {
        lastSaved = progress
        flow.value = progress
    }

    fun clear() {
        flow.value = null
        lastSaved = null
    }
}