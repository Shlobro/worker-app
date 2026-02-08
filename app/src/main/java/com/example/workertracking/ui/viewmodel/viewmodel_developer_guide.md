# ViewModel Package Developer Guide

This package contains ViewModels that back Compose screens and expose UI state via `StateFlow`.

## Files

- **AddEmployerViewModel.kt**: State and actions for creating employers.
- **AddEventViewModel.kt**: State and actions for creating events.
- **AddIncomeViewModel.kt**: State and actions for adding project income.
- **AddProjectViewModel.kt**: State and actions for creating projects with name, start date, and optional employer.
- **AddShiftViewModel.kt**: State and actions for creating shifts.
- **AddWorkerViewModel.kt**: State and actions for creating workers.
- **DashboardViewModel.kt**: Aggregated dashboard totals and lists. Financial totals are scoped to the current month, while active projects and upcoming events are not date-filtered by user input. Income and expense totals are computed using one shared month range per refresh so both values stay aligned during month boundaries.
- **EditEmployerViewModel.kt**: State and actions for editing employers.
- **EmployerDetailViewModel.kt**: Employer detail data and updates.
- **EmployersViewModel.kt**: Employer list data and filters.
- **EventDetailViewModel.kt**: Event detail data, worker assignments, and event updates. `updateEvent(...)` persists all event fields including `employerId` (nullable — pass `null` to clear employer assignment, a valid ID to set one). The `employerId` parameter is required (no default) to force callers to be explicit.
- **EventsViewModel.kt**: Event list data and filters.
- **MoneyOwedViewModel.kt**: Outstanding payment totals and grouping.
- **ProjectDetailViewModel.kt**: Project detail data, shifts, and income. Updates project name and start date (location is optional and stored in the database).
- **ProjectsViewModel.kt**: Project list data and filters.
- **RevenueDetailViewModel.kt**: Monthly revenue aggregation for detailed revenue analysis. Loads events and shifts once per refresh, resolves per-item costs once, then computes selected-month and a 12-month history window anchored to the currently selected month/year.
- **ShiftDetailViewModel.kt**: Shift detail data, worker assignments, and payment updates (direct and reference).
- **WorkerDetailViewModel.kt**: Worker detail data, payments, and related history. Exposes two separate debt totals: `totalOwed` (what is owed directly to the worker, excludes referral payouts) and `totalReferenceOwed` (referral commissions owed to this worker when they are the referrer). These are intentionally independent calculations.
- **WorkerPhotoGalleryViewModel.kt**: Worker photo gallery state and actions.
- **WorkersViewModel.kt**: Worker list data and filters.

## Usage Notes

- ViewModels should call repositories for all data access.
- UI screens should observe state flows rather than perform direct queries.
- Payment summaries should use `util/PaymentCalculator` to keep calculations consistent.
