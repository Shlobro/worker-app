# Events Screen Developer Guide

This package handles screens related to Event management.

## Files

- **EventsScreen.kt**: List of all events with navigation to details and add screens.
- **EventDetailScreen.kt**: Details view for a single event, showing worker assignments, financial summary, and payment tracking.
- **AddEventScreen.kt**: Form to create a new event with validation for required fields including event name, times, and hours.
- **EditEventScreen.kt**: Form to edit an existing event with the same validation rules as AddEventScreen.
- **AddWorkerToEventScreen.kt**: Screen to assign workers to a specific event with payment configuration.

## Key Validation Rules

### AddEventScreen & EditEventScreen
Both screens enforce the following validation rules before allowing save:
- Event name must not be blank
- Start time and end time must be 3-4 digits in valid time format (validated on input)
- **Hours must be a valid number greater than 0** - this is critical to prevent 0-hour worker assignments that break payment calculations
- The save button is disabled until all validations pass
- Error messages are displayed for invalid hours input

### Time Input
Time fields use `TimeInputVisualTransformation` to format 3-4 digit input as HH:MM:
- Validates hours (0-23) and minutes (0-59) on input
- Automatically calculates hours from start/end times
- Users can manually override calculated hours (disables auto-calculation)

## Data Flow
Events store hours as a String in the database but screens validate it can be parsed to a valid Double > 0 before saving.
