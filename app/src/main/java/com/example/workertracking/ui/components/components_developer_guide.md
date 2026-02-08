# Components Package Developer Guide

This package contains reusable Jetpack Compose UI components shared across screens.

## Files

- **AddWorkerDialog.kt**: Two-phase dialog for adding a worker (used in shift/event flows). Phase 1 shows a searchable worker list with clickable cards that set `selectedWorker` state; Phase 2 (after selection) shows a scrollable payment form with the selected worker displayed in a compact card with a change button. Supports reference worker (עובד מפנה) payment configuration when applicable. The parent screen must provide `onSearchQueryChange` and `onDismiss` callbacks with proper state management to ensure filtering works and dialog closes correctly.
- **EditEventWorkerDialog.kt**: Dialog for editing a worker assignment inside an event.
- **PaymentDialogs.kt**: Payment entry dialogs for marking payments and amounts.
- **SearchableEmployerSelector.kt**: Searchable dropdown for selecting an employer.
- **SearchableWorkerSelector.kt**: Searchable dropdown for selecting a worker.
- **SharedComponents.kt**: Common buttons, inputs, and small UI helpers used across screens.
- **TimePickerDialog.kt**: Material Design 3 clock-based time picker dialog for intuitive time selection. Uses the native Material 3 TimePicker component with a visual clock interface supporting 24-hour format. Includes utility functions `parseTimeString()` to convert "HH:mm" strings to hour/minute components and `formatTime()` to format hour and minute integers to "HH:mm" strings. Used in shift and event screens for start/end time selection.
