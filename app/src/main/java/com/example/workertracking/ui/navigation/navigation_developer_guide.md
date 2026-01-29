# Navigation Package Developer Guide

This package contains the navigation metadata used by the app's NavHost and bottom bar.

## Files

- **BottomNavItem.kt**: Defines bottom navigation destinations (label, icon, and route).
- **Screen.kt**: Defines all navigation routes and route helpers used by the NavHost.

## Usage

Routes in `Screen` are referenced by `MainActivity` when building the NavHost and when navigating from screens.
