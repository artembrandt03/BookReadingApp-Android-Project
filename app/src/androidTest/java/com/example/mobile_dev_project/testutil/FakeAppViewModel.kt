package com.example.mobile_dev_project.testutil


//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import com.example.mobile_dev_project.data.Book
//import com.example.mobile_dev_project.data.Chapter
//import com.example.mobile_dev_project.data.SearchResult
//import com.example.mobile_dev_project.vm.IAppViewModel
//
//class FakeAppViewModel : IAppViewModel {
//
//    override var books by mutableStateOf(listOf<Book>())
//
//    override var searchQuery by mutableStateOf("")
//    override var searchResults by mutableStateOf(emptyList<SearchResult>())
//    override var currentBookId by mutableStateOf(null as String?)
//    override var chapters by mutableStateOf(emptyList<Chapter>())
//    override var currentChapterIndex by mutableStateOf(0)
//    override var immersive by mutableStateOf(false)
//    override var scrollYByChapter by mutableStateOf(emptyMap<Int, Int>())
//    override var downloadState by mutableStateOf("Idle")
//
//    var wasDownloadCalled by mutableStateOf(false)
//
//    override fun updateSearchQuery(query: String) {
//        searchQuery = query
//    }
//
//    override fun addBookFromUrl(url: String) {
//        wasDownloadCalled = true
//        downloadState = "Success"
//    }
//
//    override fun clearDownloadState() {
//        downloadState = "Idle"
//    }
//
//    override fun loadFakeLibrary() { /* to add logic */ }
//    override fun markAccessed(bookId: String) { /* to add logic */ }
//    override fun selectBook(bookId: String) { currentBookId = bookId }
//    override fun openChapter(index: Int) { currentChapterIndex = index }
//    override fun nextChapter() { currentChapterIndex++ }
//    override fun prevChapter() { currentChapterIndex-- }
//    override fun toggleImmersive() { immersive = !immersive }
//    override fun rememberScrollY(chapterIndex: Int, yPx: Int) {
//       /* to add logic */
//    }
//    override fun clearSearch() {
//        searchQuery = ""
//        searchResults = emptyList()
//    }
//}
