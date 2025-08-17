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
- **Database**: `WorkerTrackingDatabase` - Room database with TypeConverters (Version 14)
- **Entities**: Core data models (Project, ProjectIncome, Worker, Shift, ShiftWorker, Event, EventWorker, Payment)
- **DAOs**: Database access objects for each entity
- **Converters**: Type converters for Room (date/time handling)

#### Domain Layer (`com.example.workertracking.repository`)
- **ProjectRepository**: Manages project-related data operations and income tracking
- **WorkerRepository**: Handles worker and payment data
- **ShiftRepository**: Manages shifts and worker-shift relationships with payment calculations
- **EventRepository**: Manages event data operations

#### Presentation Layer (`com.example.workertracking.ui`)
- **Screens**: Feature-based screen composables organized by domain
  - Dashboard: Comprehensive analytics dashboard with financial overview, active projects, upcoming events, money owed tracking, and date filtering
  - Projects: Project management (list, add, edit, detail) with financial tracking
  - Shifts: Individual shift management with multi-worker support and payment configuration (add, edit)
  - Workers: Worker management (list, add, edit, detail)
  - Events: Event management (list, add, edit)
  - Income: Project income entry and tracking
- **ViewModels**: UI state management following MVVM pattern
- **Navigation**: Bottom navigation with Jetpack Navigation Compose
- **Theme**: Material 3 theming with custom colors and typography

#### Dependency Injection (`com.example.workertracking.di`)
- **AppContainer**: Manual dependency injection container
- Provides repositories with database dependencies

### Key Technical Decisions

#### Database Schema
- **Projects**: Store project details (name, location, dates, status) - income managed separately through ProjectIncome
- **ProjectIncome**: Individual income entries per project (date, description, amount, units)
- **Workers**: Worker profiles with phone numbers and optional reference relationships (no payment rates stored)
- **Shifts**: Individual work sessions linked to projects (no direct worker relationship)
- **ShiftWorker**: Many-to-many relationship between shifts and workers with payment configuration and optional reference payment rates
- **Events**: One-off events that can have multiple workers with start/end times and duration tracking
- **EventWorker**: Many-to-many relationship between events and workers with hourly reference payment rates
- **Payments**: Payment history and tracking

#### Navigation Structure
- Bottom navigation for main screens (Dashboard, Projects, Workers, Events)
- Hierarchical navigation for add/edit/detail screens
- Conditional bottom bar display (hidden on secondary screens)
- Full edit capability for all entity types (Projects, Workers, Events, Shifts)

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
- **Photo Management**: Workers can have photo albums with sharing capabilities
- **Reference Worker Prompts**: When adding shifts for workers with references, prompt for reference payment rate

### Project-Worker Relationship (Through Shifts Only)
- **No Direct Assignment**: Workers cannot be added directly to projects
- **Shift-Based Only**: Workers are linked to projects only through shifts
- **Project Details**: Project detail screens show shifts, not workers directly
- **Worker Participation**: Workers can work on any project by creating shifts

### Shift Management (Enhanced Multi-Worker Workflow)
- **Shift Creation**: Shifts created without worker selection (date, start/end time, hours only)
- **Worker Management**: After creating shift, click to add/manage workers individually
- **Payment Configuration**: Each worker can have different payment types:
  - **שכר שעתי** (Hourly Rate): payRate × shiftHours
  - **סכום גלובלי** (Global Amount): fixed amount regardless of hours
- **Reference Worker Payment System**: When adding workers with reference relationships:
  - Prompt user for reference worker hourly rate during assignment
  - Display reference worker name and payment information on worker cards
  - Include reference payments in total shift cost calculation
  - Reference payments are always calculated hourly (rate × shiftHours)
- **Search Functionality**: Searchable worker addition with real-time filtering
- **Individual Control**: Add, edit, or remove workers from shifts independently
- **Cost Calculation**: Automatic shift cost calculation based on all assigned workers including reference payments

