# Repository Package Developer Guide

This package contains the repository layer that coordinates DAOs and provides a clean API for ViewModels.

## Files

- **EmployerRepository.kt**: Employer CRUD and employer-level aggregates.
- **EventRepository.kt**: Event CRUD plus event-worker assignment operations and totals.
- **ProjectRepository.kt**: Project CRUD and project income operations.
- **ShiftRepository.kt**: Shift CRUD and shift-worker assignment operations.
- **WorkerRepository.kt**: Worker CRUD plus worker-related debt/summary calculations.

## Responsibilities

- Expose data as `Flow` streams for live UI updates.
- Centralize write operations so ViewModels do not talk to DAOs directly.
- Encapsulate business logic and calculations tied to each feature.
