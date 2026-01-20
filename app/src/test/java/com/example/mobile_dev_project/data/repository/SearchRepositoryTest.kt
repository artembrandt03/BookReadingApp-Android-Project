package com.example.mobile_dev_project.data.repository

import com.example.mobile_dev_project.data.Chapter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SearchRepository
 * Tests search functionality with context-rich snippets
 */
class SearchRepositoryTest {

    private lateinit var searchRepository: SearchRepository

    @Before
    fun setup() {
        searchRepository = SearchRepository()
    }

    @Test
    fun searchInChapters_emptyQuery_returnsEmptyList() {
        // Given: some chapters
        val chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "Chapter 1",
                content = "This is some test content",
                htmlFilePath = ""
            )
        )

        // When: searching with empty query
        val results = searchRepository.searchInChapters(chapters, "")

        // Then: should return empty list
        assertTrue(results.isEmpty())
    }

    @Test
    fun searchInChapters_blankQuery_returnsEmptyList() {
        // Given: some chapters
        val chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "Chapter 1",
                content = "This is some test content",
                htmlFilePath = ""
            )
        )

        // When: searching with blank query (spaces)
        val results = searchRepository.searchInChapters(chapters, "   ")

        // Then: should return empty list
        assertTrue(results.isEmpty())
    }

    @Test
    fun searchInChapters_singleMatch_returnsOneResult() {
        // Given: a chapter with text
        val chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "Introduction",
                content = "The quick brown fox jumps over the lazy dog",
                htmlFilePath = ""
            )
        )

        // When: searching for a word
        val results = searchRepository.searchInChapters(chapters, "fox")

        // Then: finds exactly one match
        assertEquals(1, results.size)

        // And: has correct chapter index and title
        assertEquals(0, results[0].chapterIndex)
        assertEquals("Introduction", results[0].chapterTitle)

        // And: snippet contains the word we searched for
        assertTrue(results[0].snippet.contains("fox", ignoreCase = true))
    }

    @Test
    fun searchInChapters_uppercaseQuery_findsLowercaseText() {
        // Given: chapter with lowercase text
        val chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "Test",
                content = "this is a test of searching",
                htmlFilePath = ""
            )
        )

        // When: searching with UPPERCASE
        val results = searchRepository.searchInChapters(chapters, "TEST")

        // Then: should find the lowercase match
        assertFalse(results.isEmpty())
        assertTrue(results[0].snippet.contains("test", ignoreCase = true))
    }

    @Test
    fun searchInChapters_longText_snippetHasEllipsis() {
        // Given: chapter with lots of text before and after match
        val longTextBefore = "a".repeat(100)  // 100 a's before
        val longTextAfter = "b".repeat(200)   // 200 b's after
        val content = "$longTextBefore MATCH $longTextAfter"

        val chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "Long",
                content = content,
                htmlFilePath = ""
            )
        )

        // When: searching for word in the middle
        val results = searchRepository.searchInChapters(chapters, "MATCH")

        // Then: snippet starts with ... (text was cut)
        assertTrue(results[0].snippet.startsWith("..."))

        // And: snippet ends with ... (text was cut)
        assertTrue(results[0].snippet.endsWith("..."))
    }

    @Test
    fun searchInChapters_multipleMatches_returnsAllOccurrences() {
        // Given: chapter where word appears three times
        val chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "Chapter",
                content = "The cat sat on the mat. The cat was happy.",
                htmlFilePath = ""
            )
        )

        // When: searching for word that appears multiple times
        val results = searchRepository.searchInChapters(chapters, "the")

        // Then: finds all three matches
        assertEquals(3, results.size)

        // And: all results are from the same chapter
        results.forEach { result ->
            assertEquals(0, result.chapterIndex)
            assertEquals("Chapter", result.chapterTitle)
        }
    }

    @Test
    fun searchInChapters_multipleChapters_findsMatchesInEach() {
        // Given: three chapters, each with the word "book"
        val chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "First",
                content = "This book is good",
                htmlFilePath = ""
            ),
            Chapter(
                id = 2,
                number = 2,
                title = "Second",
                content = "That book is better",
                htmlFilePath = ""
            ),
            Chapter(
                id = 3,
                number = 3,
                title = "Third",
                content = "My book is best",
                htmlFilePath = ""
            )
        )

        // When: searching for the word
        val results = searchRepository.searchInChapters(chapters, "book")

        // Then: finds match in each chapter
        assertEquals(3, results.size)

        // And: each result is from different chapter
        assertEquals(0, results[0].chapterIndex)
        assertEquals("First", results[0].chapterTitle)

        assertEquals(1, results[1].chapterIndex)
        assertEquals("Second", results[1].chapterTitle)

        assertEquals(2, results[2].chapterIndex)
        assertEquals("Third", results[2].chapterTitle)
    }

    @Test
    fun searchInChapters_noMatches_returnsEmptyList() {
        // Given: chapters with some text
        val chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "Chapter One",
                content = "The quick brown fox",
                htmlFilePath = ""
            ),
            Chapter(
                id = 2,
                number = 2,
                title = "Chapter Two",
                content = "jumps over the lazy dog",
                htmlFilePath = ""
            )
        )

        // When: searching for word that doesn't exist
        val results = searchRepository.searchInChapters(chapters, "elephant")

        // Then: returns empty list
        assertTrue(results.isEmpty())
    }

    @Test
    fun searchInChapters_multipleMatches_hasCorrectOccurrenceNumbers() {
        // Given: chapter with word appearing 4 times
        val chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "Test",
                content = "One cat, two cat, three cat, four cat",
                htmlFilePath = ""
            )
        )

        // When: searching for the repeated word
        val results = searchRepository.searchInChapters(chapters, "cat")

        // Then: should find 4 matches
        assertEquals(4, results.size)

        // And: each has correct occurrence number
        assertEquals(1, results[0].occurrenceInChapter)
        assertEquals(2, results[1].occurrenceInChapter)
        assertEquals(3, results[2].occurrenceInChapter)
        assertEquals(4, results[3].occurrenceInChapter)
    }

    @Test
    fun searchInChapters_result_containsSearchQuery() {
        // Given: chapter with text
        val chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "Sample",
                content = "Hello world, this is a test",
                htmlFilePath = ""
            )
        )

        // When: searching for a specific word
        val queryText = "world"
        val results = searchRepository.searchInChapters(chapters, queryText)

        // Then: result stores the search query
        assertEquals(1, results.size)
        assertEquals(queryText, results[0].searchQuery)
    }
}
