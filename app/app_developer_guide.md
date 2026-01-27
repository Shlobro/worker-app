# App Module Developer Guide

This is the main application module.

## Key Files

- **build.gradle.kts**: The build configuration for the app module. It defines dependencies, SDK versions, and build types.
- **src/main/AndroidManifest.xml**: The Android manifest file, describing the app components (Activity, Application) and permissions.
- **proguard-rules.pro**: ProGuard rules for code shrinking and obfuscation.

## Folders

- **src/main/java**: Kotlin source code.
- **src/main/res**: Android resources (layouts, drawables, strings, etc.).
- **src/test**: Local unit tests.
- **src/androidTest**: Instrumented tests.
