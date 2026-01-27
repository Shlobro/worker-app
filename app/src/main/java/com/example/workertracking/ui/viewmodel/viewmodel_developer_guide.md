# ViewModel Package Developer Guide

This package contains the ViewModels for the application screens. They extend `ViewModel` and are responsible for preparing and managing the data for the UI.

## Pattern

- ViewModels interact with Repositories to fetch and save data.
- They expose UI state via `StateFlow` or `LiveData` (mostly `StateFlow` in this app).
- Naming convention: `[ScreenName]ViewModel`.

## Files (Key ViewModels)

- **DashboardViewModel.kt**: Logic for the main dashboard.
- **EmployersViewModel.kt** & **Edit/AddEmployerViewModel.kt**: Logic for employer management.
- **EventsViewModel.kt** & **Edit/AddEventViewModel.kt**: Logic for event management.
- **ProjectsViewModel.kt** & **ProjectDetailViewModel.kt**: Logic for project tracking.
- **WorkersViewModel.kt** & **WorkerDetailViewModel.kt**: Logic for worker management.
- **MoneyOwedViewModel.kt**: Logic for calculating and displaying debts.
- **WorkerPhotoGalleryViewModel.kt**: Logic for the photo gallery feature.
