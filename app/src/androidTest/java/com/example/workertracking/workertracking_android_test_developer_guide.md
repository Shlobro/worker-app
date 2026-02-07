# Android Test Package Developer Guide

This package contains instrumented tests that run on device or emulator.

## Files

- **ExampleInstrumentedTest.kt**: Placeholder instrumented test file and basic Android test setup example.
- **ui/components/AddWorkerDialogTest.kt**: Compose UI tests for the AddWorkerDialog two-phase flow. Covers phase transitions, reference payment field visibility, confirm button validation, and state reset on worker switch.

## Usage

Add UI tests and integration tests here, including Room database tests and Compose UI tests. Compose UI tests use `createComposeRule()` and assert against node text/content descriptions.
