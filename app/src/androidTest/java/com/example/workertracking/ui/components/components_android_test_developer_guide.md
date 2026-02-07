# Components Android Test Package Developer Guide

Compose UI tests for reusable components in the main `ui/components` package.

## Files

- **AddWorkerDialogTest.kt**: Tests for the two-phase AddWorkerDialog. Covers phase transitions (worker selection to payment form), reference payment field visibility based on `referenceId`, confirm button enable/disable validation, and state reset when switching workers.

## Running Tests

These are instrumented tests requiring a connected device or emulator:

```
./gradlew connectedDebugAndroidTest
```

## Conventions

- Tests use `createComposeRule()` to render composables in isolation.
- Node matching uses `onNodeWithText` and `onNodeWithContentDescription` against visible UI strings.
- Test data uses inline `Worker` instances with known IDs to control reference relationships.
