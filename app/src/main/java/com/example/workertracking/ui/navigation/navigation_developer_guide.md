# Navigation Package Developer Guide

This package handles the application's navigation logic.

## Files

- **BottomNavItem.kt**: Sealed class defining the items in the bottom navigation bar (Dashboard, Events, Projects, etc.).
- **Screen.kt**: Sealed class defining the routes for all screens in the application. It maps screen names to their route strings.

## Usage

The `Screen` sealed class is used by the `NavHost` to define the navigation graph.
