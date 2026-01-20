package com.example.mobile_dev_project.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile_dev_project.data.db.AppDatabase
import com.example.mobile_dev_project.data.db.dao.BookDao
import com.example.mobile_dev_project.data.db.dao.ChapterDao
import com.example.mobile_dev_project.data.models.BookEntity
import com.example.mobile_dev_project.data.models.ChapterEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ChapterDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var bookDao: BookDao
    private lateinit var chapterDao: ChapterDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        bookDao = db.bookDao()
        chapterDao = db.chapterDao()
    }

    @After
    fun tearDown() {
        db.close()
    }


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

    private fun ch(bookId: String, num: Int, title: String) = ChapterEntity(
        id = num,
        bookId = bookId,
        chapterNumber = num,
        title = title,
        htmlFilePath = "file:///$bookId/$num.html"
    )

    @Test
    fun insertAll_and_query_sortedBy_chapterNumber() = runBlocking {
        bookDao.insert(book("b1", "Book One"))

        val list = listOf(
            ch("b1", 3, "C3"),
            ch("b1", 1, "C1"),
            ch("b1", 2, "C2")
        )
        chapterDao.insertAll(list)

        val chapters = chapterDao.getChaptersForBook("b1").first()
        Assert.assertEquals(listOf(1, 2, 3), chapters.map { it.chapterNumber })
    }

    @Test
    fun flow_emits_on_insert_using_firstTwice() = runBlocking {
        bookDao.insert(book("b2", "Book Two"))

        val before = chapterDao.getChaptersForBook("b2").first()
        Assert.assertTrue(before.isEmpty())

        chapterDao.insertAll(listOf(ch("b2", 1, "Intro")))

        val after = chapterDao.getChaptersForBook("b2").first()
        Assert.assertEquals(1, after.size)
        Assert.assertEquals("Intro", after[0].title)
        Assert.assertEquals(1, after[0].chapterNumber)
    }
}