### Event-Worker Relationship  
- **Direct Assignment**: Workers can be assigned directly to events with hours and hourly rates
- **Payment Per Event**: Each worker-event assignment has specific hours and pay rate
- **Total Calculation**: Event total cost calculated as (hours × hourly rate) per worker
- **EventWorker Entity**: Links events to workers with hours and pay rate
- **Event Income**: Events have payment amounts for profit calculation
- **Event Details**: Full event detail screens with worker management
- **Event Editing**: Complete edit functionality for events
- **Event Deletion**: Events can be deleted like other entities
- **Event Search**: Events are searchable by name
- **Time Management**: Events have start/end times with smart formatting and auto-calculation like shifts
- **Smart Time Input**: Auto-formatting for time inputs (HHMM → HH:MM) with validation
- **Duration Calculation**: Real-time hours calculation from start/end times with manual override capability

### Financial Management System

#### Project Financial Tracking
- **Income Entries**: Add multiple income entries per project (daily, weekly, monthly work)
- **Flexible Units**: Support for various income types (days × daily_rate, hours × hourly_rate)
- **Real-time Calculations**: Automatic total income calculation from all entries

#### Payment Calculations
- **Worker Payments**: Calculated from ShiftWorker assignments
- **Dual Payment Types**: 
  - Hourly payments: `worker_rate × shift_hours`
  - Global payments: `fixed_amount`
- **Project Totals**: Sum of all worker payments across all shifts

#### Profit Analysis
- **Net Profit**: `Total Income - Total Worker Payments`
- **Real-time Updates**: Financial data refreshes when shifts, workers, or income changes
- **Visual Indicators**: Color-coded profit/loss display (green/red)

#### Financial UI Components
- **Project Detail Financial Summary**: Shows income, payments, and profit with clickable income history
- **Add Income Screen**: User-friendly income entry with unit calculations
- **Shift Detail Financial Impact**: See how workers affect project profitability
- **Dashboard Analytics**: Complete dashboard remake with revenue/expenses/profit filtering by dates
- **Active Projects Overview**: Display of current projects and upcoming events
- **Money Owed Tracking**: Outstanding payments and debt management

### Time Input Enhancement

#### Smart Time Formatting
- **Auto-format**: "0800" → "08:00", "1630" → "16:30"
- **Input Validation**: Prevents invalid times (>23:59)
- **Number Keyboard**: Numeric keypad for time inputs

#### Automatic Hours Calculation
- **Real-time Calculation**: Hours computed from start/end times for both shifts and events
- **Cross-midnight Support**: Handles shifts spanning midnight
- **Manual Override**: Users can edit hours manually for special cases
- **Smart Reset**: "חשב אוטומטית" button to return to automatic calculation
- **Event Integration**: Same time management features available for events as shifts

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
5. **Payment Configuration**: Configure per shift/event, NOT stored on worker entity
6. **String Resources**: Use "מספר טלפון" for phone number, "חפש עובדים" for search

#### Shift Feature Implementation
1. **Shift Creation**: Create shift without worker selection first
2. **Worker Assignment**: Click shift to open detail screen for worker management
3. **Payment Types**: Use "שכר שעתי" and "סכום גלובלי" (not "תשלום קבוע")
4. **Reference Payment System**: Prompt for reference worker rates when adding workers with references
5. **Search Integration**: Implement searchable worker addition dialogs
6. **Financial Updates**: Ensure financial calculations refresh on worker changes including reference payments
7. **Time Formatting**: Implement auto-formatting for time inputs (HHMM → HH:MM)

#### Event Feature Implementation (✅ COMPLETED)
1. **Event Time Management**: Events have startTime, endTime, and hours fields with smart formatting
2. **Time Input Auto-formatting**: Automatic conversion of HHMM → HH:MM format with validation
3. **Duration Calculation**: Real-time hours calculation from start/end times with manual override
4. **AddEventScreen**: Complete time input functionality with auto-calculation
5. **EditEventScreen**: Full editing capabilities with date picker and time management
6. **Database Integration**: Events table updated with time fields (database version 14)
7. **Smart Reset**: "חשב אוטומטית" button for returning to automatic time calculation

#### Income Feature Implementation
1. **Income Tracking**: Create ProjectIncome entries with date, description, amount, units
2. **Flexible Entry**: Support various income types (daily, weekly, hourly, fixed)
3. **Unit Calculations**: Show real-time total (amount × units)
4. **Financial Integration**: Include in project profit calculations
5. **Navigation**: Add income via "הוסף הכנסה" button in project details

