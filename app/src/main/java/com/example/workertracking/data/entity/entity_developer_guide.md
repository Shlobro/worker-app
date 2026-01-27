# Entity Package Developer Guide

This package contains the data classes that represent the database tables and complex query results.

## Database Tables (Entities)

- **Employer.kt**: Represents an employer.
- **Event.kt**: Represents a work event.
- **EventWorker.kt**: Cross-reference table for the many-to-many relationship between Events and Workers.
- **Payment.kt**: Represents a payment made to a worker.
- **Project.kt**: Represents a project.
- **ProjectIncome.kt**: Represents income associated with a project.
- **Shift.kt**: Represents a work shift.
- **ShiftWorker.kt**: Cross-reference table for Shift assignments.
- **Worker.kt**: Represents a worker.

## Data Views (POJOs)

- **UnpaidInfo.kt**: Helper class to represent unpaid amount information.
- **WorkerWithDebt.kt**: Helper class or view to represent a worker along with their calculated debt/credit.

## Conventions

- Each entity class is annotated with `@Entity`.
- Primary keys are annotated with `@PrimaryKey`.
- Foreign keys are defined within the `@Entity` annotation to ensure referential integrity.
