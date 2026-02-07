# Components Package Developer Guide

This package contains reusable Jetpack Compose UI components shared across screens.

## Files

- **AddWorkerDialog.kt**: Two-phase dialog for adding a worker (used in shift/event flows). Phase 1 shows a searchable worker list; Phase 2 (after selection) shows a scrollable payment form with the selected worker displayed in a compact card. Supports reference worker (עובד מפנה) payment configuration when applicable.
- **EditEventWorkerDialog.kt**: Dialog for editing a worker assignment inside an event.
- **PaymentDialogs.kt**: Payment entry dialogs for marking payments and amounts.
- **SearchableEmployerSelector.kt**: Searchable dropdown for selecting an employer.
- **SearchableWorkerSelector.kt**: Searchable dropdown for selecting a worker.
- **SharedComponents.kt**: Common buttons, inputs, and small UI helpers used across screens.
