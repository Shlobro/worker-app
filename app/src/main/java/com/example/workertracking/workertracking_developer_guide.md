# WorkerTracking Package Developer Guide

This package is the root of the Kotlin source code for the app. It is the entry point for the UI layer and the top-level dependency wiring.

## Files

- **MainActivity.kt**: The single Activity. It sets the Compose content, hosts the NavHost, and applies the app theme and scaffolding.
- **WorkerTrackingApplication.kt**: Application class. Creates the AppContainer so repositories and the database are available app-wide.

## Subpackages

- **data**: Room database configuration, DAOs, and entities.
- **di**: Manual dependency injection container (AppContainer).
- **repository**: Data access and business logic used by ViewModels.
- **ui**: Compose UI, navigation, themes, shared components, and ViewModels.

## Where to Start

- For app-level navigation or layout: **MainActivity.kt**.
- For dependency wiring: **WorkerTrackingApplication.kt** and **di/AppContainer.kt**.
- For project-specific UI behavior: **ui/screens/projects/ProjectDetailScreen.kt** and related screens.
- For shift payment workflows (including reference payments): **ui/screens/shifts/ShiftDetailScreen.kt** and **ui/viewmodel/ShiftDetailViewModel.kt**.
- For currency/financial formatting: screens use locale-aware number formatting in their summaries.
