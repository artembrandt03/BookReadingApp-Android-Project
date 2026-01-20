package com.example.mobile_dev_project.vm

import com.example.mobile_dev_project.data.models.BookEntity
import com.example.mobile_dev_project.data.models.ChapterEntity
import com.example.mobile_dev_project.data.repository.BookRepository
import com.example.mobile_dev_project.data.repository.ChapterRepository
import com.example.mobile_dev_project.data.repository.FileRepository
import com.example.mobile_dev_project.data.repository.HtmlParserRepository
import com.example.mobile_dev_project.data.repository.ReadingProgressRepository
import com.example.mobile_dev_project.data.repository.SearchRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.io.File

class AppViewModelTest {

    private lateinit var bookRepo: BookRepository
    private lateinit var chapterRepo: ChapterRepository
    private lateinit var progressRepo: ReadingProgressRepository
    private lateinit var fileRepo: FileRepository
    private lateinit var parserRepo: HtmlParserRepository
    private lateinit var searchRepo: SearchRepository
    private lateinit var vm: AppViewModel

    @Before
    fun setup() {
        // mock final classes (you already have mockito-inline in test deps)
        bookRepo = mock()
        chapterRepo = mock()
        progressRepo = mock()
        fileRepo = mock()
        parserRepo = mock()
        searchRepo = mock()

        whenever(bookRepo.getAllBooks()).thenReturn(flowOf(emptyList()))

        // If these classes have constructors with args in your project, mock() above is still fine.
        vm = AppViewModel(bookRepo, chapterRepo, progressRepo, fileRepo, parserRepo, searchRepo)
    }

    @Test
    fun markAccessed_updatesRepo() = runTest {
        vm.markAccessed("b2")
        verify(bookRepo).updateLastAccessed(eq("b2"), any())
    }

//    @Test
//    fun clearTextbooksDirectory_triggersFileDelete_andDbCleanup() = runTest {
//        val tmp = kotlin.io.path.createTempDirectory().toFile()
//        vm.clearTextbooksDirectory(tmp)
//        verify(fileRepo).deleteDirectoryContents(tmp)
//        verify(bookRepo).deleteAllDownloaded()
//    }

//    @Test
//    fun selectBook_loadsChapters_flowSubscribed() = runTest {
//        val id = "b3"
//        whenever(chapterRepo.getChaptersForBook(id)).thenReturn(
//            flowOf(listOf(ChapterEntity(1, id, 1, "Intro", "intro.html")))
//        )
//
//        vm.selectBook(id)
//
//        assertEquals(id, vm.currentBookId)
//        verify(chapterRepo).getChaptersForBook(id)
//    }

    @Test
    fun toggleImmersive_flipsFlag() {
        val before = vm.immersive
        vm.toggleImmersive()
        assertEquals(!before, vm.immersive)
    }

    @Test
    fun rememberScrollY_storesValues() {
        vm.rememberScrollY(0, 100)
        assertEquals(100, vm.scrollYByChapter[0])
    }
}