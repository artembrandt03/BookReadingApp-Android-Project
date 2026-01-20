package com.example.mobile_dev_project.vm

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_dev_project.data.SampleData
import com.example.mobile_dev_project.data.Book
import com.example.mobile_dev_project.data.Chapter
import com.example.mobile_dev_project.data.DownloadState
import com.example.mobile_dev_project.data.repository.FileRepository
import com.example.mobile_dev_project.data.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import java.io.File
import com.example.mobile_dev_project.data.*
import com.example.mobile_dev_project.data.models.BookEntity
import com.example.mobile_dev_project.data.models.ChapterEntity
import com.example.mobile_dev_project.data.models.ReadingProgressEntity
import com.example.mobile_dev_project.data.repository.BookRepository
import com.example.mobile_dev_project.data.repository.ChapterRepository
import com.example.mobile_dev_project.data.repository.ReadingProgressRepository
import com.example.mobile_dev_project.data.repository.HtmlParserRepository
import com.example.mobile_dev_project.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import java.io.IOException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull

/**
 * -------------------------------------------------------
 * AppViewModel (Milestone 2 ver. - Room + Hilt)
 * -------------------------------------------------------
 * One ViewModel to provide state + simple logic for:
 *  - Home (library list + "last accessed")
 *  - Download (fake for now)
 *  - TOC (chapters of the selected book)
 *  - Reading (current chapter, immersive mode toggle, scroll memory)
 *  - Search (basic in-book search)
 *
 *  M2 additions:
 *  - Dependencies injected by Hilt (Repos + FileRepository).
 *  - Books/Chapters/Progress come from Room instead of in-memory lists.
 *
 * (Notes from M1:
 *  - Can be refactored and split into multiple VMs in future, for now it's one singular.
 *  - All data is in-memory; "download" is fake for now for M1.
 *  )
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val chapterRepo: ChapterRepository,
    private val progressRepo: ReadingProgressRepository,
    private val repository: FileRepository,
    private val htmlParserRepo: HtmlParserRepository,
    private val searchRepo: SearchRepository
) : ViewModel(), IAppViewModel {

    // -------------------------------------------------------
    // HOME SCREEN / LIBRARY Section (Now with DB)
    // -------------------------------------------------------
    private fun BookEntity.toUiBook(): Book = Book(
        id = id,
        title = title,
        author = author,
        chapters = emptyList(),
        lastAccessedDate = lastAccessed,
        coverImagePath = coverImagePath
    )
    /** List of books shown on the Home screen */
    //Flow from Room -> StateFlow<List<Book>> for Compose
    private val _books: StateFlow<List<Book>> =
        bookRepo.getAllBooks()
            .map { list -> list.map { it.toUiBook() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override val books: StateFlow<List<Book>> get() = _books

    /**
     * Download status used by the Download screen.
     * Values could be something like "Idle", "Loading...", "Success!", "Error"
     */
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    override val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    /** Generate next Int id for a new book */
    private fun nextBookId(): String = UUID.randomUUID().toString()

    /** To reset download status */
    override fun clearDownloadState() { _downloadState.value = DownloadState.Idle }

    // -------------------------------
    // Download + file integration
    // -------------------------------
    fun downloadTextbooks(urls: List<String>, baseDir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ensureBaseDirExists(baseDir)
                _downloadState.value = DownloadState.Progress(ProgressMessageKey.StartingDownloads)

                //Download each book to its dedicated folder
                for (url in urls) {
                    processDownloadBook(url, baseDir)
                }

                _downloadState.value = DownloadState.Success
            } catch (e: Exception) {
                Log.e("BookDownload", "Exception in downloadTextbooks", e)
                _downloadState.value = DownloadState.Error(ErrorMessageKey.UnknownError(e.message ?: "Unknown error")
                )
                e.printStackTrace()
            }
        }
    }

    /* Ensures the base directory exists */
    private fun ensureBaseDirExists(baseDir: File) {
        if (!baseDir.exists()) {
            baseDir.mkdirs()
            Log.d("BookDownload", "Created base directory: ${baseDir.absolutePath}")
        }
    }
    /* Check if a book exist */
    private fun checkIfBookExist(folderName: String, folder: File): Boolean {
        val alreadyExists = _books.value.any {
            it.title.equals(folderName, ignoreCase = true)
        }
        val folderHasContent = folder.exists() &&
                folder.listFiles()?.let { files ->
                    files.isNotEmpty() && files.any { !it.name.endsWith(".zip") }
                } == true

        return alreadyExists || folderHasContent
    }

    /* Handle downloading book processing */
    private suspend fun processDownloadBook(url: String, baseDir: File){
        val fileName = url.substringAfterLast("/")
        val folderName = fileName.substringBeforeLast(".zip")

        val normalizedTitle = folderName
            .replace('_', ' ')
            .replace('-', ' ')
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        if (bookRepo.existsDownloadedByTitle(normalizedTitle)) {
            _downloadState.value = DownloadState.Progress(ProgressMessageKey.BookAlreadyInLibrary(normalizedTitle))
            return
        }

        val folder = File(baseDir, folderName)

        if (checkIfBookExist(folderName, folder)) {
            _downloadState.value = DownloadState.Progress(
                ProgressMessageKey.BookAlreadyExists(folderName)
            )
            return
        }

        if (!folder.exists()) {
            folder.mkdirs()
            Log.d("BookDownload", "Created folder: ${folder.absolutePath}")
        }
        val zipFile = repository.createFile(folder, fileName)
        _downloadState.value = DownloadState.Progress(ProgressMessageKey.Downloading(fileName))
        val success = repository.downloadFile(url, zipFile)
        if(success){
            extractAndProcessBook(zipFile, folder, folderName, fileName)
        } else {
            _downloadState.value = DownloadState.Error(ErrorMessageKey.DownloadFailed(fileName))
            throw IOException("Download failed for $fileName")
        }
    }

    /* Unzips the book file */
    private fun unzipBook(zipFile: File, folder: File, fileName: String) {
        _downloadState.value = DownloadState.Progress(ProgressMessageKey.Unzipping(fileName))
        repository.unzipFile(zipFile, folder)
        Log.d("BookDownload", "Unzipped $fileName to ${folder.absolutePath}")
    }

    /* Extract zip and Process */
    // Make this suspend so it can call repo/DAO on IO dispatcher
    private suspend fun extractAndProcessBook(
        zipFile: File,
        folder: File,
        folderName: String,
        fileName: String
    ) {
        if (fileName.endsWith(".zip", ignoreCase = true)) {
            unzipBook(zipFile, folder, fileName)
        }

        _downloadState.value = DownloadState.Progress(ProgressMessageKey.Parsing(folderName))

        val htmlFile = folder.walkTopDown()
            .firstOrNull { it.isFile && it.extension.lowercase() == "html" }

        if (htmlFile != null) {
            Log.d("BookDownload", "Found HTML file: ${htmlFile.name}")

            // parseBook will:
            // 1. Extract title, author, chapters from HTML
            // 2. Create BookEntity with proper metadata
            // 3. Create ChapterEntity objects
            // 4. Save everything to database
            val bookId = htmlParserRepo.parseBook(
                bookFolderPath = folder.absolutePath,
                mainHtmlFileName = htmlFile.name
            )

            if (bookId != null) {
                Log.d("BookDownload", "Successfully parsed book with ID: $bookId")
                viewModelScope.launch(Dispatchers.IO) {
                    bookRepo.updateLastAccessed(bookId, System.currentTimeMillis())
                }
                _downloadState.value = DownloadState.Progress(ProgressMessageKey.Completed(folderName))
            } else {
                Log.e("BookDownload", "Failed to parse HTML for $folderName")
                _downloadState.value = DownloadState.Error(ErrorMessageKey.ParseFailed(folderName))
            }
        } else {
            Log.e("BookDownload", "No HTML file found in $folderName")
            _downloadState.value = DownloadState.Error(ErrorMessageKey.NoHtmlFound(folderName))
        }
    }

    // -------------------------------------------------------
    // DELETING BOOK
    // -------------------------------------------------------
        //REFACTORED POST MERGE
        fun clearTextbooksDirectory(baseDir: File) {
            try {
                if (!baseDir.exists()) {
                    Log.w("BookDelete", "Directory doesn't exist: ${baseDir.absolutePath}")
                    return
                }
                repository.deleteDirectoryContents(baseDir)

                viewModelScope.launch(Dispatchers.IO) {
                    bookRepo.deleteAllDownloaded()
                }

                viewModelScope.launch(Dispatchers.Main) {
                    clearSelectedBook()
                    clearDownloadState()
                }
            } catch (e: Exception) {
                Log.e("BookDelete", "Error clearing directory contents", e)
                e.printStackTrace()
            }
        }

    // -------------------------------------------------------
    // TOC / READING (now with DB & local cache)
    // -------------------------------------------------------

    /** set when user taps a book on Home */
    override var currentBookId by mutableStateOf<String?>(null)
        private set

    //cache chapters as UI model
    private val _chaptersCache = MutableStateFlow<List<Chapter>>(emptyList())
    override val chapters: List<Chapter> get() = _chaptersCache.value

    /** Index of the chapter currently being read (starting from 0) */
    override var currentChapterIndex by mutableStateOf(0)
        private set

    /** Whether immersive mode is ON */
    override var immersive by mutableStateOf(false)
        private set

    /** stores vertical scroll position per chapter so we can restore it */
    override var scrollYByChapter by mutableStateOf<Map<Int, Int>>(emptyMap())
        private set

    //Read progress stream if needed from screens
    fun progress(bookId: String) = progressRepo.getProgress(bookId)

    //Update last accessed in DB
    override fun markAccessed(bookId: String) {
        val now = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            bookRepo.updateLastAccessed(bookId, now)
        }
    }

    /**
     * HOME → TOC: when a book card is tapped we call this and then navigate to TOC
     * - Ensures chapters exist
     * - Resets immersive mode + scroll memory
     * - Updates last accessed time for Home display
     */
    private var chaptersJob: Job? = null

    override fun selectBook(bookId: String) {
        if (currentBookId == bookId) return
        currentBookId = bookId
        currentChapterIndex = 0
        immersive = false
        markAccessed(bookId)
        scrollYByChapter = emptyMap()

        //now we pull last saved position for the current book
        restoreReadingProgress(bookId)

        chaptersJob?.cancel()
        chaptersJob = viewModelScope.launch {
            chapterRepo.getChaptersForBook(bookId)
                .map { it.map { e -> e.toUiChapter() } }
                .collect { _chaptersCache.value = it }
        }
    }

    private fun ChapterEntity.toUiChapter(): Chapter = Chapter(
        id = this.id,
        number = this.chapterNumber,
        title = this.title,
        content = "", //we can load HTML by path when rendering
        htmlFilePath = this.htmlFilePath
    )

    /**
     * TOC/SEARCH → READING: we open a chapter by index!
     * Reading screen will bind to chapters [currentChapterIndex].
     */
    override fun openChapter(index: Int, scrollToPosition: Int?, matchLength: Int?, searchQuery: String?, occurrence: Int?) {
        val total = chapters.size
        if (total == 0) return
        currentChapterIndex = min(max(index, 0), total - 1)
        selectedSearchPosition = scrollToPosition
        selectedSearchLength = matchLength
        selectedSearchQuery = searchQuery
        selectedSearchOccurrence = occurrence
    }

    /** Reading: easy controls to go next/prev chapter */
    override fun nextChapter() = openChapter(currentChapterIndex + 1, null)
    override fun prevChapter() = openChapter(currentChapterIndex - 1, null)

    /** TOC/Reading: tapping the screen toggles immersive mode */
    override fun toggleImmersive() { immersive = !immersive }

    /** Reading: save vertical scroll Y for a chapter so we can restore when returning back to it */
    override fun rememberScrollY(chapterIndex: Int, yPx: Int) {
        scrollYByChapter = scrollYByChapter.toMutableMap().apply { put(chapterIndex, yPx) }
    }

    /** Persist the current reading position for a book */
    fun saveReadingProgress(bookId: String, chapterIndex: Int, scrollY: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = ReadingProgressEntity(
                bookId = bookId,
                currentChapterIndex = chapterIndex,
                scrollPosition = scrollY,
                lastUpdated = System.currentTimeMillis()
            )
            //repo method is saveProgress()
            progressRepo.saveProgress(progress)
        }
    }

    /** Loading previously saved chapter + scroll for this book */
    private fun restoreReadingProgress(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val p = progressRepo.getProgress(bookId).firstOrNull()
            if (p != null) {
                //Switch back to main for state updates used by Compose
                launch(Dispatchers.Main) {
                    currentChapterIndex = p.currentChapterIndex
                    //seed the cache so the reader can scroll to it on first render
                    scrollYByChapter = mapOf(p.currentChapterIndex to p.scrollPosition)
                }
            }
        }
    }

    // -------------------------------------------------------
    // SEARCH (this is inside the book)
    // -------------------------------------------------------

    override var searchQuery by mutableStateOf("")
        private set

    override var searchResults by mutableStateOf(listOf<SearchResult>())
        private set

    // Search highlighting data
    var selectedSearchPosition by mutableStateOf<Int?>(null)
        private set

    var selectedSearchLength by mutableStateOf<Int?>(null)
        private set

    var selectedSearchQuery by mutableStateOf<String?>(null)
        private set

    var selectedSearchOccurrence by mutableStateOf<Int?>(null)
        private set

    fun clearSearchPosition() {
        selectedSearchPosition = null
        selectedSearchLength = null
        selectedSearchQuery = null
        selectedSearchOccurrence = null
    }

    /**
     * Search through chapters using SearchRepository.
     * Returns results with context-rich snippets.
     */
    override fun updateSearchQuery(query: String) {
        searchQuery = query
        searchResults = searchRepo.searchInChapters(chapters, query)
    }

    override fun clearSearch() {
        searchQuery = ""
        searchResults = emptyList()
    }
    fun clearSelectedBook() {
        currentBookId = null
    }

    //initialize vm (temporary for testing)
    init {
        //DB flow will emit automatically
        currentBookId = null
        currentChapterIndex = 0
    }
}