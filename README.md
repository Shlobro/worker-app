# Worker Tracking

A comprehensive Android application for managing construction projects, workers, and work shifts with full financial tracking capabilities.

## Features

### 🏗️ Project Management
- Create and manage construction projects with locations and dates
- Track project status (active/closed)
- Add multiple income entries per project with flexible units
- Real-time profit/loss calculations
- Edit project details including name, location, and financial information

### 👷 Worker Management
- Manage worker profiles with contact information and photo albums
- Reference worker system with hourly rate prompts during shift assignment
- Direct phone dialing integration
- Search workers by name
- Edit worker information and reference relationships
- Photo gallery with sharing capabilities

### ⏰ Shift Management
- Create individual work shifts linked to projects
- Multi-worker shift support with individual payment configuration
- Two payment types per worker:
  - **שכר שעתי** (Hourly Rate): rate × hours
  - **סכום גלובלי** (Global Amount): fixed amount
- Smart time input with auto-formatting (0800 → 08:00)
- Automatic hours calculation with manual override option
- Cross-midnight shift support

### 🎉 Event Management
- Create standalone events with start/end times and payment amounts
- Direct worker-to-event assignment with hours and rates
- Event cost calculation and profit analysis (income vs worker payments)
- Full event details view with worker management
- Edit event details including name, date, time, and payment
- Delete events functionality
- Search events by name

### 💰 Financial Tracking
- **Income Management**: Multiple income entries per project with detailed income history view
- **Payment Calculations**: Automatic worker payment totaling including reference worker fees
- **Profit Analysis**: Real-time net profit calculation (Income - Payments)
- **Visual Indicators**: Color-coded profit/loss display
- **Dashboard Analytics**: Revenue, expenses, and profit with date filtering
- **Active Projects Overview**: Current project status and upcoming events
- **Money Owed Tracking**: Outstanding payments and debtors

### 📱 User Interface
- Modern Material 3 design
- Fully localized in Hebrew
- Bottom navigation with hierarchical screens
- Search functionality across workers, projects, and events
- Date/time pickers with Hebrew localization
- Photo gallery and sharing capabilities

## Technical Architecture

### 🏗️ Architecture Pattern
- **MVVM (Model-View-ViewModel)**: Clean separation of concerns
- **Repository Pattern**: Data layer abstraction
- **Manual Dependency Injection**: Simple AppContainer-based DI

### 🗄️ Database
- **Room Database**: Local SQLite persistence (Version 7)
- **Entities**: Project, ProjectIncome, Worker, Shift, ShiftWorker, Event, EventWorker, Payment
- **Relationships**: Many-to-many relationships between shifts/events and workers

### 🎨 UI Framework
- **Jetpack Compose**: Modern declarative UI
- **Material 3**: Latest Material Design components
- **Navigation Compose**: Type-safe navigation

### 📱 Target Platforms
- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 35 (Android 15)
- **compileSdk**: 35

## Project Structure

```
app/src/main/java/com/example/workertracking/
├── data/
│   ├── database/          # Room database and converters
│   ├── dao/              # Database access objects
│   └── entity/           # Data entities
├── repository/           # Business logic layer
├── ui/
│   ├── screens/          # Compose screens by feature
│   ├── viewmodel/        # UI state management
│   ├── theme/            # Material 3 theming
│   └── navigation/       # Navigation components
└── di/                   # Dependency injection
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 11 or higher
- Android SDK 35

### Building the Application

#### Debug Build
```bash
# Linux/Mac
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

#### Release Build
```bash
# Linux/Mac
./gradlew assembleRelease

# Windows
gradlew.bat assembleRelease
```

#### Install on Device
```bash
# Linux/Mac
./gradlew installDebug

# Windows
gradlew.bat installDebug
```

### Testing

#### Unit Tests
```bash
# Linux/Mac
./gradlew test

# Windows
gradlew.bat test
```

#### Instrumented Tests
```bash
# Linux/Mac
./gradlew connectedAndroidTest

# Windows
gradlew.bat connectedAndroidTest
```

#### Single Test Class
```bash
./gradlew test --tests "com.example.workertracking.ExampleUnitTest"
```

### Code Quality

#### Lint Check
```bash
# Linux/Mac
./gradlew lint

# Windows
gradlew.bat lint
```

#### Clean Build
```bash
# Linux/Mac
./gradlew clean

# Windows
gradlew.bat clean
```

## Key Features in Detail

### Worker-Project Relationships
- Workers are linked to projects **only through shifts**
- No direct worker-project assignments
- Flexible shift-based work tracking

### Payment System
- **Dual Payment Types**: Hourly rates and fixed amounts
- **Per-Shift Configuration**: Payment rates set individually per shift
- **Reference Worker System**: Use reference workers for consistent rate calculations
- **Automatic Calculations**: Real-time payment and profit calculations

### Financial Management
- **Project Income**: Multiple income entries with flexible units (days, hours, fixed amounts)
- **Worker Payments**: Calculated from shift assignments
- **Profit Tracking**: Net profit = Total Income - Total Worker Payments
- **Real-time Updates**: Financial data updates automatically

### Time Management
- **Smart Input**: Auto-formats time input (0800 → 08:00)
- **Auto-calculation**: Automatic hours calculation from start/end times
- **Manual Override**: Edit hours manually when needed
- **Cross-midnight**: Support for shifts spanning midnight

## Development Guidelines

### Adding New Features
1. Create entity in `data/entity` for new data models
2. Add DAO methods in `data/dao` for database operations
3. Update repository in `repository` package for business logic
4. Create ViewModel in `ui/viewmodel` for UI state management
5. Build Compose screens in `ui/screens` following existing patterns
6. Update navigation if new routes are needed

### Database Changes
- Update database version in `WorkerTrackingDatabase`
- Add migration strategy for schema changes
- Update relevant DAOs and entities
- Test migrations thoroughly

### UI Development
- Follow Material 3 design principles
- Use existing theme components in `ui/theme`
- Maintain Hebrew localization
- Follow existing screen structure and naming conventions

## Dependencies

### Core Dependencies
- **Jetpack Compose**: Modern UI framework
- **Room**: Database ORM with KSP annotation processing
- **Navigation Compose**: In-app navigation
- **Lifecycle ViewModel**: State management
- **Kotlin Coroutines**: Asynchronous programming
- **Material 3**: Latest Material Design components

## License

This project is proprietary software. All rights reserved.

## Support

For issues or questions about the application, please contact the development team.

---

**Built with ❤️ using Android and Jetpack Compose**