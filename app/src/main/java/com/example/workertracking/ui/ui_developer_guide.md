# UI Package Developer Guide

This package contains all Jetpack Compose UI code, navigation definitions, shared components, and ViewModels.

## Subpackages

- **components**: Reusable Compose pieces (dialogs, selectors, shared controls).
- **navigation**: Route definitions and bottom navigation metadata.
- **screens**: Feature screens grouped by domain (dashboard, events, projects, etc.).
- **theme**: Material 3 theme configuration (colors, typography, theme wrapper).
- **viewmodel**: ViewModels for screens and supporting UI state.

## Usage Notes

- Screens should only depend on their ViewModel and reusable components.
- Shared UI is extracted into **components** when reused across multiple screens.
- Top-level composables expose a `modifier` parameter to support layout control from navigation.
- Screens that display money format numbers with the current locale.
- Shift payment dialogs cover both direct and reference worker payments.
- Payment totals in ViewModels should flow through `util/PaymentCalculator`.
