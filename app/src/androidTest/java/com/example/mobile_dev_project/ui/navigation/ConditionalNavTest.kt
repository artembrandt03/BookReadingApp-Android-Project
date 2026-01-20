package com.example.mobile_dev_project.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.System.currentTimeMillis
import javax.inject.Inject

/**
 * ConditionalNavTest
 *
 * UI tests for navigation behavior based on book selection state.
 * Tests conditional navigation flows and screen displays using adaptive navigation.
 *
 * References:
 * https://developer.android.com/develop/ui/compose/testing
 * https://developer.android.com/guide/navigation/navigation-testing
 * https://developer.android.com/develop/ui/compose/testing/synchronization
 */
@HiltAndroidTest
@ExperimentalMaterial3Api
class ConditionalNavTest {

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
    @Inject lateinit var ttsRepo: com.example.mobile_dev_project.data.repository.TtsRepository

    lateinit var mockViewModel: AppViewModel
    lateinit var mockTtsViewModel: com.example.mobile_dev_project.vm.TTSViewModel
    lateinit var testBookId: String

    /**
     * Setup function executed before each test
     */
    @Before
    fun setup() {
        hiltRule.inject()

        runBlocking {
            val now = currentTimeMillis()

            val book1 = BookEntity(
                id = "test_book_1",
                title = "Test Book",
                author = "Test Author",
                coverImagePath = "/cover.png",
                dateAdded = now,
                lastAccessed = now,
                totalChapters = 3,
                isDownloaded = true
            )
            bookDao.insert(book1)
            testBookId = book1.id

            val chapters = listOf(
                ChapterEntity(
                    bookId = book1.id,
                    chapterNumber = 1,
                    title = "Chapter 1",
                    htmlFilePath = "/chapter1.html",
                    contentPreview = "First chapter content..."
                ),
                ChapterEntity(
                    bookId = book1.id,
                    chapterNumber = 2,
                    title = "Chapter 2",
                    htmlFilePath = "/chapter2.html",
                    contentPreview = "Second chapter content..."
                ),
                ChapterEntity(
                    bookId = book1.id,
                    chapterNumber = 3,
                    title = "Chapter 3",
                    htmlFilePath = "/chapter3.html",
                    contentPreview = "Third chapter content..."
                )
            )
            chapterDao.insertAll(chapters)
        }

        mockViewModel = AppViewModel(
            bookRepo = bookRepository,
            chapterRepo = chapRepo,
            progressRepo = progressRepo,
            repository = fileRepo,
            htmlParserRepo = htmlRepo,
            searchRepo = searchRepo
        )

        mockTtsViewModel = com.example.mobile_dev_project.vm.TTSViewModel(
            ttsRepository = ttsRepo
        )
    }

    /**
     * Helper function to set up adaptive navigation content
     * @param hasBookSelected Whether a book is selected for navigation
     * @param windowSize Window size class for adaptive navigation (defaults to Compact)
     */
    private fun setAdaptiveNavigationContent(
        hasBookSelected: Boolean,
        windowSize: WindowWidthSizeClass = WindowWidthSizeClass.Compact
    ) {
        composeTestRule.setContent {
            MobileDevProjectTheme {
                val navController = rememberNavController()
                AdaptiveNavigationApp(
                    windowSizeClass = windowSize,
                    modifier = Modifier,
                    hasBookSelected = hasBookSelected,
                    appViewModel = mockViewModel,
                    navController = navController,
                    ttsViewModel = mockTtsViewModel
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    /**
     * Helper function to wait for books to load from database
     */
    private fun waitForBooksToLoad() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            runBlocking {
                mockViewModel.books.first().isNotEmpty()
            }
        }
    }

    /**
     * Helper function to wait for chapters to load for the selected book
     */
    private fun waitForChaptersToLoad() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            mockViewModel.chapters.isNotEmpty()
        }
    }

    // --- TEST CASES ---

    /** Verify search screen displays when book is selected */
    @Test
    fun searchScreen_displaysWhenBookSelected() {
        waitForBooksToLoad()

        mockViewModel.selectBook(testBookId)

        setAdaptiveNavigationContent(hasBookSelected = true)

        composeTestRule.onNodeWithTag("nav_search").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("search_screen").assertIsDisplayed()
    }

    /** Verify empty search screen displays when no book is selected */
    @Test
    fun searchScreen_displaysEmptyStateWhenNoBookSelected() {
        setAdaptiveNavigationContent(hasBookSelected = false)

        composeTestRule.onNodeWithTag("nav_search").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("empty_search_screen").assertIsDisplayed()
    }

    /** Verify TOC screen displays when book is selected */
    @Test
    fun tocScreen_displaysWhenBookSelected() {
        waitForBooksToLoad()

        mockViewModel.selectBook(testBookId)
        waitForChaptersToLoad()

        setAdaptiveNavigationContent(hasBookSelected = true)

        composeTestRule.onNodeWithTag("nav_toc").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("toc_screen").assertIsDisplayed()
    }

    /** Verify empty state displays in TOC when no book is selected */
    @Test
    fun tocScreen_displaysEmptyStateWhenNoBookSelected() {
        setAdaptiveNavigationContent(hasBookSelected = true)

        composeTestRule.onNodeWithTag("nav_toc").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("No Book is selected", substring = true)
            .assertIsDisplayed()
    }