#### Edit Feature Implementation (✅ COMPLETED)
1. **EditProjectScreen**: Full project editing with name, location, date, income type, and amount
2. **EditWorkerScreen**: Worker editing with name, phone number, and reference worker selection
3. **EditEventScreen**: Event editing with name, date, and time with date picker
4. **EditShiftScreen**: Comprehensive shift editing with time formatting and auto-calculation
5. **Navigation Support**: All edit screens integrated with proper navigation routes
6. **String Resources**: Added Hebrew strings for all edit operations ("ערוך פרויקט", "ערוך עובד", etc.)
7. **Validation**: Proper input validation and error handling in all edit screens
8. **Consistency**: Edit screens follow same patterns as Add screens for consistent UX

#### Dashboard Analytics Implementation (✅ COMPLETED)
1. **Comprehensive Analytics**: Complete dashboard rebuild with financial analytics and KPIs
2. **Financial Summary Card**: Revenue, expenses, and profit tracking with real-time calculations
3. **Money Owed Tracking**: Dedicated card for outstanding payments and debt management
4. **Active Projects Overview**: Horizontal scrollable list showing current active projects
5. **Upcoming Events**: Display of future events with income and date information
6. **Date Filtering**: DateFilterChip component for filtering analytics by date range
7. **DashboardViewModel**: Centralized state management with reactive data flows
8. **Navigation Integration**: Click-through navigation to project and event details
9. **Loading States**: Proper loading indicators and empty state handling
10. **Hebrew Localization**: All new strings added to both values and values-he resources

### Debt Tracking System (Version 15) (✅ COMPLETED)

#### Overview
Comprehensive debt tracking system that monitors outstanding payments to workers across shifts and events, providing clear visibility into payment obligations and enabling mark-as-paid functionality.

#### Database Schema Changes
- **Database Version**: Upgraded to 15
- **ShiftWorker Table**: Added `isPaid` BOOLEAN field (default: false)
- **EventWorker Table**: Added `isPaid` BOOLEAN field (default: false)
- **Migration 14→15**: Automatically adds isPaid columns to existing data with default false values

#### Core Entities
- **UnpaidShiftWorkerInfo**: Combines ShiftWorker with worker name, shift date, project name, and shift hours
- **UnpaidEventWorkerInfo**: Combines EventWorker with worker name, event date, and event name
- **WorkerWithDebt**: Aggregates worker info with total debt and unpaid item counts

#### Repository Enhancements
**WorkerRepository** debt-related methods:
- `getTotalPaymentsOwed()`: Calculates total unpaid amounts across all workers
- `getUnpaidShiftWorkers()`: Returns all unpaid shift assignments
- `getUnpaidEventWorkers()`: Returns all unpaid event assignments
- `getUnpaidShiftWorkersForWorker(workerId)`: Returns unpaid shifts for specific worker
- `getUnpaidEventWorkersForWorker(workerId)`: Returns unpaid events for specific worker
- `markShiftWorkerAsPaid(shiftWorkerId)`: Marks shift payment as completed
- `markEventWorkerAsPaid(eventWorkerId)`: Marks event payment as completed

#### UI Components

**MoneyOwedScreen**:
- Comprehensive debt overview with total amount
- Separate sections for unpaid shifts and events
- Individual payment cards with mark-as-paid functionality
- Navigation to worker details
- Empty state when no debts exist

**WorkerDetailScreen Enhancements**:
- Debt summary card showing total owed amount
- Unpaid shifts and events listed separately
- Mark-as-paid buttons for individual items
- Color-coded debt indicators (red for amounts owed)

**WorkersScreen Enhancements**:
- Debt indicators on worker cards
- Warning icons for workers with outstanding payments
- Unpaid item counts displayed
- Color-coded total owed amounts

**Dashboard Integration**:
- Clickable money owed card navigates to detailed debt view
- Real-time debt totals in financial summary

#### Payment Calculation Logic
**Shift Payments**:
- Hourly Rate: `payRate × shiftHours`
- Fixed Amount: `payRate` (regardless of hours)
- Reference Payments: `referencePayRate × shiftHours` (always hourly)

**Event Payments**:
- Always hourly calculation: `hours × payRate`
- Reference Payments: `referencePayRate × hours` (when worker has reference)

