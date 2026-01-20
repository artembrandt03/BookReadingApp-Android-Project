package com.example.mobile_dev_project.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile_dev_project.data.db.AppDatabase
import com.example.mobile_dev_project.data.db.dao.BookDao
import com.example.mobile_dev_project.data.models.BookEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: BookDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.bookDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun book(
        id: String,
        title: String,
        isDownloaded: Boolean = false,
        dateAdded: Long = TimeUnit.SECONDS.toMillis(1),
        lastAccessed: Long? = null
    ) = BookEntity(
        id = id,
        title = title,
        author = "Author",
        coverImagePath = "",
        dateAdded = dateAdded,
        lastAccessed = lastAccessed,
        totalChapters = 0,
        isDownloaded = isDownloaded
    )

    @Test
    fun insert_and_query_ordering_by_lastAccessed_or_dateAdded() = runBlocking {
        //initial empty
        Assert.assertTrue(dao.getAllBooks().first().isEmpty())

        //Insert 2 books
        val b1 = book("1", "Alpha", isDownloaded = true, dateAdded = 1000L, lastAccessed = null)
        val b2 = book("2", "Beta", isDownloaded = true, dateAdded = 2000L, lastAccessed = null)
        dao.insert(b1)
        dao.insert(b2)

        //With no lastAccessed set yet, we order by dateAdded DESC
        val list1 = dao.getAllBooks().first()
        Assert.assertEquals(listOf("2","1"), list1.map { it.id })

        dao.updateLastAccessed("1", 9999L)
        val list2 = dao.getAllBooks().first()
        Assert.assertEquals(listOf("1","2"), list2.map { it.id })
    }

    @Test
    fun existsDownloadedByTitle_true_only_when_downloaded_title_present() = runBlocking {
        dao.insert(book("1", "Clean Architecture", isDownloaded = false))
        Assert.assertFalse(dao.existsDownloadedByTitle("Clean Architecture"))

        dao.insert(book("2", "Clean Architecture", isDownloaded = true))
        Assert.assertTrue(dao.existsDownloadedByTitle("Clean Architecture"))
    }

    @Test
    fun deleteById_and_deleteAllDownloaded() = runBlocking {
        val a = book("a", "A", isDownloaded = true)
        val b = book("b", "B", isDownloaded = true)
        val c = book("c", "C", isDownloaded = false)
        dao.insert(a); dao.insert(b); dao.insert(c)

        //delete one by id
        dao.deleteById("b")
        var list = dao.getAllBooks().first()
        Assert.assertEquals(setOf("a","c"), list.map { it.id }.toSet())

        //delete all downloaded and only 'c' one remains
        dao.deleteAllDownloaded()
        list = dao.getAllBooks().first()
        Assert.assertEquals(listOf("c"), list.map { it.id })
    }

    @Test
    fun flow_emits_on_changes() = runBlocking {
        val flow = dao.getAllBooks()

        val initial = flow.first()
        Assert.assertTrue(initial.isEmpty())

        dao.insert(book("x", "X", isDownloaded = true))

        val after = flow.first()
        Assert.assertEquals(1, after.size)
        Assert.assertEquals("x", after[0].id)
    }
}