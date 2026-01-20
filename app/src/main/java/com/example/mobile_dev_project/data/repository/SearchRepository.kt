package com.example.mobile_dev_project.data.repository

import com.example.mobile_dev_project.data.Chapter
import com.example.mobile_dev_project.data.SearchResult
import org.jsoup.Jsoup

/**
 * SearchRepository
 * Searches through book chapters and returns results with surrounding context.
 */
class SearchRepository {

    /**
     * Looks for the query in all chapters and returns matches with snippets.
     * Each snippet shows 60 chars before and 140 chars after the match.
     *  *
     * @param chapters List of chapters to search through
     * @param query The search term
     * @return List of SearchResult with chapterIndex and snippet
     */
    fun searchInChapters(chapters: List<Chapter>, query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val results = mutableListOf<SearchResult>()

        chapters.forEachIndexed { index, chapter ->
            // Load content from HTML file
            val htmlContent = try {
                if (chapter.htmlFilePath.isNotEmpty()) {
                    java.io.File(chapter.htmlFilePath).readText()
                } else {
                    chapter.content
                }
            } catch (e: Exception) {
                "" // Skip chapters that can't be read
            }

            if (htmlContent.isBlank()) return@forEachIndexed

            // Strip HTML tags to match JavaScript's innerText
            // Only extract from body to match what's visible in WebView
            val content = try {
                val doc = Jsoup.parse(htmlContent)
                doc.body()?.text() ?: doc.text()
            } catch (e: Exception) {
                htmlContent // Fallback to raw if parsing fails
            }

            var searchStartIndex = 0
            var occurrenceNum = 0

            // Loop through all matches in this chapter
            while (true) {
                val matchIndex = content.indexOf(query, searchStartIndex, ignoreCase = true)
                if (matchIndex == -1) break

                occurrenceNum++

                // Get text around the match for the snippet
                val snippetStart = maxOf(0, matchIndex - 60)
                val snippetEnd = minOf(content.length, matchIndex + query.length + 140)

                var snippet = content.substring(snippetStart, snippetEnd)
                    .replace("\n", " ")
                    .replace("\r", " ")
                    .trim()

                // Add ... if we cut off text at the start or end
                if (snippetStart > 0) snippet = "...$snippet"
                if (snippetEnd < content.length) snippet = "$snippet..."

                results.add(SearchResult(
                    chapterIndex = index,
                    chapterTitle = chapter.title,
                    snippet = snippet,
                    matchPosition = matchIndex,
                    matchLength = query.length,
                    searchQuery = query,
                    occurrenceInChapter = occurrenceNum
                ))

                // Move to next potential match
                searchStartIndex = matchIndex + query.length
            }
        }

        return results
    }
}
