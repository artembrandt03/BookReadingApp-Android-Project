package com.example.mobile_dev_project.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile_dev_project.data.db.AppDatabase
import com.example.mobile_dev_project.data.db.dao.BookDao
import com.example.mobile_dev_project.data.db.dao.ReadingProgressDao
import com.example.mobile_dev_project.data.models.BookEntity
import com.example.mobile_dev_project.data.models.ReadingProgressEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ReadingProgressDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var bookDao: BookDao
    private lateinit var dao: ReadingProgressDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        bookDao = db.bookDao()
        dao = db.readingProgressDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    //Helper to create sample BookEntity (so progress has a valid FK target if enforced)
    private fun book(
        id: String,
        title: String,
        isDownloaded: Boolean = true,
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

    //Helper to create a ReadingProgressEntity
    private fun progress(bookId: String, chapterIndex: Int, scroll: Int) = ReadingProgressEntity(
        bookId = bookId,
        currentChapterIndex = chapterIndex,
        scrollPosition = scroll,
        lastUpdated = System.currentTimeMillis()
    )

    @Test
    fun getProgress_returnsNullThenValueAfterInsert() = runBlocking {
        //Arrange: insert book
        bookDao.insert(book("b1", "Sample Book"))

        //Initially no progress record
        val before = dao.getProgress("b1").first()
        Assert.assertNull(before)

        //Insert new progress
        val record = progress("b1", chapterIndex = 3, scroll = 150)
        dao.insertOrUpdate(record)

        //Now progress should exist
        val after = dao.getProgress("b1").first()
        Assert.assertNotNull(after)
        Assert.assertEquals(3, after!!.currentChapterIndex)
        Assert.assertEquals(150, after.scrollPosition)
    }

    @Test
    fun insertOrUpdate_overwritesExistingRecord() = runBlocking {
        bookDao.insert(book("b2", "Book Two"))

        //Insert initial record
        dao.insertOrUpdate(progress("b2", chapterIndex = 1, scroll = 50))
        var current = dao.getProgress("b2").first()
        Assert.assertEquals(1, current!!.currentChapterIndex)
        Assert.assertEquals(50, current.scrollPosition)

        //Insert updated record for same bookId → should overwrite
        dao.insertOrUpdate(progress("b2", chapterIndex = 2, scroll = 999))
        current = dao.getProgress("b2").first()
        Assert.assertEquals(2, current!!.currentChapterIndex)
        Assert.assertEquals(999, current.scrollPosition)
    }
}