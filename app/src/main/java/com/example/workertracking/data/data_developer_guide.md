# Data Package Developer Guide

This package defines the Room database setup and shared type converters used by DAOs and entities.

## Files

- **WorkerTrackingDatabase.kt**: Room database configuration, entity list, DAO accessors, migrations (currently at version 26), and the singleton builder (`getDatabase`). Migration 25-26 makes the Project location field nullable.
- **Converters.kt**: Type converters for Room (Date <-> Long and List<String> <-> JSON for stored photo URIs).

## Subpackages

- **dao**: DAO interfaces and SQL queries.
- **entity**: Room entities and projection data classes.
