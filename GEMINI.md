# Gemini Project Overview: Worker Tracking Android App

## Project Overview

This is a native Android application for managing construction projects, workers, and work shifts. It's built with Kotlin, Jetpack Compose, and follows the MVVM architecture pattern. The app uses a Room database for local data persistence and includes features for financial tracking, worker management, and shift scheduling.

**Key Technologies:**

*   **UI:** Jetpack Compose with Material 3
*   **Architecture:** MVVM (Model-View-ViewModel) with a Repository pattern
*   **Database:** Room
*   **Asynchronous Operations:** Kotlin Coroutines
*   **Navigation:** Jetpack Navigation Compose
*   **Image Loading:** Coil
*   **JSON Serialization:** Gson

## Building and Running

The project uses Gradle for building. Here are the most common commands:

*   **Build the debug APK:**
    ```bash
    gradlew.bat assembleDebug
    ```
*   **Install the debug APK on a connected device:**
    ```bash
    gradlew.bat installDebug
    ```
*   **Run unit tests:**
    ```bash
    gradlew.bat test
    ```
*   **Run instrumented tests:**
    ```bash
    gradlew.bat connectedAndroidTest
    ```
*   **Run lint checks:**
    ```bash
    gradlew.bat lint
    ```

## Development Conventions

*   **UI:** The UI is built entirely with Jetpack Compose. Follow Material 3 design principles and use existing theme components.
*   **State Management:** ViewModels are used to manage the UI state.
*   **Data:** The Room database is the single source of truth for all data. Repositories are used to abstract data access.
*   **Localization:** The app is localized in Hebrew. Maintain this localization when adding new UI elements.
*   **Code Style:** Follow the standard Kotlin coding conventions.
