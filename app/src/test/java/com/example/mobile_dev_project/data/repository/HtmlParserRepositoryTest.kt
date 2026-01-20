package com.example.mobile_dev_project.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile_dev_project.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.annotation.Config
import java.io.File

/**
 * Unit tests for HtmlParserRepository using in-memory Room database.
 *
 * This approach uses REAL repositories with a test database that exists only in memory.
 * Benefits:
 * - Tests actual database interactions
 * - No need for fake implementations
 * - Fast (in-memory) and isolated
 * - Follows Android testing best practices
 *
 * Tests cover:
 * - Book metadata extraction (title, author)
 * - Chapter extraction and numbering
 * - Database insertion verification
 * - Error handling
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [30])
class HtmlParserRepositoryTest {

    // System under test
    private lateinit var htmlParserRepository: HtmlParserRepository

    // Real repositories with in-memory database
    private lateinit var bookRepository: BookRepository
    private lateinit var chapterRepository: ChapterRepository

    // In-memory database (cleared after each test)
    private lateinit var database: AppDatabase
    private lateinit var context: Context

    // Track test folders for cleanup
    private val testFolders = mutableListOf<File>()

    /**
     * Set up in-memory database and real repositories before each test.
     *
     * Creates a fresh database that exists only in memory and is destroyed after the test.
     * This ensures each test starts with a clean state.
     */
    @Before
    fun setup() {
        // Get Android test context
        context = ApplicationProvider.getApplicationContext()

        // Create in-memory database (data disappears after test)
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // OK for tests
            .build()

        // Create REAL repositories with test database
        bookRepository = BookRepository(database.bookDao())
        chapterRepository = ChapterRepository(database.chapterDao())

        // Inject real repositories into HtmlParserRepository
        htmlParserRepository = HtmlParserRepository(
            context = context,
            bookRepository = bookRepository,
            chapterRepository = chapterRepository
        )
    }

    /**
     * Clean up after each test.
     *
     * Closes database and deletes test folders.
     */
    @After
    fun tearDown() {
        database.close()

        testFolders.forEach { folder ->
            if (folder.exists()) {
                folder.deleteRecursively()
            }
        }
        testFolders.clear()
    }

    // ==================== Helper Functions ====================

    /**
     * Create a test book folder with custom HTML content.
     *
     * @param htmlContent The complete HTML content to write
     * @return File pointing to the test book folder
     */
    private fun createTestBookWithHtml(htmlContent: String): File {
        val testDir = File(context.cacheDir, "test_book_${System.currentTimeMillis()}")
        testDir.mkdirs()
        testFolders.add(testDir)

        File(testDir, "test.html").writeText(htmlContent)
        return testDir
    }

    /**
     * Create a test book folder with basic HTML content.
     *
     * @return File pointing to the test book folder
     */
    private fun createTestBookFolder(): File {
        val testDir = File(context.cacheDir, "test_book_${System.currentTimeMillis()}")
        testDir.mkdirs()
        testFolders.add(testDir)

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test Book</title>
                <meta name="dc.creator" content="Test Author">
            </head>
            <body>
                <h2>Chapter I</h2>
                <p>This is the first chapter content.</p>
                <p>It has multiple paragraphs for testing.</p>
            </body>
            </html>
        """.trimIndent()

        File(testDir, "test.html").writeText(htmlContent)
        return testDir
    }

    /**
     * Create a test book folder with a custom title.
     *
     * @param title The title to use in the <title> tag
     * @return File pointing to the test book folder
     */
    private fun createTestBookWithTitle(title: String): File {
        val testDir = File(context.cacheDir, "test_book_${System.currentTimeMillis()}")
        testDir.mkdirs()
        testFolders.add(testDir)

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>$title</title>
                <meta name="dc.creator" content="Test Author">
            </head>
            <body>
                <h2>Chapter I</h2>
                <p>Chapter content here.</p>
            </body>
            </html>
        """.trimIndent()

        File(testDir, "test.html").writeText(htmlContent)
        return testDir
    }

    /**
     * Create a test book folder with a custom author in DC meta tag.
     *
     * @param author The author name to use in the dc.creator meta tag
     * @return File pointing to the test book folder
     */
    private fun createTestBookWithAuthor(author: String): File {
        val testDir = File(context.cacheDir, "test_book_${System.currentTimeMillis()}")
        testDir.mkdirs()
        testFolders.add(testDir)

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test Book</title>
                <meta name="dc.creator" content="$author">
            </head>
            <body>
                <h2>Chapter I</h2>
                <p>Chapter content here.</p>
            </body>
            </html>
        """.trimIndent()

        File(testDir, "test.html").writeText(htmlContent)
        return testDir
    }

    /**
     * Create a test book folder with multiple chapters.
     *
     * @param chapterCount Number of chapters to create
     * @return File pointing to the test book folder
     */
    private fun createTestBookWithChapters(chapterCount: Int): File {
        val testDir = File(context.cacheDir, "test_book_${System.currentTimeMillis()}")
        testDir.mkdirs()
        testFolders.add(testDir)

        val chapters = (1..chapterCount).joinToString("\n") { i ->
            """
                <h2>Chapter $i</h2>
                <p>This is the content of chapter $i.</p>
                <p>It has multiple paragraphs for testing.</p>
            """.trimIndent()
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test Book with Multiple Chapters</title>
                <meta name="dc.creator" content="Test Author">
            </head>
            <body>
                $chapters
            </body>
            </html>
        """.trimIndent()

        File(testDir, "test.html").writeText(htmlContent)
        return testDir
    }

    // ==================== Tests ====================

    /**
     * Test that parseBook successfully parses a valid HTML file and returns a book ID.
     *
     * This verifies:
     * - Parsing completes without errors
     * - A valid book ID is returned
     * - Book is inserted into the database
     */
    @Test
    fun parseBook_validHtmlFile_returnsBookId() = runTest {
        // Given: A test book folder with HTML file
        val testFolder = createTestBookFolder()

        // When: Parse the book
        val bookId = htmlParserRepository.parseBook(
            bookFolderPath = testFolder.absolutePath,
            mainHtmlFileName = "test.html"
        )

        // Then: Book ID should be returned
        assertNotNull("Book ID should not be null", bookId)

        // Verify book was inserted into database
        val books = bookRepository.getAllBooks().first()
        assertEquals("Should have 1 book in database", 1, books.size)
        assertEquals("Book ID should match", bookId, books[0].id)
    }

    /**
     * Test that book title is correctly extracted from HTML <title> tag.
     *
     * This verifies:
     * - Title extraction from <title> tag works
     * - Title is stored correctly in BookEntity
     */
    @Test
    fun parseBook_extractsTitle_correctly() = runTest {
        // Given: A book with specific title
        val expectedTitle = "The Art of War"
        val testFolder = createTestBookWithTitle(expectedTitle)

        // When: Parse the book
        val bookId = htmlParserRepository.parseBook(
            bookFolderPath = testFolder.absolutePath,
            mainHtmlFileName = "test.html"
        )

        // Then: Title should be extracted correctly
        val books = bookRepository.getAllBooks().first()
        val book = books.find { it.id == bookId }
        assertNotNull("Book should be in database", book)
        assertEquals("Title should match", expectedTitle, book?.title)
    }

    /**
     * Test that book author is correctly extracted from DC meta tag.
     *
     * This verifies:
     * - Author extraction from dc.creator meta tag works
     * - Author is stored correctly in BookEntity
     */
    @Test
    fun parseBook_extractsAuthor_correctly() = runTest {
        // Given: A book with specific author
        val expectedAuthor = "Sun Tzu"
        val testFolder = createTestBookWithAuthor(expectedAuthor)

        // When: Parse the book
        val bookId = htmlParserRepository.parseBook(
            bookFolderPath = testFolder.absolutePath,
            mainHtmlFileName = "test.html"
        )

        // Then: Author should be extracted correctly
        val books = bookRepository.getAllBooks().first()
        val book = books.find { it.id == bookId }
        assertNotNull("Book should be in database", book)
        assertEquals("Author should match", expectedAuthor, book?.author)
    }

    /**
     * Test that chapters are correctly extracted and numbered.
     *
     * This verifies:
     * - Multiple chapters are extracted from HTML
     * - Chapter numbers are correct (1, 2, 3, etc.)
     * - All chapters are inserted into database
     * - Chapter files are created in /chapters folder
     */
    @Test
    fun parseBook_extractsChapters_correctly() = runTest {
        // Given: A book with 3 chapters
        val expectedChapterCount = 3
        val testFolder = createTestBookWithChapters(expectedChapterCount)

        // When: Parse the book
        val bookId = htmlParserRepository.parseBook(
            bookFolderPath = testFolder.absolutePath,
            mainHtmlFileName = "test.html"
        )

        // Then: All chapters should be extracted
        val chapters = chapterRepository.getChaptersForBook(bookId!!).first()
        assertEquals("Should have $expectedChapterCount chapters", expectedChapterCount, chapters.size)

        // Verify chapter numbers are correct (1, 2, 3)
        chapters.forEachIndexed { index, chapter ->
            assertEquals("Chapter ${index + 1} should have correct number", index + 1, chapter.chapterNumber)
        }

        // Verify chapter files were created
        val chaptersFolder = java.io.File(testFolder, "chapters")
        assertTrue("Chapters folder should exist", chaptersFolder.exists())

        for (i in 1..expectedChapterCount) {
            val chapterFile = java.io.File(chaptersFolder, "chapter_$i.html")
            assertTrue("Chapter $i file should exist", chapterFile.exists())
        }
    }

    /**
     * Test that parseBook returns null when given a non-existent folder.
     *
     * This verifies:
     * - Parser handles missing folders gracefully
     * - Returns null instead of crashing
     * - No data is inserted into database
     */
    @Test
    fun parseBook_nonExistentFolder_returnsNull() = runTest {
        // Given: A path to a folder that doesn't exist
        val nonExistentPath = "/path/that/does/not/exist/book_folder"

        // When: Try to parse the book
        val bookId = htmlParserRepository.parseBook(
            bookFolderPath = nonExistentPath,
            mainHtmlFileName = "test.html"
        )

        // Then: Should return null
        assertNull("Should return null for non-existent folder", bookId)

        // Verify no book was inserted into database
        val books = bookRepository.getAllBooks().first()
        assertEquals("Should have 0 books in database", 0, books.size)
    }

    /**
     * Test that parseBook returns null when HTML file doesn't exist.
     *
     * This verifies:
     * - Parser handles missing HTML files gracefully
     * - Returns null even when folder exists but file doesn't
     * - No data is inserted into database
     */
    @Test
    fun parseBook_nonExistentHtmlFile_returnsNull() = runTest {
        // Given: An empty folder (exists) but no HTML file
        val testDir = File(context.cacheDir, "empty_folder_${System.currentTimeMillis()}")
        testDir.mkdirs()
        testFolders.add(testDir)

        // When: Try to parse with a non-existent HTML file
        val bookId = htmlParserRepository.parseBook(
            bookFolderPath = testDir.absolutePath,
            mainHtmlFileName = "does_not_exist.html"
        )

        // Then: Should return null
        assertNull("Should return null when HTML file doesn't exist", bookId)

        // Verify no book was inserted into database
        val books = bookRepository.getAllBooks().first()
        assertEquals("Should have 0 books in database", 0, books.size)
    }

    /**
     * Test that Project Gutenberg branding is removed from titles.
     *
     * This verifies:
     * - cleanTitle() function works correctly
     * - "The Project Gutenberg eBook of" prefix is removed
     * - Book is parsed successfully with cleaned title
     */
    @Test
    fun parseBook_titleWithGutenbergBranding_cleansCorrectly() = runTest {
        // Given: A book with Gutenberg branding in title
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>The Project Gutenberg eBook of Pride and Prejudice</title>
                <meta name="dc.creator" content="Jane Austen">
            </head>
            <body>
                <h2>Chapter I</h2>
                <p>Chapter content here.</p>
            </body>
            </html>
        """.trimIndent()

        val testFolder = createTestBookWithHtml(htmlContent)

        // When: Parse the book
        val bookId = htmlParserRepository.parseBook(
            bookFolderPath = testFolder.absolutePath,
            mainHtmlFileName = "test.html"
        )

        // Then: Title should be cleaned
        assertNotNull("Book ID should not be null", bookId)

        val books = bookRepository.getAllBooks().first()
        val book = books.find { it.id == bookId }
        assertNotNull("Book should be in database", book)
        assertEquals("Title should be cleaned", "Pride and Prejudice", book?.title)
        assertEquals("Author should be correct", "Jane Austen", book?.author)
    }

    /**
     * Test that BookEntity totalChapters field matches actual chapter count.
     *
     * This verifies:
     * - totalChapters is correctly set in BookEntity
     * - Chapter count matches number of chapters in database
     * - Database consistency between book and chapter records
     */
    @Test
    fun parseBook_totalChaptersCount_matchesActualChapters() = runTest {
        // Given: A book with 5 chapters
        val expectedChapterCount = 5
        val testFolder = createTestBookWithChapters(expectedChapterCount)

        // When: Parse the book
        val bookId = htmlParserRepository.parseBook(
            bookFolderPath = testFolder.absolutePath,
            mainHtmlFileName = "test.html"
        )

        // Then: totalChapters in BookEntity should match actual count
        assertNotNull("Book ID should not be null", bookId)

        val books = bookRepository.getAllBooks().first()
        val book = books.find { it.id == bookId }
        assertNotNull("Book should be in database", book)

        val chapters = chapterRepository.getChaptersForBook(bookId!!).first()

        assertEquals(
            "BookEntity totalChapters should match actual chapter count",
            expectedChapterCount,
            book?.totalChapters
        )
        assertEquals(
            "Actual chapters in database should match expected count",
            expectedChapterCount,
            chapters.size
        )
    }

    /**
     * Test that isDownloaded flag is set to true after parsing.
     *
     * This verifies:
     * - Books parsed from local files are marked as downloaded
     * - isDownloaded field in BookEntity is set correctly
     * - Distinguishes between downloaded books and search results
     */
    @Test
    fun parseBook_setsIsDownloadedFlag_toTrue() = runTest {
        // Given: A local book folder
        val testFolder = createTestBookFolder()

        // When: Parse the book from local files
        val bookId = htmlParserRepository.parseBook(
            bookFolderPath = testFolder.absolutePath,
            mainHtmlFileName = "test.html"
        )

        // Then: Book should be marked as downloaded
        assertNotNull("Book ID should not be null", bookId)

        val books = bookRepository.getAllBooks().first()
        val book = books.find { it.id == bookId }
        assertNotNull("Book should be in database", book)
        assertTrue("isDownloaded should be true for parsed books", book?.isDownloaded == true)
    }
}
