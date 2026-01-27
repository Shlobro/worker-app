# WorkerTracking Package Developer Guide

This is the root package of the application source code.

## Files

- **MainActivity.kt**: The single Activity entry point for the application. It sets up the `WorkerTrackingTheme` and hosts the main navigation graph.
- **WorkerTrackingApplication.kt**: The Application class. It initializes the dependency injection container (`AppContainer`) upon app startup.

## Architecture Overview

The app follows the recommended Android architecture:
- **UI Layer**: Jetpack Compose + ViewModels.
- **Data Layer**: Repositories + Room Database (DAOs & Entities).
- **DI**: Manual dependency injection via `AppContainer`.
