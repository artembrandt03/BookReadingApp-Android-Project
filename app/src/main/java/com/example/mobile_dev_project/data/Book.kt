package com.example.mobile_dev_project.data

/**
 * Represents a book in the library
 *
 * @property id Unique identifier for the book
 * @property title The book's title
 * @property author The book's author
 * @property coverUrl URL or path to the book cover image
 * @property chapters List of chapters in this book
 * @property lastAccessedDate Timestamp when book was last opened (for sorting)
 */
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String = "",
    val chapters: List<Chapter> = emptyList(),
    val lastAccessedDate: Long? = null,
    val coverImagePath: String? = null
)

