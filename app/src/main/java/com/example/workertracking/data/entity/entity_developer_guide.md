# Entity Package Developer Guide

This package contains Room entities (tables) and projection models used by queries.

## Room Entities (Tables)

- **Employer.kt**: Employer/company records.
- **Event.kt**: Standalone event records with time and income fields.
- **EventWorker.kt**: Join table for event-worker assignments and pay info.
- **Payment.kt**: Payment records made to workers.
- **Project.kt**: Project records with status and dates.
- **ProjectIncome.kt**: Income entries attached to a project.
- **Shift.kt**: Work shift records tied to a project.
- **ShiftWorker.kt**: Join table for shift-worker assignments and pay info.
- **Worker.kt**: Worker records including optional reference worker link and photos.

## Projection / Helper Models

- **UnpaidInfo.kt**: Lightweight projection for unpaid amounts and counts.
- **WorkerWithDebt.kt**: Combines a Worker with computed debt totals.
- **WorkerWithDebtData.kt**: Aggregated fields for worker debt and totals with helpers to map to Worker/WorkerWithDebt.
