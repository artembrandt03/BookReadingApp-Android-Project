package com.example.mobile_dev_project.data.repository
import com.example.mobile_dev_project.data.db.dao.ReadingProgressDao
import com.example.mobile_dev_project.data.models.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ReadingProgressRepositoryTest {

    private lateinit var dao: ReadingProgressDao
    private lateinit var repo: ReadingProgressRepository

    @Before
    fun setUp() {
        dao = mock()
        repo = ReadingProgressRepository(dao)
    }

    @Test
    fun saveProgress_delegatesToDao() {
        runBlocking {
            val progress: ReadingProgressEntity = mock()
            repo.saveProgress(progress)
            verify(dao).insertOrUpdate(progress)
        }
    }

    @Test
    fun getProgress_passesThroughFlowFromDao_andEmitsNullThenValue() {
        runBlocking {
            val bookId = "book-123"
            val progress: ReadingProgressEntity = mock()

            val daoFlow: Flow<ReadingProgressEntity?> = flow {
                emit(null)            //no saved progress yet
                emit(progress)        //progress saved later
            }
            whenever(dao.getProgress(bookId)).thenReturn(daoFlow)

            val emissions = repo.getProgress(bookId).take(2).toList()

            assertEquals(2, emissions.size)
            assertNull(emissions[0])
            assertEquals(progress, emissions[1])
            verify(dao).getProgress(bookId)
        }
    }
}