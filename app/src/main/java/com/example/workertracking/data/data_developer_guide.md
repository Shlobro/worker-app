# Data Package Developer Guide

This package defines the Room database setup and shared type converters used by DAOs and entities.

## Files

- **WorkerTrackingDatabase.kt**: Room database configuration, entity list, DAO accessors, migrations, and the singleton builder (`getDatabase`).
- **Converters.kt**: Type converters for Room (Date <-> Long and List<String> <-> JSON for stored photo URIs).

## Subpackages

- **dao**: DAO interfaces and SQL queries.
- **entity**: Room entities and projection data classes.
