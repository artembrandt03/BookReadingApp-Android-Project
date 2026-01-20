package com.example.mobile_dev_project.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.mobile_dev_project.data.models.BookEntity
import com.example.mobile_dev_project.data.models.ChapterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.util.UUID

/**
 * Handles parsing HTML book files from Project Gutenberg.
 *
 * This is my implementation for Milestone 2, Section 2: Parse HTML Files.
 * Uses Jsoup to extract book metadata and chapters from HTML.
 *
 * @param context Needed for file operations
 */
class HtmlParserRepository(
    private val context: Context,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository
) {

    private val TAG = "HtmlParser"

    /**
     * Parse a book from its HTML file.
     *
     * Main entry point - handles the complete parsing process.
     * When database is ready, I'll add repository calls to store the data.
     *
     * @param bookFolderPath Path to folder containing the book's HTML files
     * @param mainHtmlFileName Name of the main HTML file (default: "index.html")
     * @return Book ID if successful, null if failed
     */
    suspend fun parseBook(
        bookFolderPath: String,
        mainHtmlFileName: String = "index.html"
    ): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting to parse book from: $bookFolderPath")

            // Validate folder exists
            val bookFolder = File(bookFolderPath)
            if (!bookFolder.exists() || !bookFolder.isDirectory) {
                Log.e(TAG, "Book folder doesn't exist: $bookFolderPath")
                return@withContext null
            }

            // Validate HTML file exists
            val htmlFile = File(bookFolder, mainHtmlFileName)
            if (!htmlFile.exists()) {
                Log.e(TAG, "HTML file not found: ${htmlFile.absolutePath}")
                return@withContext null
            }

            // Parse HTML document
            val document = Jsoup.parse(htmlFile, "UTF-8")

            // Generate book ID
            val bookId = UUID.randomUUID().toString()

            // Extract book title
            val title = extractTitle(document)
            Log.d(TAG, "Extracted title: $title")

            // Extract author name
            val author = extractAuthor(document)
            Log.d(TAG, "Extracted author: $author")

            // Find cover image
            val coverPath = findCoverImage(bookFolder)
            if (coverPath.isNotEmpty()) {
                Log.d(TAG, "Found cover image: $coverPath")
            } else {
                Log.d(TAG, "No cover image found")
            }

            // Extract chapters (save chapters as separate HTML files)
            Log.d(TAG, "Extracting chapters...")
            val chapters = extractChapters(document, bookId, bookFolder)
            Log.d(TAG, "Extracted ${chapters.size} chapters")

            // Create book entity with all the extracted data
            val bookEntity = BookEntity(
                id = bookId,
                title = title,
                author = author,
                coverImagePath = coverPath,
                totalChapters = chapters.size,
                isDownloaded = true
            )

            Log.d(TAG, "Successfully parsed book: $title")

            // Insert book into database
            bookRepository.insert(bookEntity)
            Log.d(TAG, "Inserted book entity into database")

            // Insert all chapters into database
            chapterRepository.insertAll(chapters)
            Log.d(TAG, "Inserted ${chapters.size} chapters into database")


            return@withContext bookId

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing book: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Extract the book title from HTML document.
     *
     * Project Gutenberg books have titles in different places, so I try multiple strategies:
     * 1. Check the <title> tag
     * 2. Look for the first <h1> heading
     * 3. Check for Open Graph meta tags
     *
     * Then clean up the result to remove Project Gutenberg branding.
     */
    private fun extractTitle(document: Document): String {
        // Strategy 1: Look in <title> tag
        val titleTag = document.select("title").firstOrNull()?.text()
        if (!titleTag.isNullOrBlank()) {
            return cleanTitle(titleTag)
        }

        // Strategy 2: Look for <h1> heading
        val h1Tag = document.select("h1").firstOrNull()?.text()
        if (!h1Tag.isNullOrBlank()) {
            return cleanTitle(h1Tag)
        }

        // Strategy 3: Check Open Graph meta tag
        val metaTitle = document.select("meta[property=og:title]")
            .firstOrNull()?.attr("content")
        if (!metaTitle.isNullOrBlank()) {
            return cleanTitle(metaTitle)
        }

        return context.getString(com.example.mobile_dev_project.R.string.parser_unknown_title)
    }

    /**
     * Clean up title by removing Project Gutenberg branding.
     *
     * Removes things like "The Project Gutenberg eBook of" and "[with illustrations]"
     */
    private fun cleanTitle(title: String): String {
        return title
            .replace(Regex("The Project Gutenberg [eE]Book of\\s*"), "")
            .replace(Regex("Project Gutenberg's\\s*"), "")
            .replace(Regex("\\s*\\[.*?]"), "")
            .replace(Regex("\\s*[|].*"), "")
            .replace(Regex(",\\s*by\\s+.*"), "")
            .trim()
    }

    /**
     * Extract author name from HTML document.
     *
     * I try to find the author in:
     * 1. Meta tags (most reliable if present)
     * 2. Text patterns like "by Author Name"
     */
    private fun extractAuthor(document: Document): String {
        // First: Look for author in meta tags
        val metaAuthor = document.select("meta[name=author]")
            .firstOrNull()?.attr("content")
        if (!metaAuthor.isNullOrBlank()) {
            return metaAuthor.trim()
        }

        // Second: Look for author in DC meta tags (common in Gutenberg books)
        val dcCreator = document.select("meta[name=dc.creator], meta[property=dc:creator]")
        .firstOrNull()?.attr("content")
        if (!dcCreator.isNullOrBlank()) {
            return dcCreator.trim()
        }

        // Third: Search for "by [Author Name]" pattern early in the document
        // Only search first 5000 characters to avoid false matches
        val bodyText = document.select("body").text().take(5000)
        val byPattern =
            Regex("\\bby\\s+([A-Z][a-zA-Z\\s.'-]{2,40}?)(?:\\s*[,.]|\\s+\\(|$)",
                RegexOption.IGNORE_CASE)
        val match = byPattern.find(bodyText)
        if (match != null) {
            val author = match.groupValues[1].trim()
            // Validate: must be at least 2 words or one word with at least 3 letters
            if (author.contains(" ") || author.length >= 3) {
                return author
            }
        }

        return context.getString(com.example.mobile_dev_project.R.string.parser_unknown_author)
    }

    /**
     * Try to find a cover image in the book folder.
     *
     * Looks for common cover image filenames in:
     * - The main book folder
     * - An /images subfolder
     *
     * It's okay if we don't find one - cover images are optional.
     *
     * @param bookFolder The folder containing the book files
     * @return Absolute path to cover image, or empty string if not found
     */
    private fun findCoverImage(bookFolder: File): String {
        // Step 1: Look for files with "cover" in the name
        val coverFile = bookFolder.walkTopDown()
            .firstOrNull {
                it.isFile &&
                        it.extension.lowercase() in listOf("jpg", "jpeg", "png") &&
                        it.name.contains("cover", ignoreCase = true)
            }

        if (coverFile != null) {
            return coverFile.absolutePath
        }

        // Step 2: If no cover found, use ANY image file in images/ folder
        val imagesFolder = File(bookFolder, "images")
        if (imagesFolder.exists()) {
            val anyImage = imagesFolder.listFiles()?.firstOrNull {
                it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png")
            }
            if (anyImage != null) {
                return anyImage.absolutePath
            }
        }

        // Step 3: Use ANY image in the main folder as last resort
        val anyImageInMain = bookFolder.listFiles()?.firstOrNull {
            it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png")
        }

        return anyImageInMain?.absolutePath ?: ""
    }

    /**
     * Extract chapters from the HTML document.
     *
     * "Save processed chapters as separate HTML files"
     */
    @SuppressLint("SuspiciousIndentation")
    private fun extractChapters(
        document: Document,
        bookId: String,
        bookFolder: File
    ): List<ChapterEntity> {
        val chaptersFolder = createChaptersFolder(bookFolder)
        val chapterHeadings = document.select("h2:containsOwn(Chapter), h3:containsOwn(Chapter)")
            if (chapterHeadings.isEmpty()) {
                Log.w(TAG, "No chapter headings found, treating whole book as one chapter")
                return listOf(createSingleChapter(document, bookId, chaptersFolder))
            }
        // Extract front matter and create Chapter 0
        val allChapters = mutableListOf<ChapterEntity>()
        val frontMatterHtml = extractFrontMatter(document)
        if (frontMatterHtml.isNotBlank()) {
            val frontMatterChapter = createFrontMatterChapter(frontMatterHtml, bookId,
                chaptersFolder)
            allChapters.add(frontMatterChapter)
            Log.d(TAG, "Created front matter chapter (Chapter 0)")
        }
        // Extract regular chapters starting from Chapter 1
        val regularChapters = chapterHeadings.mapIndexedNotNull { index, heading ->
            processChapter(heading, index + 1, bookId, chaptersFolder)
        }
        allChapters.addAll(regularChapters)
        return allChapters
    }

    /**
     * Create the chapters subfolder for storing individual chapter HTML files.
     */
    private fun createChaptersFolder(bookFolder: File): File {
        val chaptersFolder = File(bookFolder, "chapters")
        if (!chaptersFolder.exists()) {
            chaptersFolder.mkdirs()
            Log.d(TAG, "Created chapters folder")
        }
        return chaptersFolder
    }

    /**
     * Process a single chapter: extract content, save as file, create entity.
     */
    private fun processChapter(
        heading: Element,
        chapterNumber: Int,
        bookId: String,
        chaptersFolder: File
    ): ChapterEntity? {
        return try {
            val chapterTitle = heading.text().trim()
            Log.d(TAG, "  Processing Ch$chapterNumber: $chapterTitle")

            val content = extractChapterContent(heading)
            val preview = content.text().take(200)
            val chapterFilePath = saveChapterFile(chapterTitle, content.html(), chapterNumber, chaptersFolder)

            ChapterEntity(
                bookId = bookId,
                chapterNumber = chapterNumber,
                title = chapterTitle,
                htmlFilePath = chapterFilePath,
                contentPreview = preview
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing chapter $chapterNumber: ${e.message}")
            null
        }
    }

    /**
     * Save chapter content as an HTML file and return the file path.
     */
    private fun saveChapterFile(
        title: String,
        content: String,
        chapterNumber: Int,
        chaptersFolder: File
    ): String {
        val chapterFileName = "chapter_$chapterNumber.html"
        val chapterFile = File(chaptersFolder, chapterFileName)
        val fixedContent = fixImagePaths(content, chaptersFolder.parentFile)
        val chapterHtml = wrapInHtmlDocument(title, fixedContent)
        chapterFile.writeText(chapterHtml)
        return chapterFile.absolutePath
    }

    /**
     * Extract chapter content using the best available strategy.
     */
    private fun extractChapterContent(chapterHeading: Element): Element {

        // Strategy 1: Try extracting siblings
        var contentDiv = extractContentViaSiblings(chapterHeading)
        var elementCount = contentDiv.childNodeSize()

        // Strategy 2: If no siblings, try forward search
        if (elementCount == 0) {
            contentDiv = extractContentViaForwardSearch(chapterHeading)
            elementCount = contentDiv.childNodeSize()
        }
        return contentDiv
    }

    /**
     * Strategy 1: Extract content from sibling elements.
     * Works for books where content follows the heading directly.
     */
    private fun extractContentViaSiblings(chapterHeading: Element): Element {
        val contentDiv = Element("div")

        var currentElement = chapterHeading.nextElementSibling()

        while (currentElement != null) {
            val tagName = currentElement.tagName()
            val text = currentElement.text()

            // Stop when we hit the next chapter heading
            if ((tagName == "h2" || tagName == "h3") &&
                text.contains("Chapter", ignoreCase = true)) {
                break
            }

            // Add this element to our chapter content
            contentDiv.appendChild(currentElement.clone())
            currentElement = currentElement.nextElementSibling()
        }
        return contentDiv
    }

    /**
     * Strategy 2: Extract content by searching forward in the document.
     * Works for books with complex HTML structure where content isn't a sibling.
     */
    private fun extractContentViaForwardSearch(chapterHeading: Element): Element {
        val contentDiv = Element("div")
        val document = chapterHeading.ownerDocument() ?: return contentDiv
        // Find all h2 elements to determine chapter boundaries
        val allH2s = document.select("h2")
        val currentIndex = allH2s.indexOf(chapterHeading)

        if (currentIndex < 0) return contentDiv

        val nextH2 = if (currentIndex + 1 < allH2s.size) allH2s[currentIndex + 1] else null

        // Extract all relevant elements between current h2 and next h2
        val allElements = document.body()?.getAllElements() ?: return contentDiv
        val relevantTags = setOf("p", "div", "img", "ul", "ol", "blockquote")
        var capturing = false
        for (element in allElements) {
            when {
                element == chapterHeading -> capturing = true
                element == nextH2 -> break
                capturing && element.tagName() in relevantTags -> {
                    if (element.text().trim().isNotEmpty() || element.tagName() == "img") {
                        contentDiv.appendChild(element.clone())
                    }
                }
            }
        }
        return contentDiv
    }
    /**
     * Create Chapter 0 from front matter content.
     */
    private fun createFrontMatterChapter(
        frontMatterHtml: String,
        bookId: String,
        chaptersFolder: File
    ): ChapterEntity {
        val fixedHtml = fixImagePaths(frontMatterHtml, chaptersFolder.parentFile)
        val chapterFileName = "chapter_0.html"
        val chapterFile = File(chaptersFolder, chapterFileName)
        val frontMatterTitle = context.getString(com.example.mobile_dev_project.R.string.parser_front_matter)
        val chapterHtml = wrapInHtmlDocument(frontMatterTitle, fixedHtml)
        chapterFile.writeText(chapterHtml)

        // Create preview from the HTML content
        val preview = Jsoup.parse(frontMatterHtml).text().take(200)

        return ChapterEntity(
            bookId = bookId,
            chapterNumber = 0,
            title = frontMatterTitle,
            htmlFilePath = chapterFile.absolutePath,
            contentPreview = preview
        )
    }
    /**
     * Fallback for books without chapter markers.
     * Treats the entire book as one big chapter.
     */
    private fun createSingleChapter(
        document: Document,
        bookId: String,
        chaptersFolder: File
    ): ChapterEntity {
        val bodyContent = document.select("body").html()
        val preview = document.select("body").text().take(200)

        val chapterFile = File(chaptersFolder, "chapter_1.html")
        val fullTextTitle = context.getString(com.example.mobile_dev_project.R.string.parser_full_text)
        val chapterHtml = wrapInHtmlDocument(fullTextTitle, bodyContent)
        chapterFile.writeText(chapterHtml)

        return ChapterEntity(
            bookId = bookId,
            chapterNumber = 1,
            title = fullTextTitle,
            htmlFilePath = chapterFile.absolutePath,
            contentPreview = preview
        )
    }
    /**
     * Extract the front matter of a book.
     *
     * Front matter is defined as everything starting from the first <h2>
     * up to (but not including) the first <h2> that contains "Chapter".
     *
     * @param document The parsed HTML document
     * @return HTML content of the front matter as a string
     */
    private fun extractFrontMatter(document: Document): String {
        val frontMatterDiv = Element("div")

        val firstChapterHeading = document.select("h2:containsOwn(Chapter), h3:containsOwn(Chapter)")
            .firstOrNull()

        if (firstChapterHeading == null) {
            return ""
        }

        val bodyElements = document.select("body").first()?.children() ?: return ""

        for (element in bodyElements) {
            if (element == firstChapterHeading ||
                element.select("h2:containsOwn(Chapter), h3:containsOwn(Chapter)").isNotEmpty()) {
                break
            }
            frontMatterDiv.appendChild(element.clone())
        }

        return frontMatterDiv.html()
    }
    /**
     * Fix image paths in HTML content to be relative to the chapters folder.
     *
     * Since chapter HTML files are saved in a "chapters" subfolder, we need to
     * adjust image paths to go up one level (../) to reference images in the book folder.
     *
     * @param html The HTML content with potentially broken image paths
     * @param bookFolder The root book folder containing images
     * @return HTML with corrected image paths
     */
    private fun fixImagePaths(html: String, bookFolder: File): String {
        val doc = Jsoup.parse(html)
        val images = doc.select("img")

        images.forEach { img ->
            val src = img.attr("src")
            if (src.isNotBlank() && !src.startsWith("http")) {
                val cleanSrc = src.removePrefix("./").removePrefix("/")

                val newSrc = "../$cleanSrc"
                img.attr("src", newSrc)

                Log.d(TAG, "Fixed image path: $src -> $newSrc")
            }
        }

        return doc.body().html()
    }
    /**
     * Wrap chapter content in a complete HTML document.
     *
     * Creates a properly formatted HTML file that can be displayed in WebView.
     * Includes basic CSS styling for readability.
     */
    private fun wrapInHtmlDocument(title: String, content: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$title</title>
                <style>
                    body {
                        font-family: Georgia, serif;
                        font-size: 18px;
                        line-height: 1.6;
                        padding: 16px;
                        max-width: 800px;
                        margin: 0 auto;
                        color: #333;
                    }
                    p {
                        margin: 1em 0;
                        text-align: justify;
                    }
                    h1, h2, h3 {
                        margin-top: 1.5em;
                        margin-bottom: 0.5em;
                    }
                    img {
                        max-width: 100%;
                        height: auto;
                        display: block;
                        margin: 1em auto;
                    }
                </style>
            </head>
            <body>
                <h2>$title</h2>
                $content
            </body>
            </html>
        """.trimIndent()
    }
}
