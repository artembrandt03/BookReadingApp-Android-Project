package com.example.mobile_dev_project.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mobile_dev_project.R
import com.example.mobile_dev_project.data.DownloadState
import com.example.mobile_dev_project.data.db.dao.BookDao
import com.example.mobile_dev_project.data.db.dao.ChapterDao
import com.example.mobile_dev_project.data.repository.BookRepository
import com.example.mobile_dev_project.data.repository.ChapterRepository
import com.example.mobile_dev_project.data.repository.FileRepository
import com.example.mobile_dev_project.data.repository.HtmlParserRepository
import com.example.mobile_dev_project.data.repository.ReadingProgressRepository
import com.example.mobile_dev_project.ui.theme.MobileDevProjectTheme
import com.example.mobile_dev_project.vm.AppViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * DownloadScreenTest
 *
 * UI tests for the DownloadScreen composable.
 * Uses Hilt for dependency injection and ComposeTestRule for UI interactions.
 *
 * References:
 * https://developer.android.com/develop/ui/compose/testing
 * https://developer.android.com/develop/ui/compose/testing/apis
 * https://developer.android.com/develop/ui/compose/testing/synchronization
 */
@HiltAndroidTest
class DownloadScreenTest {

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
        hiltRule.inject()

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
     * Helper function to render DownloadScreen
     */
    private fun setupDownloadScreen() {
        composeTestRule.setContent {
            MobileDevProjectTheme {
                DownloadScreen(viewModel = mockViewModel)
            }
        }
        composeTestRule.waitForIdle()
    }

    // --- TEST CASES ---

    /** Verify DownloadScreen composable is displayed */
    @Test
    fun downloadScreen_isDisplayed() {
        setupDownloadScreen()
        composeTestRule.onNodeWithTag("download_screen").assertIsDisplayed()
    }

    /** Verify download title header is visible */
    @Test
    fun downloadTitle_isVisible() {
        setupDownloadScreen()
        val titleText = composeTestRule.activity.getString(R.string.download_title)
        composeTestRule.onNodeWithText(titleText).assertIsDisplayed()
    }

    /** Verify download subtitle is visible */
    @Test
    fun downloadSubtitle_isVisible() {
        setupDownloadScreen()
        val subtitleText = composeTestRule.activity.getString(R.string.download_and_extract_textbooks)
        composeTestRule.onNodeWithText(subtitleText).assertIsDisplayed()
    }

    /** Verify URL text field is displayed */
    @Test
    fun urlTextField_isDisplayed() {
        setupDownloadScreen()
        val labelText = composeTestRule.activity.getString(R.string.enter_book_url)
        composeTestRule.onNodeWithText(labelText).assertIsDisplayed()
    }

    /** Verify download button is displayed */
    @Test
    fun downloadButton_isDisplayed() {
        setupDownloadScreen()
        val buttonText = composeTestRule.activity.getString(R.string.download_button)
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
    }

    /** Verify download button is disabled when URL is empty */
    @Test
    fun downloadButton_isDisabled_whenUrlIsEmpty() {
        setupDownloadScreen()
        val buttonText = composeTestRule.activity.getString(R.string.download_button)
        composeTestRule.onNodeWithText(buttonText).assertIsNotEnabled()
    }

    /** Verify text can be entered in URL field */
    @Test
    fun urlTextField_acceptsTextInput() {
        setupDownloadScreen()
        val labelText = composeTestRule.activity.getString(R.string.enter_book_url)
        val testUrl = "https://example.com/book.epub"

        composeTestRule.onNodeWithText(labelText)
            .performTextInput(testUrl)

        composeTestRule.waitForIdle()

        // Verify the text was entered by checking the viewmodel state
        Assert.assertEquals(testUrl, mockViewModel.searchQuery)
    }

    /** Verify download button becomes enabled after entering URL */
    @Test
    fun downloadButton_isEnabled_whenUrlIsEntered() {
        setupDownloadScreen()
        val labelText = composeTestRule.activity.getString(R.string.enter_book_url)
        val buttonText = composeTestRule.activity.getString(R.string.download_button)
        val testUrl = "https://example.com/book.epub"

        composeTestRule.onNodeWithText(labelText)
            .performTextInput(testUrl)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(buttonText).assertIsEnabled()
    }

