# Database Schema Design - Book Reading App

---

## Table 1: Books

This table stores the main information about each book in the library.

### Structure

| Column | Type | Constraints |           What It Stores                      |
|--------|------|-------------|-----------------------------------------------|
| id     | TEXT | PRIMARY KEY | Generated UUID to uniquely identify each book |
| title  | TEXT | NOT NULL    | The book's title (extracted from HTML)        |
| author | TEXT | NOT NULL    | Author's name (extracted from HTML)           |
|coverImagePath| TEXT | nullable | Where the cover image is saved on device |
| dateAdded | INTEGER | NOT NULL | When the book was first downloaded (timestamp in milliseconds) |
| lastAccessed | INTEGER | nullable | Last time user opened this book (null if never opened) |
| totalChapters | INTEGER | NOT NULL | How many chapters this book has |

### Why I Designed It This Way

- I chose TEXT for the ID instead of auto-increment because I want to generate UUIDs which are better for avoiding conflicts if we ever sync data
- The `dateAdded` and `lastAccessed` fields are required by Milestone 2 (section 6) to display on the bookshelf
- `coverImagePath` can be null because not all books might have covers
- Using INTEGER for dates lets us store precise timestamps that we can format however we want in the UI

---

## Table 2: Chapters

Each book gets split into chapters during parsing. This table stores information about individual chapters.

### Structure

| Column | Type | Constraints | What It Stores |
|--------|------|-------------|----------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique ID for each chapter |
| bookId | TEXT | FOREIGN KEY, NOT NULL | Links to the parent book |
| chapterNumber | INTEGER | NOT NULL | Sequential number (1, 2, 3, etc.) |
| title | TEXT | NOT NULL | The chapter heading extracted from HTML |
| htmlFilePath | TEXT | NOT NULL | Where the saved chapter HTML file is located |
| contentPreview | TEXT | nullable | First ~200 characters for search results |

### Foreign Key Setup

- `bookId` references `books.id`
- I set it to CASCADE on delete so if a book gets deleted, all its chapters automatically get removed too

### Why I Designed It This Way

- Auto-incrementing ID makes it simple and efficient
- I'm storing the chapter as a separate HTML file (requirement in M2), so I need to track the file path
- The `contentPreview` field will help with the search feature in Milestone 3 - instead of searching through entire files, we can quickly search these previews
- Having `chapterNumber` separate from `id` is important because the ID is just database stuff, but chapter number is what users see

### Index on bookId

I'm adding an index on the `bookId` column because we'll frequently query "give me all chapters for this book", so indexing it will make those queries much faster.

---

## Table 3: ReadingProgress

This table remembers where the user left off reading in each book.

### Structure

| Column | Type | Constraints | What It Stores |
|--------|------|-------------|----------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique identifier |
| bookId | TEXT | FOREIGN KEY, UNIQUE, NOT NULL | Which book this progress is for |
| currentChapterIndex | INTEGER | NOT NULL, default 0 | Which chapter they're currently on |
| scrollPosition | INTEGER | NOT NULL, default 0 | How far down the page they scrolled (in pixels) |
| lastUpdated | INTEGER | NOT NULL | When this position was last saved |

### Why I Designed It This Way

- The `UNIQUE` constraint on `bookId` ensures each book only has one progress record - we don't want duplicates
- I'm storing the scroll position in pixels, which matches how Android's scroll state works
- Setting defaults to 0 means if there's no saved progress, they start at the beginning
- This addresses the requirement in M2 Section 8 about resuming reading position

---

## How The Tables Connect

### Books to Chapters (One-to-Many)

One book contains many chapters. When I delete a book, all its chapters should also be deleted automatically (cascade delete).

```
Books (1) ──→ Chapters (many)
```

Each chapter points back to exactly one book through the `bookId` foreign key.

### Books to ReadingProgress (One-to-One)

Each book has exactly one reading progress record. The UNIQUE constraint on `bookId` enforces this.

```
Books (1) ──→ ReadingProgress (1)
```

---

## Visual Diagram

Here's how the tables relate to each other:

```
┌─────────────────────────┐
│       BOOKS             │
│─────────────────────────│
│ • id (PK)               │
│ • title                 │
│ • author                │
│ • coverImagePath        │
│ • dateAdded             │
│ • lastAccessed          │
│ • totalChapters         │
└───────┬─────────────────┘
        │
        │ one book has...
        │
        ├──────────────────────────┐
        │                          │
        ├─→ many chapters          ├─→ one progress record
        │                          │
        ▼                          ▼
┌─────────────────────┐    ┌──────────────────────┐
│    CHAPTERS         │    │  READING_PROGRESS    │
│─────────────────────│    │──────────────────────│
│ • id (PK)           │    │ • id (PK)            │
│ • bookId (FK)       │    │ • bookId (FK, unique)│
│ • chapterNumber     │    │ • currentChapterIndex│
│ • title             │    │ • scrollPosition     │
│ • htmlFilePath      │    │ • lastUpdated        │
│ • contentPreview    │    └──────────────────────┘
└─────────────────────┘
```

---

## How Data Flows Through The App

### When a user downloads a new book:

1. Download component gets the ZIP file from Project Gutenberg
2. Unzips it to local storage
3. My parser reads the HTML and extracts book title, author, etc.
4. Parser creates a record in the `books` table
5. Parser splits content into chapters and creates records in `chapters` table for each one
6. The bookshelf automatically updates because it's watching the books table

### When a user opens a book to read:

1. They click on a book from the home screen
2. App queries the `chapters` table for all chapters where `bookId` matches
3. Table of contents displays the chapter list
4. They select a chapter to read
5. App checks `reading_progress` table to see if there's a saved position
6. If found, it scrolls to that position automatically
7. As they read and scroll, we periodically update the `scrollPosition` in `reading_progress`

### When a user searches within a book:

1. They enter a search term in the search screen
2. App queries `chapters` table filtering by `bookId`
3. Searches through the `contentPreview` column for matches
4. Returns list of chapters that contain the search term
5. When they click a result, opens that chapter directly

---


