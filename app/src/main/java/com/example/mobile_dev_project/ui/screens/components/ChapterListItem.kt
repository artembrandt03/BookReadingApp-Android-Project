package com.example.mobile_dev_project.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobile_dev_project.R

/**
 * Individual chapter list item showing chapter number and title
 *
 * @param chapterNumber The chapter number (e.g., 1, 2, 3)
 * @param chapterTitle The chapter's title
 * @param onClick Callback when the item is clicked
 */
@Composable
fun ChapterListItem(
    chapterNumber: Int,
    chapterTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.toc_item_padding))
            .clickable(onClick = onClick)
            .testTag("chapter_$chapterNumber"),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.toc_elevation)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        ChapterItemContent(chapterNumber, chapterTitle)
    }
}

@Composable
private fun ChapterItemContent(
    chapterNumber: Int,
    chapterTitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.toc_item_padding)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChapterNumberBadge(chapterNumber)
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_medium)))
        ChapterTitleSection(chapterNumber, chapterTitle)
    }
}

@Composable
private fun ChapterNumberBadge(chapterNumber: Int) {
    Surface(
        modifier = Modifier.size(dimensionResource(id = R.dimen.toc_chapter_badge_size)),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = chapterNumber.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(dimensionResource(id = R.dimen.toc_chapter_badge_padding))
        )
    }
}

@Composable
private fun ChapterTitleSection(chapterNumber: Int, chapterTitle: String) {
    Column {
        Text(
            text = stringResource(id = R.string.chapter_number, chapterNumber),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = chapterTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
