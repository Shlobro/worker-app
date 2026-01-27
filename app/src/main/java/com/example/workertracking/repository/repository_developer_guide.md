# Repository Package Developer Guide

This package contains the Repository layer, which acts as a single source of truth for data. It mediates between the data sources (DAOs) and the UI/Domain layer (ViewModels).

## Files

- **EmployerRepository.kt**: Manages data operations for Employers.
- **EventRepository.kt**: Manages data operations for Events.
- **ProjectRepository.kt**: Manages data operations for Projects and Project Income.
- **ShiftRepository.kt**: Manages data operations for Shifts.
- **WorkerRepository.kt**: Manages data operations for Workers.

## Responsibilities

- Exposing data as `Flow` streams to the ViewModels.
- Executing suspend functions for database writes on appropriate dispatchers (though Room handles this, Repositories provide the API).
- Abstraction: The UI layer interacts with these repositories rather than directly with the DAOs.
