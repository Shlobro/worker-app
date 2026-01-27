# UI Package Developer Guide

This package contains all the User Interface related code, including Composables, Navigation, Themes, and reusable Components.

## Sub-packages

- **components**: Reusable UI components used across multiple screens (e.g., Dialogs, Cards).
- **navigation**: Navigation definitions, routes, and bottom bar configurations.
- **screens**: The actual screens of the application, organized by feature.
- **theme**: Jetpack Compose theme definitions (Colors, Typography, Shapes).
- **viewmodel**: Contains the ViewModels for the screens (sometimes located here or in a parallel package).

## Architecture

The app uses Jetpack Compose for the UI. It follows a unidirectional data flow pattern (MVI/MVVM) where ViewModels expose StateFlows that the UI observes.
