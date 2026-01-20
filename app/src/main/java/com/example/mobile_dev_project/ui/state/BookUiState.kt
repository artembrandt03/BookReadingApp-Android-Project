package com.example.mobile_dev_project.ui.state

import com.example.mobile_dev_project.data.Book

/**
 *
 * @property currentBook The currently selected book (null if none selected)
 * @property currentChapterIndex Index of the current chapter being viewed (0-based)
 * @property isImmersiveMode Whether immersive mode is active (hides system UI)
 * @property isLoading Whether content is currently loading
 * @property errorMessage Error message to display, null if no error
 */
data class BookUiState(
    val currentBook: Book? = null,
    val currentChapterIndex: Int = 0,
    val isImmersiveMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

