package com.example.mobile_dev_project.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.mobile_dev_project.R
import com.example.mobile_dev_project.data.Book
import com.example.mobile_dev_project.data.db.dao.BookDao
import com.example.mobile_dev_project.data.db.dao.ChapterDao
import com.example.mobile_dev_project.data.models.BookEntity
import com.example.mobile_dev_project.data.models.ChapterEntity
import com.example.mobile_dev_project.data.repository.BookRepository
import com.example.mobile_dev_project.data.repository.ChapterRepository
import com.example.mobile_dev_project.data.repository.FileRepository
import com.example.mobile_dev_project.data.repository.HtmlParserRepository
import com.example.mobile_dev_project.data.repository.ReadingProgressRepository
import com.example.mobile_dev_project.ui.theme.MobileDevProjectTheme
import com.example.mobile_dev_project.vm.AppViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.System.currentTimeMillis
import javax.inject.Inject


/**
 * HomeScreenTest
 *
 * UI tests for the HomeScreen composable.
 * Uses Hilt for dependency injection and ComposeTestRule for UI interactions.
 *
 * references:
 * https://developer.android.com/develop/ui/compose/testing
 * https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/run-blocking.html
 * Unit test powerpoint
 * https://developer.android.com/develop/ui/compose/testing/apis
 * https://developer.android.com/develop/ui/compose/testing/synchronization
 */