#### Navigation Routes
- `MoneyOwedScreen`: Accessible from dashboard debt card
- Worker detail navigation from debt screens
- Seamless integration with existing navigation structure

#### String Resources
Added comprehensive Hebrew strings for:
- Debt tracking interface elements
- Payment status indicators
- Mark-as-paid functionality
- Empty states and confirmations

#### Implementation Notes
- All debt calculations are real-time and reactive
- Payment status changes immediately refresh dependent screens
- Database migration handles existing data automatically
- Backwards compatible with previous app versions

### Event-Worker Relationship Enhancement (Version 17) (✅ COMPLETED)

#### Overview
Enhanced event worker assignment to match shift worker functionality, including reference worker support and unified UI experience.

#### Database Schema Changes
- **Database Version**: Upgraded to 17
- **EventWorker Table**: 
  - Added `referencePayRate` DOUBLE field for storing hourly reference worker payment rates (Version 16)
  - Added `isHourlyRate` BOOLEAN field for payment type selection (Version 17)
- **Migration 15→16**: Adds referencePayRate column to existing event_workers table
- **Migration 16→17**: Adds isHourlyRate column (defaults to true for backward compatibility)

#### Core Features
- **Unified Worker Assignment**: Events now use the same dialog interface as shifts for consistency
- **Payment Type Selection**: Events support both hourly rates and fixed amounts (just like shifts)
- **Reference Worker Support**: When adding workers with reference relationships, prompts for reference worker hourly rate
- **Automatic Hours Calculation**: Workers are assigned to events using the event's duration automatically (like shifts)
- **Event Hours Display**: Dialog shows the event's duration that will be used for calculation
- **Payment Calculation**: Total event cost includes both worker payments and reference payments with proper payment type handling
- **Real-time Payment Preview**: Dialog shows total payment calculation before confirmation (handles both hourly and fixed amounts)
- **Real-time Updates**: Financial calculations automatically refresh when workers are added/removed

#### UI Components

**Shared AddWorkerDialog Component**:
- Reusable dialog for both shift and event worker assignment
- Supports payment type selection (hourly/fixed amounts) for both shifts and events
- Built-in worker search functionality with real-time filtering
- Reference worker payment prompts when applicable
- Automatic hours calculation based on event/shift duration
- Real-time payment calculation preview including reference payments

**EventDetailScreen Enhancements**:
- Direct worker assignment through dialog (no separate screen navigation)
- Payment type display (hourly rate vs. fixed amount)
- Display of reference worker information and payments
- Total payment calculations including reference amounts with proper payment type handling
- Worker cards show payment type, worker payment, and reference payment details

#### Payment Calculation Updates
**EventWorkerDao**:
- Updated total cost query to handle payment types and reference payments:
  ```sql
  SELECT SUM(
      CASE 
          WHEN isHourlyRate = 1 THEN hours * payRate 
          ELSE payRate 
      END + 
      COALESCE(hours * referencePayRate, 0.0)
  ) FROM event_workers WHERE eventId = :eventId
  ```

**EventDetailViewModel**:
- Enhanced `addWorkerToEvent` method to support payment types and reference payment rates
- Added `allWorkers` state for worker reference lookups  
- Real-time cost calculations include reference payments with proper payment type handling

#### Navigation Simplification
- Removed separate `AddWorkerToEventScreen` navigation
- Integrated worker assignment directly into event detail screen via dialog
- Simplified navigation routes and removed unused AddWorkerToEvent screen

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
- never build the project. let me run and test
- Update claude.md after any relevent change
- make sure to add and edit the english and hebrew strings everytime there is a change that needs it
- when running the build use cd /c/Users/shlob/AndroidStudioProjects/workerTracking && ./gradlew.bat compileDebugKotlin. since the terminal is powershell

### Important String Resource Guidelines
- **Critical**: This app is designed to be fully in Hebrew. Both `values/strings.xml` (default) and `values-he/strings.xml` should contain Hebrew text
- **Common Mistake**: DO NOT put English text in `values/strings.xml` - it should contain Hebrew text just like `values-he/strings.xml`
- **Reason**: The app is Hebrew-first, so the default string resources should be in Hebrew to ensure proper display regardless of locale settings