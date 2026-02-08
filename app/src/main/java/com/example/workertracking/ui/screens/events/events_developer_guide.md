# Events Screen Developer Guide

This folder contains screens for event management.

## Files

- **EventsScreen.kt**: Event list and entry point to add/edit flows.
- **EventDetailScreen.kt**: Event detail view with worker assignments and totals. Workers and reference payments are displayed in separate `Card` sections — the workers card has a header with an add button separated from the list by a divider. When reference payments exist, they appear in their own card with a distinct secondary container background; this card is only rendered when at least one reference payment is present. Worker addition is handled via `AddWorkerDialog` component with searchable filtering. Dialog state is properly managed with dismissal after successful addition and search query reset.
- **AddEventScreen.kt**: Form to create a new event with optional employer selection and clock-based time pickers. Uses Material Design 3 TimePicker for start and end times, automatically calculating hours with manual override option. Time displayed in HH:mm format (24-hour).
- **EditEventScreen.kt**: Form to update an existing event including employer assignment and time selection. Uses clock-based time pickers matching AddEventScreen UX. Supports changing or clearing the employer via `ExposedDropdownMenuBox` and `SearchableEmployerSelector`. The initial employer selection is derived from the `availableEmployers` list matched against `event.employerId` (no separate async lookup).
- **AddWorkerToEventScreen.kt**: Legacy/unused worker assignment screen. Worker addition is now handled through the `AddWorkerDialog` component in `EventDetailScreen`.

## Employer Selection (Add/Edit Event)

Both `AddEventScreen` and `EditEventScreen` use `ExposedDropdownMenuBox` wrapping a read-only `OutlinedTextField` to select an employer. When an employer is selected, a clear icon appears in the trailing icon slot allowing the user to remove the selection. Tapping the field itself opens `SearchableEmployerSelector`. The employer list comes from `EmployersViewModel` in `MainActivity`, which also computes per-employer financials (profit, income, expenses) — these are unused by the event forms but run in the background. If this becomes a performance concern, consider a lightweight flow that exposes only employer names/IDs.

## Dialog State Management

`EventDetailScreen` manages multiple dialog states (`showAddWorkerDialog`, `showPaymentDialog`, `showEditPaymentDialog`, `showReferencePaymentDialog`, `showReferenceEditPaymentDialog`, `showEditWorkerDialog`, `showDeleteDialog`). All dialogs properly implement:
- `onDismiss` callbacks to close the dialog and reset state
- `onConfirm` callbacks that perform the action AND close the dialog
- Search query management for `AddWorkerDialog` with proper reset on dismiss/confirm

Worker filtering for `AddWorkerDialog` is handled by `filteredWorkers` (lines 68-71) which filters by search query and excludes already-assigned workers.

## When to Edit

- Event list behaviors: **EventsScreen.kt**.
- Event payment math and worker assignment UI: **EventDetailScreen.kt**.
- Employer selection in event forms: **AddEventScreen.kt** and **EditEventScreen.kt**.