    /** Verify download button is clickable */
    @Test
    fun downloadButton_isClickable() {
        setupDownloadScreen()
        val labelText = composeTestRule.activity.getString(R.string.enter_book_url)
        val buttonText = composeTestRule.activity.getString(R.string.download_button)
        val testUrl = "https://example.com/book.epub"

        composeTestRule.onNodeWithText(labelText)
            .performTextInput(testUrl)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(buttonText)
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()
    }

    /** Verify clear button functionality clears URL */
    @Test
    fun clearButton_clearsUrl() {
        setupDownloadScreen()
        val labelText = composeTestRule.activity.getString(R.string.enter_book_url)
        val testUrl = "https://example.com/book.epub"

        composeTestRule.onNodeWithText(labelText)
            .performTextInput(testUrl)

        composeTestRule.waitForIdle()

        mockViewModel.clearSearch()

        composeTestRule.waitForIdle()

        Assert.assertEquals("", mockViewModel.searchQuery)
    }

    /** Verify UrlTextField component displays correctly */
    @Test
    fun urlTextField_component_isDisplayed() {
        val testUrl = "https://test.com"

        composeTestRule.setContent {
            UrlTextField(viewModel = mockViewModel, urlState = testUrl)
        }

        composeTestRule.onNodeWithText(testUrl).assertExists()
    }

    /** Verify UrlTextField component accepts input */
    @Test
    fun urlTextField_component_acceptsInput() {
        composeTestRule.setContent {
            UrlTextField(viewModel = mockViewModel, urlState = mockViewModel.searchQuery)
        }

        val labelText = composeTestRule.activity.getString(R.string.enter_book_url)
        val testUrl = "https://test.com"

        composeTestRule.onNodeWithText(labelText)
            .performTextInput(testUrl)

        composeTestRule.waitForIdle()

        Assert.assertEquals(testUrl, mockViewModel.searchQuery)
    }

    /** Verify DownloadButton component is displayed */
    @Test
    fun downloadButton_component_isDisplayed() {
        composeTestRule.setContent {
            DownloadButton(
                enabled = true,
                onDownloadClick = {}
            )
        }

        val buttonText = composeTestRule.activity.getString(R.string.download_button)
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
    }

    /** Verify DownloadButton component respects enabled state */
    @Test
    fun downloadButton_component_respectsEnabledState() {
        composeTestRule.setContent {
            DownloadButton(
                enabled = false,
                onDownloadClick = {}
            )
        }

        val buttonText = composeTestRule.activity.getString(R.string.download_button)
        composeTestRule.onNodeWithText(buttonText).assertIsNotEnabled()
    }

