package com.example.mobile_dev_project.data.repository
import com.example.mobile_dev_project.data.db.dao.ChapterDao
import com.example.mobile_dev_project.data.models.ChapterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ChapterRepositoryTest {

    private lateinit var dao: ChapterDao
    private lateinit var repo: ChapterRepository

    @Before
    fun setUp() {
        dao = mock()
        repo = ChapterRepository(dao)
    }

    @Test
    fun insertAll_delegatesToDao() {
        runBlocking {
            val c1: ChapterEntity = mock()
            val c2: ChapterEntity = mock()
            val list = listOf(c1, c2)

            repo.insertAll(list)

            verify(dao).insertAll(list)
        }
    }

    @Test
    fun getChaptersForBook_passesThroughFlowFromDao_andEmitsUpdates() {
        runBlocking {
            val bookId = "book-123"
            val ch1: ChapterEntity = mock()
            val ch2: ChapterEntity = mock()

            val daoFlow: Flow<List<ChapterEntity>> = flow {
                emit(emptyList())            //initial (no chapters for now)
                emit(listOf(ch1, ch2))       //and this is after insert/parsing
            }
            whenever(dao.getChaptersForBook(bookId)).thenReturn(daoFlow)

            val emissions = repo.getChaptersForBook(bookId).take(2).toList()

            assertEquals(2, emissions.size)
            assertEquals(emptyList<ChapterEntity>(), emissions[0])
            assertEquals(listOf(ch1, ch2), emissions[1])
            verify(dao).getChaptersForBook(bookId)
        }
    }
}