package com.example.mobile_dev_project.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobile_dev_project.ui.screens.components.EmptyTableOfContents
import com.example.mobile_dev_project.ui.screens.components.TableOfContentsList
import com.example.mobile_dev_project.ui.utils.ImmersiveMode
import com.example.mobile_dev_project.vm.AppViewModel
import com.example.mobile_dev_project.vm.IAppViewModel



/**
 * Main Table of Contents Screen
 * Displays the list of chapters for the current book
 * Supports immersive mode (tap to toggle full screen)
 *
 * @param viewModel The shared AppViewModel
 * @param onChapterClick Callback when a chapter is clicked (receives chapter index)
 * @param modifier Modifier for this composable
 *
 */

@Composable
fun TableOfContentsScreen(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel,
    onChapterClick: (chapterIndex: Int) -> Unit,
) {
    val books by viewModel.books.collectAsState()

    val currentBookId = viewModel.currentBookId
    val currentBook = books.find { it.id == currentBookId }
    val chapters = viewModel.chapters
    val isImmersiveMode = viewModel.immersive

    ImmersiveMode(isEnabled = isImmersiveMode) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clickable { viewModel.toggleImmersive() }
                .testTag("toc_screen")
        ) {
            if (currentBook == null || chapters.isEmpty()) {
                EmptyTableOfContents()
            } else {
                TableOfContentsList(
                    book = currentBook,
                    chapters = chapters,
                    onChapterClick = onChapterClick
                )
            }
        }
    }
}