    /** Verify DownloadButton click callback is triggered */
    @Test
    fun downloadButton_component_triggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            DownloadButton(
                enabled = true,
                onDownloadClick = { clicked = true }
            )
        }

        val buttonText = composeTestRule.activity.getString(R.string.download_button)
        composeTestRule.onNodeWithText(buttonText).performClick()

        Assert.assertTrue(clicked)
    }

    /** Verify StatusMessage component displays loading text */
    @Test
    fun statusMessage_component_displaysLoading() {
        val loadingText = composeTestRule.activity.getString(R.string.loading)

        composeTestRule.setContent {
            StatusMessage(statusMessage = loadingText)
        }

        val loadingInProgress = composeTestRule.activity.getString(R.string.loading_in_progress)
        composeTestRule.onNodeWithText(loadingInProgress).assertExists()
    }

    /** Verify StatusMessage component displays custom message */
    @Test
    fun statusMessage_component_displaysCustomMessage() {
        val customMessage = "Custom status message"

        composeTestRule.setContent {
            StatusMessage(statusMessage = customMessage)
        }

        composeTestRule.onNodeWithText(customMessage).assertExists()
    }

    /** Verify StatusMessage component displays success message */
    @Test
    fun statusMessage_component_displaysSuccess() {
        val successText = composeTestRule.activity.getString(R.string.success)

        composeTestRule.setContent {
            StatusMessage(statusMessage = successText)
        }

        composeTestRule.onNodeWithText(successText).assertExists()
    }

    /** Verify StatusMessage component displays error message */
    @Test
    fun statusMessage_component_displaysError() {
        val errorText = composeTestRule.activity.getString(R.string.error) + ": Test error"

        composeTestRule.setContent {
            StatusMessage(statusMessage = errorText)
        }

        composeTestRule.onNodeWithText("Test error", substring = true).assertExists()
    }

    /** Verify ClearButton component is displayed */
    @Test
    fun clearButton_component_isDisplayed() {
        composeTestRule.setContent {
            ClearButton(viewModel = mockViewModel)
        }

        val clearText = composeTestRule.activity.getString(R.string.clear)
        composeTestRule.onNodeWithText(clearText).assertIsDisplayed()
    }

    /** Verify ClearButton component triggers viewModel methods */
    @Test
    fun clearButton_component_triggersViewModel() {
        composeTestRule.setContent {
            ClearButton(viewModel = mockViewModel)
        }

        mockViewModel.updateSearchQuery("test")

        val clearText = composeTestRule.activity.getString(R.string.clear)
        composeTestRule.onNodeWithText(clearText).performClick()

        composeTestRule.waitForIdle()

        Assert.assertEquals("", mockViewModel.searchQuery)
    }

    /** Verify DownloadBookHeader component is displayed */
    @Test
    fun downloadBookHeader_component_isDisplayed() {
        composeTestRule.setContent {
            DownloadBookHeader()
        }

        val titleText = composeTestRule.activity.getString(R.string.download_title)
        composeTestRule.onNodeWithText(titleText).assertExists()

        val subtitleText = composeTestRule.activity.getString(R.string.download_and_extract_textbooks)
        composeTestRule.onNodeWithText(subtitleText).assertExists()
    }

    /** Verify initial download state is Idle */
    @Test
    fun downloadState_initiallyIdle() {
        setupDownloadScreen()

        composeTestRule.waitForIdle()

        Assert.assertTrue(mockViewModel.downloadState.value is DownloadState.Idle)
    }

    /** Verify clearDownloadState resets to Idle */
    @Test
    fun clearDownloadState_resetsToIdle() {
        setupDownloadScreen()

        mockViewModel.clearDownloadState()

        composeTestRule.waitForIdle()

        Assert.assertTrue(mockViewModel.downloadState.value is DownloadState.Idle)
    }

    /** Verify empty URL keeps download button disabled */
    @Test
    fun downloadButton_staysDisabled_withEmptyUrl() {
        setupDownloadScreen()

        mockViewModel.clearSearch()

        composeTestRule.waitForIdle()

        val buttonText = composeTestRule.activity.getString(R.string.download_button)
        composeTestRule.onNodeWithText(buttonText).assertIsNotEnabled()
    }

    /** Verify URL with only whitespace keeps button disabled */
    @Test
    fun downloadButton_isDisabled_withWhitespaceUrl() {
        setupDownloadScreen()

        val labelText = composeTestRule.activity.getString(R.string.enter_book_url)

        composeTestRule.onNodeWithText(labelText)
            .performTextInput("   ")

        composeTestRule.waitForIdle()

        val buttonText = composeTestRule.activity.getString(R.string.download_button)
        composeTestRule.onNodeWithText(buttonText).assertIsNotEnabled()
    }

    /** Verify multiple text inputs update searchQuery correctly */
    @Test
    fun urlTextField_updatesMultipleTimes() {
        setupDownloadScreen()

        val labelText = composeTestRule.activity.getString(R.string.enter_book_url)

        composeTestRule.onNodeWithText(labelText)
            .performTextInput("first")
        composeTestRule.waitForIdle()
        Assert.assertEquals("first", mockViewModel.searchQuery)

        mockViewModel.clearSearch()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(labelText)
            .performTextInput("second")
        composeTestRule.waitForIdle()
        Assert.assertEquals("second", mockViewModel.searchQuery)
    }
}