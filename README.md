# Book Reading App

An Android app that lets you download and read HTML-formatted books from sites like Project Gutenberg. The app includes features like text-to-speech narration, full-text search, and automatic reading progress tracking.

## Our Team

- Alisina Zahabsaniei
- Caden Ho
- Artem Brandt

## What This App Does

We built a complete reading app over three milestones. You can download books in HTML format, read them with a clean interface, search for specific text, and even have the app read books aloud to you. The app remembers where you left off and supports both English and French languages.

## Features

### Reading Experience
- Download books from Project Gutenberg or custom URLs
- Read books with a clean, distraction-free interface
- Swipe horizontally between chapters, scroll vertically within chapters
- Immersive mode - tap to hide all UI elements for fullscreen reading
- Automatic reading progress tracking - pick up right where you left off

### Book Management
- Library view with book covers and last accessed dates
- Automatic HTML parsing to extract chapters and content
- Cover images displayed on your bookshelf
- All book data stored locally using Room database

### Search & Audio
- Full-text search across entire books
- Search results show context snippets with surrounding text
- Text-to-speech narration with play, pause, and stop controls
- TTS automatically stops when you switch chapters

### Localization
- Complete support for English and French
- App automatically uses your device's language setting

## Technology Stack

**Core**
- Kotlin (100% Kotlin codebase)
- Jetpack Compose for UI
- Material Design 3 theming
- Min SDK: 30, Target SDK: 36

**Key Libraries**
- **Room 2.8.3** - Database and persistence
- **Hilt 2.54** - Dependency injection
- **OkHttp 4.11.0** - Network requests and downloads
- **Jsoup 1.17.2** - HTML parsing
- **Navigation Compose 2.7.7** - Screen navigation
- **Kotlin Coroutines 1.8.1** - Asynchronous operations
- **Readium Toolkit 3.1.2** - Text-to-speech support
- **Coil 2.4.0** - Image loading

**Testing**
- JUnit, Mockito, MockK - Unit testing
- Espresso, Compose UI Test - Instrumentation testing
- Robolectric - Android framework simulation

## Architecture

We followed Android's recommended MVVM (Model-View-ViewModel) architecture with clear separation of concerns:

- **UI Layer**: Jetpack Compose screens (HomeScreen, DownloadScreen, ReadingScreen, SearchScreen, TableOfContentsScreen)
- **ViewModel Layer**: AppViewModel and TTSViewModel manage state using StateFlow
- **Data Layer**: Repositories handle all data operations (database, network, file I/O, parsing)
- **Dependency Injection**: Hilt manages all dependencies

**Database Schema**
- **BookEntity** - Stores book metadata (title, author, cover, dates)
- **ChapterEntity** - Stores chapter information with foreign key to books
- **ReadingProgressEntity** - Tracks current chapter and scroll position

All async operations use Kotlin Coroutines with proper dispatchers. Room queries use Flow for reactive updates.

## Milestone Breakdown

### Milestone 1: UI & Navigation (October 2024)
Built the foundation with five screens, bottom navigation bar, Material 3 theming, and immersive mode. Implemented vertical and horizontal scrolling. Added initial unit and instrumentation tests.

### Milestone 2: Persistence & Book Management (November 2024)
Integrated Room database with three tables. Implemented book downloads using OkHttp, ZIP extraction, and HTML parsing with Jsoup. Added reading progress persistence and Hilt dependency injection. Localized the app for English and French.

### Milestone 3: Search & Text-to-Speech (December 2024)
Added full-text search with context snippets and navigation to results. Integrated Android TextToSpeech with Readium Toolkit for narration. Added TTS controls to reading screen. Expanded test coverage for all new features.

## Running the App

### Requirements
- Android Studio (Hedgehog or newer)
- Android device or emulator (Android 11/API 30+)
- Internet connection for downloading books

### Installation

1. Clone the repository:
```bash
git clone https://gitlab.com/dawson-cst-cohort-2026/511/section1/Caden-Artem-Alisina/book-reading-app.git
cd book-reading-app
```

2. Open in Android Studio and wait for Gradle sync

3. Run on your device or emulator

4. On first launch, the app automatically downloads three sample books from Project Gutenberg (takes 30-60 seconds)

### Using the App

1. **Browse books** - View your library on the home screen
2. **Download new books** - Tap the + button and enter a URL or use pre-filled URLs
3. **Read** - Tap a book cover, select a chapter, then read with swipe navigation
4. **Search** - Use the Search tab to find text within your current book
5. **Listen** - Use the Play button at the bottom of the reading screen for text-to-speech
6. **Immersive mode** - Tap anywhere while reading to hide/show navigation

## Testing

### Run Unit Tests
```bash
./gradlew test
```

Tests include:
- DAO tests (BookDao, ChapterDao, ReadingProgressDao)
- Repository tests (all repositories)
- ViewModel tests (AppViewModel, TTSViewModel)

### Run Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

Tests include:
- All five screen UI tests
- Navigation tests
- User interaction tests
- Integration tests

## Development Process

### Git Workflow
- **main** - Stable production branch
- **dev** - Integration branch
- **project-m1/m2/m3** - Milestone branches
- **Feature branches** - Individual features

We commit frequently (every 15 minutes on average), use merge requests for all changes, and require code reviews from other team members before merging.

### Code Quality
- Separation of concerns with distinct UI, ViewModel, and Data layers
- Small, focused functions
- All strings in string resources for localization
- Comprehensive comments for complex logic
- Consistent naming conventions

## Project Status

✅ **Milestone 1** - Completed (October 2024)
✅ **Milestone 2** - Completed (November 2024)
✅ **Milestone 3** - Completed (December 2024)

All features are fully implemented, tested, and integrated.

## Acknowledgments

This project was built for Dawson College's 420-511 Mobile Development course (Fall 2025) taught by C. Davis.

Code references:
- Android Developer Documentation
- Android Codelabs for testing patterns
- UnzipUtil from Nitin Prakash's GitHub Gist
- Books from Project Gutenberg (public domain)
