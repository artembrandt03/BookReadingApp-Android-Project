package com.example.mobile_dev_project.vm

import com.example.mobile_dev_project.data.Book
import com.example.mobile_dev_project.data.Chapter
import com.example.mobile_dev_project.data.DownloadState
import com.example.mobile_dev_project.data.SearchResult
import kotlinx.coroutines.flow.StateFlow

interface IAppViewModel {
    // BOOKS
    val books: StateFlow<List<Book>>
    val downloadState: StateFlow<DownloadState>

    fun clearDownloadState()
    fun markAccessed(bookId: String)

    // TOC / READING
    val currentBookId: String?
    val chapters: List<Chapter>
    val currentChapterIndex: Int
    val immersive: Boolean
    val scrollYByChapter: Map<Int, Int>

    fun selectBook(bookId: String)
    fun openChapter(index: Int, scrollToPosition: Int? = null, matchLength: Int? = null, searchQuery: String? = null, occurrence: Int? = null)
    fun nextChapter()
    fun prevChapter()
    fun toggleImmersive()
    fun rememberScrollY(chapterIndex: Int, yPx: Int)

    // SEARCH
    val searchQuery: String
    val searchResults: List<SearchResult>

    fun updateSearchQuery(query: String)
    fun clearSearch()
}