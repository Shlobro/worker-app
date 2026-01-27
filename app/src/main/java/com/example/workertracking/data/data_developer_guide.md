# Data Package Developer Guide

This package contains the data layer of the application, responsible for data persistence and retrieval. It is built using the Room persistence library.

## Files

- **WorkerTrackingDatabase.kt**: The main database holder class. It is an abstract class that extends `RoomDatabase`. It defines the database configuration and serves as the main access point for the underlying connection to the app's persistent data.
- **Converters.kt**: Contains type converters for Room. Since Room only allows primitive types and Strings to be stored, these converters are used to map complex objects (like `Date` or custom Enums) to types that can be persisted.

## Sub-packages

- **dao**: Contains Data Access Objects (DAOs). These interfaces define the methods for accessing the database.
- **entity**: Contains the data classes that represent the database tables.

## Usage

The `WorkerTrackingDatabase` is typically instantiated as a singleton (handled in the `di` package) and provides instances of the DAOs.
