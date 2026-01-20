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
 * ReadingScreenTest
 *
 * UI tests for the ReadingScreen composable.
 * Uses Hilt for dependency injection (same pattern as DownloadScreenTest).
 *
 * References:
 * https://developer.android.com/develop/ui/compose/testing
 */
@HiltAndroidTest
class ReadingScreenTest {

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
    @Inject lateinit var ttsRepo: com.example.mobile_dev_project.data.repository.TtsRepository

    private lateinit var viewModel: AppViewModel
    private lateinit var ttsViewModel: com.example.mobile_dev_project.vm.TTSViewModel

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

        // Create the TTS ViewModel with injected repository
        ttsViewModel = com.example.mobile_dev_project.vm.TTSViewModel(
            ttsRepository = ttsRepo
        )
    }

    /**
     * Helper function to render ReadingScreen
     */
    private fun setupReadingScreen() {
        composeTestRule.setContent {
            MobileDevProjectTheme {
                ReadingScreen(
                    viewModel = viewModel,
                    ttsViewModel = ttsViewModel
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    /**
     * Helper function to create a test book with chapters in the database
     * @return bookId of the created book
     */
    private fun createTestBookWithChapters(): String = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testDir = File(context.cacheDir, "test_reading_${System.currentTimeMillis()}")
        testDir.mkdirs()

        // Create chapters folder
        val chaptersDir = File(testDir, "chapters")
        chaptersDir.mkdirs()

        // Create chapter HTML file with simple content
        val chapterHtml = """
            <!DOCTYPE html>
            <html>
            <head><title>Test Chapter</title></head>
            <body>
                <h2>Chapter 1</h2>
                <p>This is test chapter content for reading.</p>
            </body>
            </html>
        """.trimIndent()

        val chapterFile = File(chaptersDir, "chapter_1.html")
        chapterFile.writeText(chapterHtml)

        // Insert book into database
        val bookId = "test-book-${System.currentTimeMillis()}"
        val book = BookEntity(
            id = bookId,
            title = "Test Book",
            author = "Test Author",
            coverImagePath = "",
            totalChapters = 1,
            isDownloaded = true
        )
        bookRepository.insert(book)

        // Insert chapter into database
        val chapter = ChapterEntity(
            bookId = bookId,
            chapterNumber = 1,
            title = "Chapter 1",
            htmlFilePath = chapterFile.absolutePath,
            contentPreview = "This is test chapter content"
        )
        chapterRepository.insertAll(listOf(chapter))

        return@runBlocking bookId
    }

    // --- TEST CASES ---

    /**
     * Verify ReadingScreen renders without crash when no book is selected
     */
    @Test
    fun readingScreen_displaysNothingWhenNoBook() {
        // Given: No book selected (currentBookId is null by default)

        // When: Reading screen is displayed
        setupReadingScreen()

        // Then: No crash occurs (screen handles missing book gracefully)
        // The screen shows an empty Box when there's no book
        // Test passes if no exception is thrown
    }

    /**
     * Verify ReadingScreen displays correctly when a book is selected
     */
    @Test
    fun readingScreen_isDisplayed_whenBookIsSelected() {
        val bookId = createTestBookWithChapters()

        viewModel.selectBook(bookId)
        composeTestRule.waitForIdle()

        setupReadingScreen()

        composeTestRule.onNodeWithTag("reading_screen").assertExists()
    }

    /**
     * Verify TTS control bar is visible
     */
    @Test
    fun ttsControlBar_isVisible() {
        val bookId = createTestBookWithChapters()
        viewModel.selectBook(bookId)
        composeTestRule.waitForIdle()

        setupReadingScreen()

        composeTestRule.onNodeWithContentDescription("Play").assertExists()
        composeTestRule.onNodeWithContentDescription("Stop").assertExists()
    }


    /**
     * Verify TTS play button can be clicked
     */
    @Test
    fun ttsPlayButton_isClickable() {
        val bookId = createTestBookWithChapters()
        viewModel.selectBook(bookId)
        composeTestRule.waitForIdle()

        setupReadingScreen()

        composeTestRule.onNodeWithContentDescription("Play").assertExists()
        composeTestRule.onNodeWithContentDescription("Play").performClick()
    }
}
