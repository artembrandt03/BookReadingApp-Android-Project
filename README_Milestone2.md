# Book Reading App

A mobile Android app that allows users to **download, store, and read HTML books** (e.g., from Project Gutenberg) with chapter navigation, localization, and resume-reading functionality.  
Built with **Kotlin**, **Jetpack Compose**, **Room**, **Hilt**, and **Coroutines**.

---

## Team
**Alisina Zahabsaniei**  
**Caden Ho**  
**Artem Brandt**

---

## Milestone 2 – Completed Features

This milestone focused on persistence, parsing, and internationalization — moving the project from an in-memory prototype to a full database-backed reading platform.

### 1. Database Integration (Room DB)
- Designed and implemented **Room Database** with `BookEntity`, `ChapterEntity`, and `ReadingProgressEntity`.
- Created corresponding **DAOs** (`BookDao`, `ChapterDao`, `ReadingProgressDao`) and **repositories** for clean separation of concerns.
- Added **ER diagram** to document relationships between entities.
- Used **Flow** to observe live data updates between DB → UI.


### 2. File Download + Unzipping + Storage
- Integrated **OkHttp** for reliable book downloads.
- Implemented `FileRepository` to:
  - Download ZIP archives.  
  - Create book-specific folders in app storage.  
  - Unzip archives using `UnzipUtil`.  
  - Store cover images and HTML files.

### 3. HTML Parsing with Jsoup
- Implemented `HtmlParserRepository` to extract chapter titles and content.  
- Parsed HTML into structured `ChapterEntity` records and saved them to DB.  
- Cleaned HTML text and removed hardcoded strings.  
- Added tests for HTML parsing.

### 4. Display Book Content
- Replaced sample data with real parsed content loaded from local files.  
- Chapters rendered inside a `WebView` for rich HTML layout.  
- Implemented vertical scroll per chapter + horizontal swipe between chapters (`HorizontalPager`).  
- Fixed scroll conflict between pager and WebView for buttery-smooth reading.

### 5. Resume Last Reading Position
- Added `ReadingProgressEntity` table and repository.  
- When reading, scroll position and chapter index are saved on scroll.  
- On re-launch, the app restores the last chapter and scroll position.  

### 6. Localization & Internationalization
- Full UI support for **English and French**.  
- Moved all UI text to `strings.xml`.  
- Added `values-fr/strings.xml` for localized French translations.

### 7. ViewModel + Dependency Injection (Hilt)
- Introduced `@HiltViewModel` for `AppViewModel`.  
- Injected repositories using Hilt modules (`DatabaseModule`, `NetworkModule`).  
- ViewModel now handles all business logic for:
  - Downloading books and processing them.  
  - Managing state for Home, TOC, Reading, Search screens.  
  - Saving and restoring reading progress.

### 8. Coroutines for Async Operations
All I/O-bound tasks now run on background threads using Kotlin Coroutines:
- File downloads and unzip.  
- HTML parsing.  
- Room DB operations.  
- Progress updates on main thread.

### 9. Testing Infrastructure
We added both Unit and Instrumentation tests for all new features.

### 10. UI Polish & UX Improvements
- Fixed scroll behavior in Reading Screen – vertical scroll now smooth and independent of pager swipe.  
- Improved snackbar messages and progress status visibility on Download Screen.  
- Applied consistent Material 3 theming with spacing, typography, and elevation from `theme/`.  
- Added cover image display for downloaded books.

## Architecture Overview
**MVVM + Repository pattern** with Hilt DI.

| Layer | Responsibilities |
|:--|:--|
| UI | Jetpack Compose screens + Navigation components |
| ViewModel | Business logic, state management, coroutines |
| Data | Room entities, DAOs, repositories |
| Utils | File handling, initial download, immersive mode |

## Navigation
- **Bottom Navigation Bar** for compact screens (Home, Download, TOC, Search).  
- **Conditional Visibility:** TOC and Search tabs only appear when a book is selected.  
- Future support for Navigation Rail/Drawer planned for larger devices.

## Tech Stack
- **Language:** Kotlin  
- **UI:** Jetpack Compose + Material 3  
- **Async:** Kotlin Coroutines  
- **Persistence:** Room Database  
- **Networking:** OkHttp  
- **Parsing:** Jsoup  
- **DI:** Hilt  
- **Testing:** JUnit + Espresso + Compose UI Test  
- **Architecture:** MVVM (Separation of Concerns)

## How to Run
1. Clone the repository
2. Open in Android Studio Hedgehog or later.  
3. Run on an emulator (API 26+) or Android device.  
4. Use the **Download screen** to import new books via URL or use the sample books included.

## Current Milestone Status
✅ **Milestone 1 – Completed**  
✅ **Milestone 2 – Completed (on branch `project-m2`)**  
⏳ **Milestone 3 – Upcoming (Text-to-Speech & Search Enhancements)**  
