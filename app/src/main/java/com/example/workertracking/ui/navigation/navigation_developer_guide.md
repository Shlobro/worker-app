# Navigation Package Developer Guide

This package contains the navigation metadata used by the app's NavHost and bottom bar.

## Files

- **BottomNavItem.kt**: Defines bottom navigation destinations (label, icon, and route).
- **Screen.kt**: Defines all navigation routes and route helpers used by the NavHost.
- **DashboardNavigation.kt**: NavGraphBuilder extension for dashboard, money owed, and revenue detail routes.
- **ProjectsNavigation.kt**: NavGraphBuilder extension for all project-related routes (list, add, edit, detail, income).
- **WorkersNavigation.kt**: NavGraphBuilder extension for all worker-related routes (list, add, edit, detail, photos).
- **EventsNavigation.kt**: NavGraphBuilder extension for all event-related routes (list, add, edit, detail).
- **ShiftsNavigation.kt**: NavGraphBuilder extension for all shift-related routes (add, edit, detail).
- **EmployersNavigation.kt**: NavGraphBuilder extension for all employer-related routes (list, add, edit, detail).

## Navigation Architecture

Navigation is split into feature-based modules to keep MainActivity under 1000 lines. Each navigation file provides a `NavGraphBuilder` extension function (e.g., `dashboardNavigation()`) that registers all routes for that feature. MainActivity calls these extensions in its NavHost to assemble the complete navigation graph.

Routes that require loading related entities by ID (for example Add/Edit shift or add income) show a loading state while the entity is fetched. If the entity is missing, the route exits via `popBackStack()` instead of rendering a blank screen.

## Usage

Routes in `Screen` are referenced by the navigation extension functions and when navigating from screens. MainActivity imports and calls all navigation extension functions to build the complete navigation graph.

Route arguments that may contain reserved route characters (for example worker names used in photo gallery routes) are encoded in `Screen.createRoute(...)` and decoded when read from `NavBackStackEntry` arguments.