@HiltAndroidTest
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Inject lateinit var bookDao: BookDao
    @Inject lateinit var chapterDao: ChapterDao
    @Inject lateinit var bookRepository: BookRepository
    @Inject lateinit var chapRepo: ChapterRepository
    @Inject lateinit var progressRepo: ReadingProgressRepository
    @Inject lateinit var fileRepo: FileRepository
    @Inject lateinit var htmlRepo: HtmlParserRepository
    @Inject lateinit var searchRepo: com.example.mobile_dev_project.data.repository.SearchRepository

    lateinit var mockViewModel: AppViewModel

    /**
     * Setup function executed before each test
     */
    @Before
    fun setup() {
        // Inject Hilt dependencies
        hiltRule.inject()
        val now = currentTimeMillis()

        // Use runBlocking instead of runTest for setup
        runBlocking {
            // Insert sample books
            val book1 = BookEntity(
                id = "b1",
                title = "Book 1",
                author = "Me",
                coverImagePath = "/cover1.png",
                dateAdded = now,
                lastAccessed = now,
                totalChapters = 2,
                isDownloaded = true
            )
            val book2 = BookEntity(
                id = "b2",
                title = "Book 2",
                author = "Me",
                coverImagePath = "/cover2.png",
                dateAdded = now,
                lastAccessed = now,
                totalChapters = 2,
                isDownloaded = true
            )
            bookDao.insert(book1)
            bookDao.insert(book2)

            val chapters1 = listOf(
                ChapterEntity(
                    bookId = book1.id,
                    chapterNumber = 1,
                    title = "Chapter 1",
                    htmlFilePath = "/chapter1.html",
                    contentPreview = "First few words of chapter 1..."
                )
            )
            val chapters2 = listOf(
                ChapterEntity(
                    bookId = book2.id,
                    chapterNumber = 1,
                    title = "Chapter 1",
                    htmlFilePath = "/chapter1.html",
                    contentPreview = "First few words of chapter 1..."
                )
            )
            chapterDao.insertAll(chapters1)
            chapterDao.insertAll(chapters2)
        }

        // Create the ViewModel with injected repositories
        mockViewModel = AppViewModel(
            bookRepo = bookRepository,
            chapterRepo = chapRepo,
            progressRepo = progressRepo,
            repository = fileRepo,
            htmlParserRepo = htmlRepo,
            searchRepo = searchRepo
        )
    }
    /**
     * Helper function to render HomeScreen
     * @param onAddBookClick Lambda invoked when add book button is clicked
     * @param onBookClick Lambda invoked when a book is clicked
     */
    private fun setupHomeScreen(
        onAddBookClick: () -> Unit = {},
        onBookClick: (Book) -> Unit = {}
    ) {
        composeTestRule.setContent {
            MobileDevProjectTheme {
                HomeScreen(
                    viewModel = mockViewModel,
                    onAddBookClick = onAddBookClick,
                    onBookClick = onBookClick,
                    context = composeTestRule.activity
                )
            }
        }
        composeTestRule.waitForIdle()
    }
    /** Waits until books are loaded in the UI or timeout occurs */
    private fun waitForBooksToLoad() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Book 1", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    // --- TEST CASES ---

    /** Verify HomeScreen composable is displayed */
    @Test
    fun homeScreen_isDisplayed() {
        setupHomeScreen()
        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }
    /** Verify app logo text is visible */
    @Test
    fun appLogo_isVisible() {
        setupHomeScreen()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.cd_app_logo)
        )
    }
    /** Verify "About App" text is visible */
    @Test
    fun aboutAppText_isVisible() {
        setupHomeScreen()
        val aboutText = composeTestRule.activity.getString(R.string.home_about)
        composeTestRule.onNodeWithText(aboutText).assertIsDisplayed()
    }
    /** Verify "Library" section title is visible */
    @Test
    fun librarySectionTitle_isVisible() {
        setupHomeScreen()
        val title = composeTestRule.activity.getString(R.string.home_section_library)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }
    /** Verify sample books are displayed in the grid */
    @Test
    fun books_areDisplayedInGrid() {
        setupHomeScreen()
        waitForBooksToLoad()
        composeTestRule.onNodeWithText("Book 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Book 2").assertIsDisplayed()
    }
    /** Verify clicking a book triggers the correct callback */
    @Test
    fun clickingBook_invokesCallback() {
        var clickedBookTitle: String? = null

        setupHomeScreen(
            onBookClick = { clickedBookTitle = it.title }
        )
        waitForBooksToLoad()
        composeTestRule.onNodeWithText("Book 1").performClick()

        Assert.assertEquals("Book 1", clickedBookTitle)
    }
    /** Verify Add Book button is visible and clickable */
    @Test
    fun addBookButton_isVisible_andClickable() {
        setupHomeScreen()
        val addBookText = composeTestRule.activity.getString(R.string.home_add_new_book)
        composeTestRule.onNodeWithText(addBookText).assertIsDisplayed()
        composeTestRule.onNodeWithText(addBookText).performClick()
    }
    /** Verify Delete Library button is visible and clickable */
    @Test
    fun deleteLibraryButton_isVisible_andClickable() {
        setupHomeScreen()
        val deleteText = composeTestRule.activity.getString(R.string.delete_library)
        composeTestRule.onNodeWithText(deleteText).assertIsDisplayed()
        composeTestRule.onNodeWithText(deleteText).performClick()
    }
    /** Verify "Last accessed" text is displayed for each book */
    @Test
    fun lastAccessedText_isDisplayedForEachBook() {
        setupHomeScreen()
        waitForBooksToLoad()
        composeTestRule.onAllNodesWithText(
            text = "Last accessed",
            substring = true
        ).assertCountEquals(2)
    }
    /** Verify BookItem displays the correct title */
    @Test
    fun bookItem_displaysTitle() {
        val testBook = Book(
            id = "test_1",
            title = "Test Title",
            author = "Test Author",
            coverUrl = "",
            chapters = emptyList(),
            lastAccessedDate = null,
            coverImagePath = null
        )

        composeTestRule.setContent {
            BookItem(book = testBook, onBookClick = {})
        }
        composeTestRule.onNodeWithText("Test Title").assertExists()
    }
    /** Verify BookCover shows placeholder when no image is provided */
    @Test
    fun bookCover_displaysPlaceholder_whenNoImage() {
        val testBook = Book(
            id = "no_cover",
            title = "No Cover Book",
            author = "Test Author",
            coverUrl = "",
            chapters = emptyList(),
            lastAccessedDate = null,
            coverImagePath = null
        )

        composeTestRule.setContent {
            BookCover(book = testBook)
        }
        composeTestRule.onNodeWithContentDescription("No Cover Book")
            .assertExists()
    }
    /** Verify LastAccessedText correctly formats the date */
    @Test
    fun lastAccessedText_formatsDateCorrectly() {
        val testDate = 1699920000000L
        val testBook = Book(
            id = "dated_book",
            title = "Dated Book",
            author = "Test Author",
            coverUrl = "",
            chapters = emptyList(),
            lastAccessedDate = testDate,
            coverImagePath = null
        )

        composeTestRule.setContent {
            LastAccessedText(book = testBook)
        }
        composeTestRule.onNodeWithText("Last accessed", substring = true, ignoreCase = true)
            .assertExists()
    }
}