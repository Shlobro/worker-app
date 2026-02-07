# Dashboard Screen Developer Guide

This folder contains the dashboard overview screen.

## Files

- **DashboardScreen.kt**: Main landing screen showing high-level totals and recent activity summaries.

## Key Composables

### DashboardScreen
Top-level screen with a `LazyColumn` displaying cards: financial summary, money owed, active projects, upcoming events. Accepts navigation callbacks and a `DashboardViewModel`.

### MoneyOwedCard
Displays the total outstanding debt amount. Styles adapt based on loading state and whether there is debt:
- **Loading**: Uses `surfaceVariant` colors with an info icon, displays "—" as amount placeholder, and shows "Loading payment status…" subtitle to avoid showing misleading financial status before data arrives.
- **Has debt (> 0)**: Uses `errorContainer` colors with a warning icon to draw attention. Shows formatted currency amount and "Total Pending Payments" subtitle.
- **No debt (<= 0)**: Uses `surfaceVariant` colors with a check-circle icon for a calm, neutral look. Shows formatted currency amount and "All payments are up to date" subtitle.

Clicking the card navigates to the detailed `MoneyOwedScreen`.

### FinancialSummaryCard
Shows total revenue, total expenses, and net profit/loss.

### ActiveProjectsCard / UpcomingEventsCard
Horizontal scrolling rows of project/event summary cards with "View All" navigation.

### DateFilterChip / DateRangePickerDialog
Date range filter controls that affect all dashboard data via `DashboardViewModel`.
