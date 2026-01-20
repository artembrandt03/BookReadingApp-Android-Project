package com.example.mobile_dev_project.data

/**
 * SearchResult
 * Represents a single search result when the user looks content within a book.
 * Each Search shows where the match happened and a small text preview.
 *
 * Used moslty in Search screen to list results and in the Reading screen
 * to jump to the correct chapter when a result is selected.
 */
data class SearchResult(
    val chapterIndex: Int,
    val chapterTitle: String,           //chapter name for display
    val snippet: String,                //preview text with surrounding context
    val matchPosition: Int,             //character position of match in content
    val matchLength: Int,               //length of matched text
    val searchQuery: String,            //the search term
    val occurrenceInChapter: Int        //which occurrence in this chapter (1st, 2nd, etc)
)