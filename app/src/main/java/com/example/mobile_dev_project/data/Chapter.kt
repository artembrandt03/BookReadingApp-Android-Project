package com.example.mobile_dev_project.data

/**
 * Represents a single chapter in a book
 *
 * @property id Unique identifier for the chapter
 * @property number Chapter number (e.g., 1, 2, 3...)
 * @property title The chapter's title
 * @property content The chapter's text content
 */
data class Chapter(
    val id: Int,
    val number: Int,
    val title: String,
    val content: String = "",
    val htmlFilePath: String = ""
)

