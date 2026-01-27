# DAO Package Developer Guide

This package contains the Data Access Objects (DAOs) for the Room database. Each DAO provides methods for reading and writing data to a specific table or set of tables.

## Files

- **EmployerDao.kt**: Database access for `Employer` entities.
- **EventDao.kt**: Database access for `Event` entities.
- **EventWorkerDao.kt**: Database access for `EventWorker` join entities (many-to-many relationship).
- **PaymentDao.kt**: Database access for `Payment` entities.
- **ProjectDao.kt**: Database access for `Project` entities.
- **ProjectIncomeDao.kt**: Database access for `ProjectIncome` entities.
- **ShiftDao.kt**: Database access for `Shift` entities.
- **ShiftWorkerDao.kt**: Database access for `ShiftWorker` join entities.
- **WorkerDao.kt**: Database access for `Worker` entities.

## Key Concepts

- All methods are either `suspend` functions (for one-shot operations) or return `Flow` (for observing data changes).
- We use standard SQL queries via the `@Query` annotation for complex operations.
- Basic CRUD operations are handled via `@Insert`, `@Update`, and `@Delete` annotations.

## Performance Optimizations

### WorkerDao - Aggregated Queries

**getAllWorkersWithDebtData()**: This query performs a single SQL operation to fetch all workers with their complete debt data, avoiding N+1 query problems. It uses multiple LEFT JOINs with subqueries to aggregate:
- Unpaid shifts totals and counts
- Unpaid events totals and counts
- Reference payment totals and counts (when the worker is the reference worker)
- Total earnings across all shifts and events
- Reference worker name

This single query replaces what previously required 1 + (4 * N) queries where N is the number of workers.
