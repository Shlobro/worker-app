# Dependency Injection Package Developer Guide

This package provides the manual dependency injection container used by the app.

## Files

- **AppContainer.kt**: Builds the `WorkerTrackingDatabase` and exposes repository singletons (ProjectRepository, WorkerRepository, EventRepository, ShiftRepository, EmployerRepository). Use this when wiring new repositories or database access.

## How It Is Used

`WorkerTrackingApplication` creates the AppContainer at startup so ViewModels and screens can access repositories.
