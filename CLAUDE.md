# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Development Commands

### Building the Application
- **Debug build**: `./gradlew assembleDebug` or `gradlew.bat assembleDebug` (Windows)
- **Release build**: `./gradlew assembleRelease` or `gradlew.bat assembleRelease` (Windows)
- **Install on device**: `./gradlew installDebug` or `gradlew.bat installDebug` (Windows)

### Testing
- **Unit tests**: `./gradlew test` or `gradlew.bat test` (Windows)
- **Instrumented tests**: `./gradlew connectedAndroidTest` or `gradlew.bat connectedAndroidTest` (Windows)
- **Single test class**: `./gradlew test --tests "com.example.workertracking.ExampleUnitTest"`

### Code Quality
- **Lint check**: `./gradlew lint` or `gradlew.bat lint` (Windows)
- **Clean build**: `./gradlew clean` or `gradlew.bat clean` (Windows)

## Project Architecture

### Overview
Worker Tracking is an Android application for managing projects, workers, and work shifts. The app is designed to be fully localized in Hebrew and built using modern Android development practices with Jetpack Compose.

### Core Architecture
- **MVVM Pattern**: ViewModels manage UI state and business logic
- **Repository Pattern**: Data layer abstraction with repositories
- **Dependency Injection**: Manual DI using AppContainer
- **Room Database**: Local data persistence with SQLite
- **Jetpack Compose**: Modern declarative UI framework

### Module Structure

#### Data Layer (`com.example.workertracking.data`)
- **Database**: `WorkerTrackingDatabase` - Room database with TypeConverters
- **Entities**: Core data models (Project, Worker, Shift, Event, EventWorker, Payment)
- **DAOs**: Database access objects for each entity
- **Converters**: Type converters for Room (date/time handling)

#### Domain Layer (`com.example.workertracking.repository`)
- **ProjectRepository**: Manages project-related data operations
- **WorkerRepository**: Handles worker and payment data
- **EventRepository**: Manages event data operations

#### Presentation Layer (`com.example.workertracking.ui`)
- **Screens**: Feature-based screen composables organized by domain
  - Dashboard: Financial overview and main navigation
  - Projects: Project management (list, add, detail)
  - Workers: Worker management (list, add, detail)
  - Events: Event management (list, add)
- **ViewModels**: UI state management following MVVM pattern
- **Navigation**: Bottom navigation with Jetpack Navigation Compose
- **Theme**: Material 3 theming with custom colors and typography

#### Dependency Injection (`com.example.workertracking.di`)
- **AppContainer**: Manual dependency injection container
- Provides repositories with database dependencies

### Key Technical Decisions

#### Database Schema
- **Projects**: Store project details, income tracking (rate-based or fixed)
- **Workers**: Worker profiles with phone numbers and optional reference relationships (no percentage stored)
- **Shifts**: Individual work sessions linked to projects and workers
- **Events**: One-off events that can have multiple workers
- **EventWorker**: Many-to-many relationship between events and workers with hourly reference payment rates
- **Payments**: Payment history and tracking

#### Navigation Structure
- Bottom navigation for main screens (Dashboard, Projects, Workers, Events)
- Hierarchical navigation for add/detail screens
- Conditional bottom bar display (hidden on secondary screens)

#### State Management
- ViewModels use StateFlow for reactive UI updates
- Repository pattern provides clean separation of concerns
- Manual DI through AppContainer for simplicity

#### Worker Management Requirements
- **Worker Creation**: Workers are created with name, phone number, and optional reference worker (NO payment rates stored on worker)
- **Phone Integration**: Worker phone numbers are clickable and trigger phone dialer intent
- **Search Functionality**: Workers can be searched by name in the workers list
- **Worker Details**: Clicking on a worker shows their details and only projects they participate in through shifts
- **Payment Rates**: Payment rates are set per shift or per event, NOT stored on worker entity
- **Database Schema**: Worker entity uses `phoneNumber` field without any payment rate fields

### Project-Worker Relationship (Through Shifts Only)
- **No Direct Assignment**: Workers cannot be added directly to projects
- **Shift-Based Only**: Workers are linked to projects only through shifts
- **Project Details**: Project detail screens show shifts, not workers directly
- **Worker Participation**: Workers can work on any project by creating shifts

### Shift Management (Primary Workflow)
- **Shift Creation**: Shifts can be created for ANY worker and ANY project with date, start time, hours, and pay rate
- **No Pre-Assignment Required**: Workers don't need to be "assigned" to projects before creating shifts
- **Payment Per Shift**: Each shift has its own pay rate specified during creation
- **Project View**: Projects show their shifts with worker names, dates, hours, and payments

### Event-Worker Relationship  
- **Direct Assignment**: Workers can be assigned directly to events with hours and hourly rates
- **Payment Per Event**: Each worker-event assignment has specific hours and pay rate
- **Total Calculation**: Event total cost calculated as (hours × hourly rate) per worker
- **EventWorker Entity**: Links events to workers with hours and pay rate

### Development Guidelines

#### Adding New Features
1. Create entity in `data/entity` if new data model needed
2. Add DAO methods in `data/dao` for database operations
3. Update repository in `repository` package for business logic
4. Create ViewModel in `ui/viewmodel` for UI state management
5. Build Compose screens in `ui/screens` following existing patterns
6. Update navigation in `MainActivity` if new routes needed

#### Database Changes
- Update database version in `WorkerTrackingDatabase`
- Add migration strategy for schema changes
- Update relevant DAOs and entities
- Test migrations thoroughly

#### UI Development
- Follow Material 3 design principles
- Use existing theme components in `ui/theme`
- Maintain Hebrew localization (strings in `values-he/strings.xml`)
- Follow existing screen structure and naming conventions

#### Worker Feature Implementation
1. **Adding Workers**: Use `phoneNumber` field, avoid `contactInfo` or `referencePercentage`
2. **Phone Dialer Integration**: Use `Intent(Intent.ACTION_DIAL)` with `tel:` URI scheme
3. **Search Implementation**: Filter workers by name using case-insensitive contains()
4. **Worker Detail Screen**: Show worker info, projects, and events with search capability
5. **Reference Payment**: Prompt for hourly rate in shekels during project/event assignment
6. **String Resources**: Use "מספר טלפון" for phone number, "חפש עובדים" for search

### Target API Levels
- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 35 (Android 15)
- **compileSdk**: 35

### Key Dependencies
- **Jetpack Compose**: UI framework with Material 3
- **Room**: Database ORM with KSP annotation processing
- **Navigation Compose**: In-app navigation
- **Lifecycle ViewModel**: State management
- **Kotlin Coroutines**: Asynchronous programming
- update the plan file and claude.md whenever there is anything important changed