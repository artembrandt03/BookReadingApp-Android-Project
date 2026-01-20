package com.example.mobile_dev_project.data.repository

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.mobile_dev_project.data.Chapter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

/**
 * SearchRepositoryTest
 *
 * Tests for the SearchRepository class.
 * Uses Hilt for dependency injection.
 */
@HiltAndroidTest
class SearchRepositoryTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Inject
    lateinit var searchRepository: SearchRepository

    private lateinit var testChapters: List<Chapter>

    /**
     * Setup function executed before each test
     * - Injects Hilt dependencies
     * - Creates test chapters with HTML files
     */
    @Before
    fun setup() = runBlocking {
        hiltRule.inject()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val testDir = File(context.cacheDir, "test_search_${System.currentTimeMillis()}")
        testDir.mkdirs()

        // Create test chapter 1
        val chapter1Html = """
            <!DOCTYPE html>
            <html>
            <body>
                <h2>Chapter 1</h2>
                <p>The quick brown fox jumps over the lazy dog.</p>
                <p>Another fox appears in the forest.</p>
            </body>
            </html>
        """.trimIndent()

        val chapter1File = File(testDir, "chapter1.html")
        chapter1File.writeText(chapter1Html)

        // Create test chapter 2
        val chapter2Html = """
            <!DOCTYPE html>
            <html>
            <body>
                <h2>Chapter 2</h2>
                <p>The dog runs through the meadow.</p>
                <p>A big dog barks loudly.</p>
            </body>
            </html>
        """.trimIndent()

        val chapter2File = File(testDir, "chapter2.html")
        chapter2File.writeText(chapter2Html)

        testChapters = listOf(
            Chapter(
                id = 1,
                title = "Chapter 1",
                content = "",
                htmlFilePath = chapter1File.absolutePath,
                number = 1
            ),
            Chapter(
                id = 2,
                title = "Chapter 2",
                content = "",
                htmlFilePath = chapter2File.absolutePath,
                number = 2
            )
        )
    }

    // --- TEST CASES ---

    /**
     * Verify search returns empty list for blank query
     */
    @Test
    fun search_returnsEmpty_whenQueryIsBlank() {
        composeTestRule.waitForIdle()

        val results = searchRepository.searchInChapters(testChapters, "")

        assert(results.isEmpty())
    }

    /**
     * Verify search finds matches in chapters
     */
    @Test
    fun search_findsMatches_whenQueryExists() {
        composeTestRule.waitForIdle()

        val results = searchRepository.searchInChapters(testChapters, "fox")

        assert(results.isNotEmpty())
        assert(results.size == 2)
    }

    /**
     * Verify search is case insensitive
     */
    @Test
    fun search_isCaseInsensitive() {
        composeTestRule.waitForIdle()

        val results = searchRepository.searchInChapters(testChapters, "FOX")

        assert(results.size == 2)
    }

    /**
     * Verify search returns correct chapter index
     */
    @Test
    fun search_returnsCorrectChapterIndex() {
        composeTestRule.waitForIdle()

        val results = searchRepository.searchInChapters(testChapters, "dog")

        assert(results.any { it.chapterIndex == 0 })
        assert(results.any { it.chapterIndex == 1 })
    }

    /**
     * Verify search snippet contains the query
     */
    @Test
    fun search_snippetContainsQuery() {
        composeTestRule.waitForIdle()

        val results = searchRepository.searchInChapters(testChapters, "fox")

        results.forEach { result ->
            assert(result.snippet.lowercase().contains("fox"))
        }
    }

    /**
     * Verify search returns no results for non-existent query
     */
    @Test
    fun search_returnsEmpty_whenQueryNotFound() {
        composeTestRule.waitForIdle()

        val results = searchRepository.searchInChapters(testChapters, "elephant")

        assert(results.isEmpty())
    }
}