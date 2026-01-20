package com.example.mobile_dev_project.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * This class represents a book stored in our database.
 *
 * I'm using this to hold all the important info about a book after it gets downloaded
 * and parsed. Room will use this to create the actual "books" table in SQLite.
 *
 * The parser fills this in when it extracts data from the HTML files, then the database
 * layer stores it, and finally the UI reads it to show books on the bookshelf.
 *
 * @property id A unique ID for each book - I generate this as a UUID during parsing
 * @property title The book's name, pulled from the HTML title or h1 tag
 * @property author Who wrote the book - I try to find this in meta tags or by searching for "by Author Name"
 * @property coverImagePath Where the cover image lives on the device (empty if we didn't find one)
 * @property dateAdded When this book was first downloaded, stored as milliseconds timestamp
 * @property lastAccessed Last time the user opened this book - stays null if they've never read it
 * @property totalChapters How many chapters the parser found in this book
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val id: String,

    val title: String,

    val author: String,

    // If we can't find a cover image, this just stays empty - not big deal
    val coverImagePath: String = "",

    // Gets set to now when creating a new book
    val dateAdded: Long = System.currentTimeMillis(),

    // Stays null until the user actually opens the book for the first time
    val lastAccessed: Long? = null,

    // Parser counts how many chapters it found and sets this
    val totalChapters: Int = 0,

    //differentiates between bundled (false) and downloaded (true) books
    @ColumnInfo(name = "isDownloaded", defaultValue = "0")
    val isDownloaded: Boolean = false
)
