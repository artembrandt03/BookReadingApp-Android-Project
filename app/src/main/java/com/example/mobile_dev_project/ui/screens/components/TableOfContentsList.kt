package com.example.mobile_dev_project.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.mobile_dev_project.R
import com.example.mobile_dev_project.data.Book

/**
 * Displays a scrollable list of chapters for the given book
 *
 * @param book The book whose chapters to display
 * @param onChapterClick Callback when a chapter is clicked
 */
@Composable
fun TableOfContentsList(
    book: Book,
    chapters: List<com.example.mobile_dev_project.data.Chapter>,
    onChapterClick: (chapterIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                vertical = dimensionResource(id =
                    R.dimen.spacing_medium)
            ),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.toc_item_spacing)
            )
        ) {
            // Header with book title
            item {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(id =
                            R.dimen.toc_header_padding),
                        vertical = dimensionResource(id =
                            R.dimen.spacing_small)
                    )
                )
            }

            // Chapter items
            itemsIndexed(chapters) { index, chapter ->
                ChapterListItem(
                    chapterNumber = chapter.number,
                    chapterTitle = chapter.title,
                    onClick = { onChapterClick(index) }
                )
            }
        }
    }
}
