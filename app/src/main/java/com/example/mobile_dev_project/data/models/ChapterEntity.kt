package com.example.mobile_dev_project.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Holds information about a single chapter within a book.
 *
 * When I parse a book, I split it into chapters and save each one as its own HTML file.
 * This class stores the metadata for each chapter so we can find and display them later.
 *
 * I set up a foreign key to BookEntity, which means each chapter is linked to a book.
 * If someone deletes a book, Room automatically deletes all its chapters too (CASCADE).
 *
 * @property id Room generates this automatically for each chapter
 * @property bookId Points to the parent book - has to match a real book ID
 * @property chapterNumber The number users see (1, 2, 3, etc.) in the table of contents
 * @property title The chapter heading I pulled from the HTML, like "Chapter 1: The Beginning"
 * @property htmlFilePath Full path to where I saved this chapter's HTML file
 * @property contentPreview The first bit of text - helps with searching without loading the whole file
 */
@Entity(
    tableName = "chapters",

    // Setting up the link between chapters and their parent book
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,           // Points to the books table
            parentColumns = ["id"],               // Uses the book's id column
            childColumns = ["bookId"],            // Maps to our bookId column
            onDelete = ForeignKey.CASCADE         // If book gets deleted, delete its chapters too
        )
    ],

    // Adding an index here speeds up queries when we need all chapters for a specific book
    indices = [Index("bookId")]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Has to be a real book ID from the books table
    val bookId: String,

    // What number chapter this is - shows up in the table of contents
    val chapterNumber: Int,

    // The actual chapter title from the HTML heading
    val title: String,

    // Where we saved this chapter's HTML file on the device
    val htmlFilePath: String,

    // Stores the first ~200 characters so we can search without loading full HTML files
    val contentPreview: String = ""
)
