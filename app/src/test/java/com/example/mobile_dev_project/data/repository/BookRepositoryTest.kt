package com.example.mobile_dev_project.data.repository

import com.example.mobile_dev_project.data.db.dao.BookDao
import com.example.mobile_dev_project.data.models.BookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class BookRepositoryTest {

    private lateinit var dao: BookDao
    private lateinit var repo: BookRepository

    @Before
    fun setUp() {
        dao = mock()
        repo = BookRepository(dao)
    }

    @Test
    fun insert_delegatesToDao() {
        runBlocking {
            val book: BookEntity = mock()
            repo.insert(book)
            verify(dao).insert(book)
        }
    }

    @Test
    fun updateLastAccessed_delegatesToDao() {
        runBlocking {
            val id = "id-123"
            val ts = 1_720_000_000_000L
            repo.updateLastAccessed(id, ts)
            verify(dao).updateLastAccessed(id, ts)
        }
    }

    @Test
    fun existsDownloadedByTitle_returnsDaoResult() {
        runBlocking {
            whenever(dao.existsDownloadedByTitle("Clean Architecture")).thenReturn(true)
            val exists = repo.existsDownloadedByTitle("Clean Architecture")
            assertTrue(exists)
            verify(dao).existsDownloadedByTitle("Clean Architecture")
        }
    }

    @Test
    fun deleteById_delegatesToDao() {
        runBlocking {
            repo.deleteById("deadbeef")
            verify(dao).deleteById("deadbeef")
        }
    }

    @Test
    fun deleteAllDownloaded_delegatesToDao() {
        runBlocking {
            repo.deleteAllDownloaded()
            verify(dao).deleteAllDownloaded()
        }
    }

    @Test
    fun getAllBooks_passesThroughFlowFromDao_andEmitsUpdates() {
        runBlocking {
            val b1: BookEntity = mock()
            val b2: BookEntity = mock()
            val daoFlow: Flow<List<BookEntity>> = flow {
                emit(emptyList())
                emit(listOf(b1, b2))
            }
            whenever(dao.getAllBooks()).thenReturn(daoFlow)

            val emissions = repo.getAllBooks().take(2).toList()

            assertEquals(2, emissions.size)
            assertEquals(emptyList<BookEntity>(), emissions[0])
            assertEquals(listOf(b1, b2), emissions[1])
            verify(dao).getAllBooks()
        }
    }
}