    /** Verify reading screen displays after selecting a chapter */
    @Test
    fun readingScreen_displaysAfterChapterClick() {
        waitForBooksToLoad()

        mockViewModel.selectBook(testBookId)
        waitForChaptersToLoad()

        setAdaptiveNavigationContent(hasBookSelected = true)

        composeTestRule.onNodeWithTag("nav_toc").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("toc_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("chapter_1")
            .assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("reading_screen").assertIsDisplayed()
    }

    /** Verify home screen is always accessible */
    @Test
    fun homeScreen_isAccessible() {
        setAdaptiveNavigationContent(hasBookSelected = false)

        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    /** Verify download screen is always accessible */
    @Test
    fun downloadScreen_isAccessible() {
        setAdaptiveNavigationContent(hasBookSelected = false)

        composeTestRule.onNodeWithTag("nav_download").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("download_screen").assertIsDisplayed()
    }

    /** Verify navigation between screens maintains state */
    @Test
    fun navigation_maintainsBookSelection() {
        waitForBooksToLoad()

        mockViewModel.selectBook(testBookId)
        waitForChaptersToLoad()

        setAdaptiveNavigationContent(hasBookSelected = false)

        composeTestRule.onNodeWithTag("nav_toc").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("toc_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_search").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_toc").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("toc_screen").assertIsDisplayed()

        assert(mockViewModel.currentBookId == testBookId)
    }

    /** Verify bottom nav reflects current destination in compact mode */
    @Test
    fun bottomNav_reflectsCurrentDestination_compactMode() {
        setAdaptiveNavigationContent(hasBookSelected = false, windowSize = WindowWidthSizeClass.Compact)

        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_download").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("download_screen").assertIsDisplayed()
    }

    /** Verify navigation rail works in medium window size */
    @Test
    fun navigationRail_worksInMediumMode() {
        setAdaptiveNavigationContent(hasBookSelected = false, windowSize = WindowWidthSizeClass.Medium)

        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()

        composeTestRule.onNodeWithText("Download").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("download_screen").assertIsDisplayed()
    }

    /** Verify clearing book selection updates navigation state */
    @Test
    fun clearingBookSelection_updatesNavigationState() {
        waitForBooksToLoad()

        mockViewModel.selectBook(testBookId)
        waitForChaptersToLoad()

        setAdaptiveNavigationContent(hasBookSelected = true)

        composeTestRule.onNodeWithTag("nav_toc").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("toc_screen").assertIsDisplayed()

        mockViewModel.clearSelectedBook()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_toc").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("No Book is selected", substring = true)
            .assertIsDisplayed()
    }

    /** Verify multiple chapter navigation works correctly */
    @Test
    fun multipleChapterNavigation_worksCorrectly() {
        waitForBooksToLoad()

        mockViewModel.selectBook(testBookId)
        waitForChaptersToLoad()

        setAdaptiveNavigationContent(hasBookSelected = true)

        composeTestRule.onNodeWithTag("nav_toc").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("chapter_2")
            .assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("reading_screen").assertIsDisplayed()
        assert(mockViewModel.currentChapterIndex == 1)
    }

    /** Verify compact window size shows bottom navigation */
    @Test
    fun adaptiveNav_compactShowsBottomNav() {
        setAdaptiveNavigationContent(hasBookSelected = false, windowSize = WindowWidthSizeClass.Compact)
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()
    }

    /** Verify medium window size shows navigation rail */
    @Test
    fun adaptiveNav_mediumShowsNavigationRail() {
        setAdaptiveNavigationContent(hasBookSelected = false, windowSize = WindowWidthSizeClass.Medium)
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }

    /** Verify expanded window size shows permanent drawer */
    @Test
    fun adaptiveNav_expandedShowsPermanentDrawer() {
        setAdaptiveNavigationContent(hasBookSelected = false, windowSize = WindowWidthSizeClass.Expanded)
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }

    /** Verify book selection persists in compact layout */
    @Test
    fun bookSelection_persistsInCompactLayout() {
        waitForBooksToLoad()

        mockViewModel.selectBook(testBookId)
        waitForChaptersToLoad()

        setAdaptiveNavigationContent(hasBookSelected = true, windowSize = WindowWidthSizeClass.Compact)
        composeTestRule.onNodeWithTag("nav_toc").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("toc_screen").assertIsDisplayed()

        // Verify book is still selected
        assert(mockViewModel.currentBookId == testBookId)
    }

    /** Verify navigation works in compact layout */
    @Test
    fun navigation_worksInCompactLayout() {
        setAdaptiveNavigationContent(hasBookSelected = false, windowSize = WindowWidthSizeClass.Compact)

        composeTestRule.onNodeWithTag("nav_download").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("download_screen").assertIsDisplayed()
    }

    /** Verify navigation works in medium layout */
    @Test
    fun navigation_worksInMediumLayout() {
        setAdaptiveNavigationContent(hasBookSelected = false, windowSize = WindowWidthSizeClass.Medium)

        composeTestRule.onNodeWithText("Download").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("download_screen").assertIsDisplayed()
    }

}