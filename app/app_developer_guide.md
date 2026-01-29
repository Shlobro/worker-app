# App Module Developer Guide

This is the main Android application module.

## Files

- **build.gradle.kts**: Module build configuration (SDK versions, dependencies, Compose setup).
- **proguard-rules.pro**: ProGuard/R8 rules for release builds.
- **.gitignore**: Module-specific ignore rules.
- **Plan**: Freeform product notes for upcoming tasks.
- **todo's**: Short backlog of feature behavior notes.
- **app_developer_guide.md**: This guide.

## Folders

- **src/main**: Production code and resources.
  - **AndroidManifest.xml**: App manifest and component declarations.
  - **java/com/example/workertracking**: Kotlin source root (see `workertracking_developer_guide.md`).
  - **res**: Android resources used by Compose and the system.
    - **drawable**: Vector assets and images.
    - **mipmap-***: App launcher icons.
    - **values**: Default resources (strings, colors, styles).
    - **values-en**: English localized resources.
    - **values-he**: Hebrew localized resources.
    - **values-he-rIL**: Hebrew (Israel) localized resources.
    - **xml**: XML configuration resources.
- **src/test**: JVM unit tests (see `workertracking_test_developer_guide.md`).
- **src/androidTest**: Instrumented tests on device/emulator (see `workertracking_android_test_developer_guide.md`).
