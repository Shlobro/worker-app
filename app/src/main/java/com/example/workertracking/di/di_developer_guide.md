# Dependency Injection Package Developer Guide

This package manages dependency injection for the application.

## Files

- **AppContainer.kt**: Implements a manual dependency injection container. It holds the singleton instance of the `WorkerTrackingDatabase` and provides repository instances (`WorkerRepository`, `EventRepository`, etc.) to the rest of the application.

## Usage

The `AppContainer` is initialized in the `WorkerTrackingApplication` class. ViewModels or other components access dependencies through this container.
