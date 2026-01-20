package com.example.mobile_dev_project.ui.screens

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.mobile_dev_project.data.models.BookEntity
import com.example.mobile_dev_project.data.models.ChapterEntity
import com.example.mobile_dev_project.data.repository.*
import com.example.mobile_dev_project.ui.theme.MobileDevProjectTheme
import com.example.mobile_dev_project.vm.AppViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

/**
 * TableOfContentsScreenTest
 *
 * UI tests for the TableOfContentsScreen composable.
 * Uses Hilt for dependency injection (same pattern as DownloadScreenTest).
 *
 * References:
 * https://developer.android.com/develop/ui/compose/testing
 */
@HiltAndroidTest
class TableOfContentsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Inject lateinit var bookRepository: BookRepository
    @Inject lateinit var chapterRepository: ChapterRepository
    @Inject lateinit var progressRepo: ReadingProgressRepository
    @Inject lateinit var fileRepo: FileRepository
    @Inject lateinit var htmlRepo: HtmlParserRepository
    @Inject lateinit var searchRepo: com.example.mobile_dev_project.data.repository.SearchRepository

    private lateinit var viewModel: AppViewModel

    /**
     * Setup function executed before each test
     * - Injects Hilt dependencies
     * - Initializes the ViewModel with real repositories
     */
    @Before
    fun setup() {
        hiltRule.inject()

        // Create the ViewModel with injected repositories (same as DownloadScreenTest)
        viewModel = AppViewModel(
            bookRepo = bookRepository,
            chapterRepo = chapterRepository,
            progressRepo = progressRepo,
            repository = fileRepo,
            htmlParserRepo = htmlRepo,
            searchRepo = searchRepo
        )
    }

    /**
     * Helper function to render TableOfContentsScreen
     */
    private fun setupTableOfContentsScreen() {
        composeTestRule.setContent {
            MobileDevProjectTheme {
                TableOfContentsScreen(
                    viewModel = viewModel,
                    onChapterClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    /**
     * Helper function to create a test book with chapters in the database
     * @return bookId of the created book
     */
    private fun createTestBookWithChapters(chapterCount: Int = 3): String = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testDir = File(context.cacheDir, "test_toc_${System.currentTimeMillis()}")
        testDir.mkdirs()

        // Create chapters folder
        val chaptersDir = File(testDir, "chapters")
        chaptersDir.mkdirs()

        // Insert book into database
        val bookId = "test-book-${System.currentTimeMillis()}"
        val book = BookEntity(
            id = bookId,
            title = "Test Book",
            author = "Test Author",
            coverImagePath = "",
            totalChapters = chapterCount,
            isDownloaded = true
        )
        bookRepository.insert(book)

        // Insert chapters into database
        val chapters = (1..chapterCount).map { num ->
            // Create chapter HTML file
            val chapterHtml = """
                <!DOCTYPE html>
                <html>
                <head><title>Chapter $num</title></head>
                <body>
                    <h2>Chapter $num</h2>
                    <p>Content of chapter $num</p>
                </body>
                </html>
            """.trimIndent()

            val chapterFile = File(chaptersDir, "chapter_$num.html")
            chapterFile.writeText(chapterHtml)

            ChapterEntity(
                bookId = bookId,
                chapterNumber = num,
                title = "Chapter $num",
                htmlFilePath = chapterFile.absolutePath,
                contentPreview = "Content of chapter $num"
            )
        }
        chapterRepository.insertAll(chapters)

        return@runBlocking bookId
    }

    // --- TEST CASES ---

    /**
     * Verify TableOfContentsScreen displays empty state when no book is selected
     */
    @Test
    fun tableOfContentsScreen_displaysEmptyStateWhenNoBook() {
        // Given: No book selected (currentBookId is null by default)

        // When: Screen is displayed
        setupTableOfContentsScreen()

        // Then: Empty state message is shown
        composeTestRule.onNodeWithText("No Book is selected")
            .assertIsDisplayed()
    }

    /**
     * Verify TableOfContentsScreen displays chapters when book is selected
     */
    @Test
    fun tableOfContentsScreen_displaysChapterListWhenBookHasChapters() {
        // Given: A book with 3 chapters
        val bookId = createTestBookWithChapters(chapterCount = 3)

        // When: Book is selected and screen is displayed
        viewModel.selectBook(bookId)
        composeTestRule.waitForIdle()

        setupTableOfContentsScreen()

        // Then: Book title is displayed
        composeTestRule.onNodeWithText("Test Book")
            .assertIsDisplayed()

        // And: All chapters are displayed
        composeTestRule.onNodeWithText("Chapter 1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Chapter 2")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Chapter 3")
            .assertIsDisplayed()
    }
}
