package com.example.mobile_dev_project.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobile_dev_project.vm.AppViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import com.example.mobile_dev_project.R
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.text.style.TextAlign
import com.example.mobile_dev_project.vm.IAppViewModel


/**
 * ---------------------------------------------------------------
 * SearchScreen (Milestone 1 implementation)
 * ---------------------------------------------------------------
 * Purpose:
 *  - Allows the user to search within the currently opened book.
 *  - Displays a TextField for typing, and a list of search results.
 *  - Uses the AppViewModel’s state:
 *      vm.searchQuery       -> current input
 *      vm.searchResults     -> list of SearchResult (chapterIndex + snippet)
 *  - When a result is tapped:
 *      -> vm.openChapter(hit.chapterIndex)
 *      -> navigate to the Reading screen
 *
 * Notes:
 *  - This is a standalone screen in /ui/screens/
 *  - It reads data from the shared AppViewModel
 *  - It’s fully functional with M1 dummy data for now
 */
@Composable
fun SearchScreen(
    vm: IAppViewModel,
    onOpenReading: () -> Unit, //nav callback: goes to ReadingScreen
    modifier: Modifier = Modifier
) {
    // Check if a book is selected - if not, show empty state
    if (vm.currentBookId == null) {
        NoBookSelected()
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.spacing_default))
            .testTag("search_screen")

    ) {
        // -----------------------------
        // Search input field
        // -----------------------------
        OutlinedTextField(
            value = vm.searchQuery,                    //current text typed by the user
            onValueChange = { vm.updateSearchQuery(it) }, //update the ViewModel search query
            label = { Text(stringResource(R.string.search_label)) },
            singleLine = true,                         //makes the input one line only
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            trailingIcon = {
                //show a "Clear" button when there’s text in the search box
                if (vm.searchQuery.isNotBlank()) {
                    TextButton(onClick = { vm.clearSearch() }) {
                        Text(stringResource(R.string.search_clear))
                    }
                }
            }
        )

        Spacer(Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

        // -----------------------------
        // Conditional UI states
        // -----------------------------
        when {
            //Nothing typed yet -> show a helpful hint
            vm.searchQuery.isBlank() -> EmptyBox()

            //User typed something but found no matches
            vm.searchResults.isEmpty() -> NoResults()

            //There are results -> display them in a scrollable list
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = dimensionResource(id = R.dimen.spacing_default)
                    )
                ) {
                    //show each search result from the ViewModel
                    items(vm.searchResults) { hit ->
                        SearchResultRow(
                            title = hit.chapterTitle,
                            snippet = hit.snippet,
                            onClick = {
                                //When a result is tapped:
                                //1. Tell VM to open the chapter with search data
                                //2. Navigate to the Reading screen
                                vm.openChapter(hit.chapterIndex, hit.matchPosition, hit.matchLength, hit.searchQuery, hit.occurrenceInChapter)
                                onOpenReading()
                            }
                        )
                        HorizontalDivider(thickness = dimensionResource(id = R.dimen.toc_divider_thickness))
                    }
                }
            }
        }
    }
}

/**
 * Displays one search result row (chapter title + text snippet).
 * On click opens the selected chapter and goes to Reading screen.
 */
@Composable
private fun SearchResultRow(
    title: String,
    snippet: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = dimensionResource(id = R.dimen.spacing_medium))
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(dimensionResource(id = R.dimen.spacing_small)))
        Text(
            text = snippet,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3
        )
    }
}

/**
 * Shown when the search box is empty (before the user starts typing).
 */
@Composable
private fun EmptyBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.spacing_large)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.search_hint_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Shown when the user typed something but no matches were found.
 * Indicates that the search returned zero results.
 */
@Composable
private fun NoResults() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.spacing_large)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.search_no_results),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Shown when no book has been selected yet
 */
@Composable
private fun NoBookSelected() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.spacing_large))
            .testTag("empty_search_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(dimensionResource(id = R.dimen.toc_chapter_badge_size))
            )

            Spacer(modifier = Modifier.height(dimensionResource(id =
                R.dimen.spacing_medium)))

            Text(
                text = stringResource(id = R.string.toc_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}


// --- PREVIEWS ---------------------------------------------------------------
//@SuppressLint("ViewModelConstructorInComposable")
//@Preview(showBackground = true, name = "Search – Empty (type hint)")
//@Composable
//private fun PreviewSearch_Empty() {
//    val vm = AppViewModel().apply {
//        loadFakeLibrary()               //seed 2 books
//        selectBook("b1")                //seeds chapters for b1
//        clearSearch()                   //query = "" -> shows EmptyBox()
//    }
//    MaterialTheme {
//        SearchScreen(vm = vm, onOpenReading = {})
//    }
//}

//@SuppressLint("ViewModelConstructorInComposable")
//@Preview(showBackground = true, name = "Search – No Results")
//@Composable
//private fun PreviewSearch_NoResults() {
//    val vm = AppViewModel().apply {
//        loadFakeLibrary()
//        selectBook("b1")
//        updateSearchQuery("zzzzzz")     //unlikely to match -> NoResults()
//    }
//    MaterialTheme {
//        SearchScreen(vm = vm, onOpenReading = {})
//    }
//}
//
//@SuppressLint("ViewModelConstructorInComposable")
//@Preview(showBackground = true, name = "Search – With Results")
//@Composable
//private fun PreviewSearch_WithResults() {
//    val vm = AppViewModel().apply {
//        loadFakeLibrary()
//        selectBook("b1")
//        //The seeded chapter content contains the word "Content", so this should match
//        //Tested - works
//        updateSearchQuery("Content")    //list of hits
//    }
//    MaterialTheme {
//        SearchScreen(vm = vm, onOpenReading = {})
//    }
//}