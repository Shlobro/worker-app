# Screens Package Developer Guide

This package contains top-level Compose screens. Feature screens are organized into subpackages.

## Direct Files

- **MoneyOwedScreen.kt**: Displays outstanding payments owed to workers and references.
- **RevenueDetailScreen.kt**: Displays detailed revenue information with month/year selection and a 12-month historical window anchored to the selected month. Year navigation controls use localized accessibility labels.
- **WorkerPhotoGalleryScreen.kt**: Displays and manages a worker's photo gallery.

## Subpackages

- **dashboard**: App overview and summary screen. The MoneyOwedCard displays loading/debt/no-debt states with appropriate colors and icons to avoid showing misleading financial status before data loads.
- **employers**: Employer list, detail, add, and edit screens.
- **events**: Event list, detail, add/edit, and worker assignment screens.
- **projects**: Project list, detail, add/edit, and project income screens (detail includes a locale-aware financial summary and shift list).
- **shifts**: Shift detail, add, and edit screens including worker/reference payment status dialogs.
- **workers**: Worker list, detail, add, and edit screens.
