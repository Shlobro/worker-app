# Shifts Screen Developer Guide

This folder contains screens for shift management.

## Files

- **ShiftDetailScreen.kt**: Shift detail view with assigned workers, payment status dialogs, and reference payment totals.
- **AddShiftScreen.kt**: Form to create a new shift with Material Design 3 clock-based time pickers for start and end times. Automatically calculates hours worked from time selections with option for manual override. Time is displayed in HH:mm format (24-hour).
- **EditShiftScreen.kt**: Form to update an existing shift with clock-based time pickers and auto-calculated hours. Maintains same UX as AddShiftScreen for consistency.
