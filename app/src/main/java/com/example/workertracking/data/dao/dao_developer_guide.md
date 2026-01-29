# DAO Package Developer Guide

This package contains the Room DAO interfaces. Each DAO is responsible for queries and writes for a specific table or join table.

## Files

- **EmployerDao.kt**: Employer CRUD and employer-focused aggregates.
- **EventDao.kt**: Event CRUD and event list/detail queries.
- **EventWorkerDao.kt**: Join table queries for event-worker assignments and related totals.
- **PaymentDao.kt**: Payment CRUD and payment history queries.
- **ProjectDao.kt**: Project CRUD and project list/detail queries.
- **ProjectIncomeDao.kt**: Project income CRUD and income history queries.
- **ShiftDao.kt**: Shift CRUD and shift list/detail queries.
- **ShiftWorkerDao.kt**: Join table queries for shift-worker assignments.
- **WorkerDao.kt**: Worker CRUD plus worker list/detail queries.

## Notes

- Read operations typically return `Flow` for live updates.
- Write operations are `suspend` functions for use in repositories/viewmodels.
