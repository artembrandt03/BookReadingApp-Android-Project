# Book Reading App — Android (Kotlin)

An Android application that allows users to **download, store, and read HTML-formatted books** (e.g., from Project Gutenberg).  
The app supports chapter navigation, full-text search, text-to-speech narration, localization, and automatic reading progress tracking!

This project was developed as part of **Dawson College – 420-511 Mobile Development**.

---

## Team

- **Artem Brandt**
- Alisina Zahabsaniei  
- Caden Ho  

---

## Overview

The Book Reading App delivers a complete mobile reading experience using modern Android development practices.  
Users can download public-domain books, read them in a clean and immersive interface, search across entire books, and listen to content using text-to-speech.

All data is stored locally on the device, enabling **offline reading** once books are downloaded.

---

## Features

### Reading Experience
- Download HTML books from Project Gutenberg or custom URLs  
- Clean, distraction-free reading interface  
- Vertical scrolling within chapters  
- Horizontal swipe navigation between chapters  
- Immersive fullscreen reading mode  
- Automatically resumes reading from last position  

### Book Management
- Library view with book covers and metadata  
- Automatic HTML parsing into chapters  
- Local storage using Room database  
- Cover images displayed in the library  

### Search & 🔊 Audio
- Full-text search across entire books  
- Contextual search results with surrounding text  
- Text-to-Speech (TTS) narration  
- Play / pause / stop controls  
- TTS automatically syncs with chapter changes  

### Localization
- Full support for **English** and **French**  
- Automatically adapts to device language settings  

---

## Architecture & Project Structure

The project follows **MVVM (Model–View–ViewModel)** architecture with clear separation of concerns.

```
app/
├── data/            # Room DB, DAOs, entities, repositories
├── di/              # Hilt dependency injection modules
├── ui/              # Compose UI, navigation, screens, components
├── vm/              # ViewModels and state management
├── utils/           # Utilities (immersive mode, downloads)
└── MainActivity.kt  # Application entry point
```

All asynchronous operations use **Kotlin Coroutines**.

---

## Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose + Material 3  
- **Architecture:** MVVM  
- **Persistence:** Room Database  
- **Dependency Injection:** Hilt  
- **Networking:** OkHttp  
- **HTML Parsing:** Jsoup  
- **Async:** Kotlin Coroutines  
- **Testing:** JUnit, MockK, Espresso, Compose UI Test  

---

## Running the App Locally

### Requirements
- Android Studio Hedgehog or newer  
- Android Emulator or device (API 30+)  

### Steps
1. Clone the repository  
2. Open the project in Android Studio  
3. Wait for Gradle sync  
4. Run on an emulator or physical device  

On first launch, the app automatically downloads sample books.

---

## Testing

### Unit Tests
```
./gradlew test
```

### Instrumentation Tests
```
./gradlew connectedAndroidTest
```

---

## Media

Screenshots and demo videos of the application are included in the repository  
or provided separately for presentation and evaluation.

---