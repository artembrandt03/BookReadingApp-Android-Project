package com.example.mobile_dev_project.data

/**
 * Sample data for testing the Table of Contents and Reading screens
 */
object SampleData {

    val sampleBook = Book(
        id = "1",
        title = "The Great Adventure",
        author = "Jane Doe",
        coverUrl = "",
        chapters = listOf(
            Chapter(
                id = 1,
                number = 1,
                title = "The Beginning",
                content = "Sample content for chapter 1..."
            ),
            Chapter(
                id = 2,
                number = 2,
                title = "The Journey",
                content = "Sample content for chapter 2..."
            ),
            Chapter(
                id = 3,
                number = 3,
                title = "The Discovery",
                content = "Sample content for chapter 3..."
            ),
            Chapter(
                id = 4,
                number = 4,
                title = "The Return",
                content = "Sample content for chapter 4..."
            )
        )
    )

    //------- FOR DOWNLOAD SCREEN ---------
    /** These are default dummy chapters used when "downloading" a book */
    /** this is just a test data for M1 **/
    val defaultChapters: List<Chapter> = listOf(
        Chapter(id = 1, number = 1, title = "Introduction", content = "This is the intro text..."),
        Chapter(id = 2, number = 2, title = "Chapter 1",    content = "This is the first chapter..."),
        Chapter(id = 3, number = 3, title = "Chapter 2",    content = "This is the second chapter...")
    )

    /** Factory for creating a placeholder book used by Download screen */
    fun makeDownloadedBook(
        id: String,
        title: String,
        author: String = "Unknown",
        coverUrl: String = ""
    ): Book = Book(
        id = id,
        title = title,
        author = author,
        coverUrl = coverUrl,
        chapters = defaultChapters,
        lastAccessedDate = null
    )
}
