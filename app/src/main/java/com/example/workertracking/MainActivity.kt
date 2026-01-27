package com.example.workertracking

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.workertracking.ui.navigation.Screen
import com.example.workertracking.ui.navigation.bottomNavItems
import com.example.workertracking.ui.screens.dashboard.DashboardScreen
import com.example.workertracking.ui.screens.projects.ProjectsScreen
import com.example.workertracking.ui.screens.projects.AddProjectScreen
import com.example.workertracking.ui.screens.projects.EditProjectScreen
import com.example.workertracking.ui.screens.projects.ProjectDetailScreen
import com.example.workertracking.ui.screens.projects.AddIncomeScreen
import com.example.workertracking.ui.screens.projects.ProjectIncomeListScreen
import com.example.workertracking.ui.screens.projects.EditIncomeScreen
import com.example.workertracking.ui.screens.workers.WorkersScreen
import com.example.workertracking.ui.screens.workers.AddWorkerScreen
import com.example.workertracking.ui.screens.workers.EditWorkerScreen
import com.example.workertracking.ui.screens.WorkerPhotoGalleryScreen
import com.example.workertracking.ui.screens.workers.WorkerDetailScreen
import com.example.workertracking.ui.screens.events.EventsScreen
import com.example.workertracking.ui.screens.events.AddEventScreen
import com.example.workertracking.ui.screens.events.EditEventScreen
import com.example.workertracking.ui.screens.events.EventDetailScreen
import com.example.workertracking.ui.screens.employers.EmployersScreen
import com.example.workertracking.ui.screens.employers.AddEmployerScreen
import com.example.workertracking.ui.screens.employers.EditEmployerScreen
import com.example.workertracking.ui.screens.employers.EmployerDetailScreen
import com.example.workertracking.ui.screens.shifts.AddShiftScreen
import com.example.workertracking.ui.screens.shifts.EditShiftScreen
import com.example.workertracking.ui.screens.shifts.ShiftDetailScreen
import com.example.workertracking.ui.screens.MoneyOwedScreen
import com.example.workertracking.ui.viewmodel.ProjectsViewModel
import com.example.workertracking.ui.viewmodel.AddProjectViewModel
import com.example.workertracking.ui.viewmodel.ProjectDetailViewModel
import com.example.workertracking.ui.viewmodel.EventsViewModel
import com.example.workertracking.ui.viewmodel.WorkersViewModel
import com.example.workertracking.ui.viewmodel.AddWorkerViewModel
import com.example.workertracking.ui.viewmodel.WorkerDetailViewModel
import com.example.workertracking.ui.viewmodel.AddEventViewModel
import com.example.workertracking.ui.viewmodel.EventDetailViewModel
import com.example.workertracking.ui.viewmodel.AddShiftViewModel
import com.example.workertracking.ui.viewmodel.ShiftDetailViewModel
import com.example.workertracking.ui.viewmodel.AddIncomeViewModel
import com.example.workertracking.ui.viewmodel.MoneyOwedViewModel
import com.example.workertracking.ui.viewmodel.DashboardViewModel
import com.example.workertracking.ui.viewmodel.EmployersViewModel
import com.example.workertracking.ui.viewmodel.AddEmployerViewModel
import com.example.workertracking.ui.viewmodel.EditEmployerViewModel
import com.example.workertracking.ui.viewmodel.EmployerDetailViewModel
import com.example.workertracking.ui.theme.WorkerTrackingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Debug logging
        Log.d("MainActivity", "Current locale: ${resources.configuration.locales[0]}")
        Log.d("MainActivity", "App name from resources: ${getString(R.string.app_name)}")
        Log.d("MainActivity", "Dashboard title: ${getString(R.string.nav_dashboard)}")
        
        enableEdgeToEdge()
        setContent {
            WorkerTrackingTheme {
                WorkerTrackingApp()
            }
        }
    }
    
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerTrackingApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as WorkerTrackingApplication
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute = currentDestination?.route
            
            // Only show bottom bar on main screens
            if (currentRoute in listOf(
                Screen.Dashboard.route,
                Screen.Projects.route,
                Screen.Workers.route,
                Screen.Events.route,
                Screen.Employers.route
            )) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination?.hierarchy?.any { it.route == item.screen.route } == true) {
                                        item.selectedIcon
                                    } else {
                                        item.icon
                                    },
                                    contentDescription = stringResource(item.screen.titleRes)
                                )
                            },
                            label = { Text(stringResource(item.screen.titleRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                // Handle navigation to dashboard differently
                                if (item.screen.route == Screen.Dashboard.route) {
                                    // Force navigation to dashboard by clearing everything and recreating
                                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                                    if (navController.currentDestination?.route != Screen.Dashboard.route) {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                } else {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel {
                    DashboardViewModel(
                        application.container.projectRepository,
                        application.container.shiftRepository,
                        application.container.eventRepository,
                        application.container.workerRepository,
                        application.container
                    )
                }
                
                DashboardScreen(
                    viewModel = viewModel,
                    onProjectClick = { projectId ->
                        navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                    },
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    },
                    onViewAllProjects = {
                        navController.navigate(Screen.Projects.route)
                    },
                    onViewAllEvents = {
                        navController.navigate(Screen.Events.route)
                    },
                    onMoneyOwedClick = {
                        navController.navigate(Screen.MoneyOwed.route)
                    }
                )
            }
            composable(Screen.Projects.route) {
                val viewModel: ProjectsViewModel = viewModel {
                    ProjectsViewModel(application.container.projectRepository)
                }
                val projects by viewModel.projects.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                
                ProjectsScreen(
                    projects = projects,
                    isLoading = isLoading,
                    onAddProject = {
                        navController.navigate(Screen.AddProject.route)
                    },
                    onProjectClick = { project ->
                        navController.navigate(Screen.ProjectDetail.createRoute(project.id))
                    },
                    onDeleteProject = { project ->
                        viewModel.deleteProject(project)
                    }
                )
            }
            composable(Screen.AddProject.route) {
                val viewModel: AddProjectViewModel = viewModel {
                    AddProjectViewModel(application.container.projectRepository)
                }
                val employersViewModel: EmployersViewModel = viewModel {
                    EmployersViewModel(application.container.employerRepository)
                }
                val saveSuccess by viewModel.saveSuccess.collectAsState()
                val availableEmployers by employersViewModel.employers.collectAsState()
                
                LaunchedEffect(saveSuccess) {
                    if (saveSuccess) {
                        viewModel.clearSaveSuccess()
                        navController.popBackStack()
                    }
                }
                
                AddProjectScreen(
                    availableEmployers = availableEmployers,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveProject = { name, location, startDate, employerId ->
                        viewModel.saveProject(name, location, startDate, employerId)
                    }
                )
            }
            composable(
                route = Screen.EditProject.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val viewModel: ProjectDetailViewModel = viewModel {
                    ProjectDetailViewModel(
                        application.container.projectRepository,
                        application.container.shiftRepository
                    )
                }
                val project by viewModel.project.collectAsState()
                val updateSuccess by viewModel.updateSuccess.collectAsState()
                
                LaunchedEffect(projectId) {
                    viewModel.loadProject(projectId)
                }
                
                LaunchedEffect(updateSuccess) {
                    if (updateSuccess) {
                        viewModel.clearUpdateSuccess()
                        navController.popBackStack()
                    }
                }
                
                EditProjectScreen(
                    project = project,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onUpdateProject = { name, location, startDate ->
                        viewModel.updateProject(name, location, startDate)
                    }
                )
            }
            composable(
                route = Screen.ProjectDetail.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val viewModel: ProjectDetailViewModel = viewModel {
                    ProjectDetailViewModel(
                        application.container.projectRepository,
                        application.container.shiftRepository
                    )
                }
                val project by viewModel.project.collectAsState()
                val shifts by viewModel.shifts.collectAsState()
                val totalIncome by viewModel.totalIncome.collectAsState()
                val totalPayments by viewModel.totalPayments.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                
                LaunchedEffect(projectId) {
                    viewModel.loadProject(projectId)
                }
                
                ProjectDetailScreen(
                    project = project,
                    shifts = shifts,
                    totalIncome = totalIncome,
                    totalPayments = totalPayments,
                    isLoading = isLoading,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditProject = {
                        navController.navigate(Screen.EditProject.createRoute(projectId))
                    },
                    onDeleteProject = {
                        viewModel.deleteProject()
                        navController.popBackStack()
                    },
                    onAddShift = {
                        navController.navigate(Screen.AddShift.createRoute(projectId))
                    },
                    onShiftClick = { shiftId ->
                        navController.navigate(Screen.ShiftDetail.createRoute(shiftId))
                    },
                    onDeleteShift = { shift ->
                        viewModel.deleteShift(shift)
                    },
                    onAddIncome = {
                        navController.navigate(Screen.AddIncome.createRoute(projectId))
                    },
                    onIncomeHistoryClick = {
                        navController.navigate(Screen.ProjectIncomeList.createRoute(projectId))
                    },
                    onCloseProject = {
                        viewModel.closeProject()
                    }
                )
            }
            composable(Screen.Workers.route) {
                val viewModel: WorkersViewModel = viewModel {
                    WorkersViewModel(application.container.workerRepository)
                }
                val workers by viewModel.workers.collectAsState()
                val workersWithDebt by viewModel.workersWithDebt.collectAsState()
                val workerEarnings by viewModel.workerEarnings.collectAsState()
                val referenceWorkerNames by viewModel.referenceWorkerNames.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                
                WorkersScreen(
                    workers = workers,
                    workersWithDebt = workersWithDebt,
                    workerEarnings = workerEarnings,
                    referenceWorkerNames = referenceWorkerNames,
                    isLoading = isLoading,
                    onAddWorker = {
                        navController.navigate(Screen.AddWorker.route)
                    },
                    onWorkerClick = { worker ->
                        navController.navigate(Screen.WorkerDetail.createRoute(worker.id))
                    },
                    onDeleteWorker = { worker ->
                        viewModel.deleteWorker(worker)
                    }
                )
            }
            composable(Screen.AddWorker.route) {
                val viewModel: AddWorkerViewModel = viewModel {
                    AddWorkerViewModel(application.container.workerRepository)
                }
                val saveSuccess by viewModel.saveSuccess.collectAsState()
                val availableWorkers by viewModel.availableWorkers.collectAsState()
                val error by viewModel.error.collectAsState()

                LaunchedEffect(saveSuccess) {
                    if (saveSuccess) {
                        viewModel.clearSaveSuccess()
                        navController.popBackStack()
                    }
                }

                AddWorkerScreen(
                    availableWorkers = availableWorkers,
                    error = error,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveWorker = { name, phoneNumber, referenceId ->
                        viewModel.saveWorker(name, phoneNumber, referenceId)
                    },
                    onClearError = {
                        viewModel.clearError()
                    }
                )
            }
            composable(
                route = Screen.WorkerDetail.route,
                arguments = listOf(navArgument("workerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val workerId = backStackEntry.arguments?.getLong("workerId") ?: 0L
                val viewModel: WorkerDetailViewModel = viewModel {
                    WorkerDetailViewModel(
                        application.container.workerRepository,
                        application.container.projectRepository,
                        application.container.eventRepository,
                        application.container.shiftRepository
                    )
                }
                val worker by viewModel.worker.collectAsState()
                val referenceWorker by viewModel.referenceWorker.collectAsState()
                val projects by viewModel.projects.collectAsState()
                val events by viewModel.events.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val unpaidShifts by viewModel.unpaidShifts.collectAsState()
                val unpaidEvents by viewModel.unpaidEvents.collectAsState()
                val allShifts by viewModel.allShifts.collectAsState()
                val allEvents by viewModel.allEvents.collectAsState()
                val totalOwed by viewModel.totalOwed.collectAsState()
                val unpaidReferenceShifts by viewModel.unpaidReferenceShifts.collectAsState()
                val unpaidReferenceEvents by viewModel.unpaidReferenceEvents.collectAsState()
                val totalReferenceOwed by viewModel.totalReferenceOwed.collectAsState()
                val paidShifts by viewModel.paidShifts.collectAsState()
                val paidEvents by viewModel.paidEvents.collectAsState()
                val showPaidItems by viewModel.showPaidItems.collectAsState()
                val dateFilter by viewModel.dateFilter.collectAsState()
                
                LaunchedEffect(workerId) {
                    viewModel.loadWorker(workerId)
                }
                
                WorkerDetailScreen(
                    worker = worker,
                    referenceWorker = referenceWorker,
                    projects = projects,
                    events = events,
                    isLoading = isLoading,
                    unpaidShifts = unpaidShifts,
                    unpaidEvents = unpaidEvents,
                    allShifts = allShifts,
                    allEvents = allEvents,
                    totalOwed = totalOwed,
                    unpaidReferenceShifts = unpaidReferenceShifts,
                    unpaidReferenceEvents = unpaidReferenceEvents,
                    totalReferenceOwed = totalReferenceOwed,
                    paidShifts = paidShifts,
                    paidEvents = paidEvents,
                    showPaidItems = showPaidItems,
                    dateFilter = dateFilter,
                    onNavigateBack = {
                        application.container.triggerDashboardRefresh()
                        navController.popBackStack()
                    },
                    onEditWorker = {
                        navController.navigate(Screen.EditWorker.createRoute(workerId))
                    },
                    onDeleteWorker = {
                        viewModel.deleteWorker()
                        application.container.triggerDashboardRefresh()
                        navController.popBackStack()
                    },
                    onViewPhotos = {
                        worker?.let { w ->
                            navController.navigate(Screen.WorkerPhotoGallery.createRoute(w.id, w.name))
                        }
                    },
                    onMarkShiftAsPaid = { shiftWorkerId ->
                        viewModel.markShiftAsPaid(shiftWorkerId)
                        application.container.triggerDashboardRefresh()
                    },
                    onMarkEventAsPaid = { eventWorkerId ->
                        viewModel.markEventAsPaid(eventWorkerId)
                    },
                    onUpdateEventPayment = { eventWorkerId, isPaid, amountPaid, tipAmount ->
                        viewModel.updateEventWorkerPayment(eventWorkerId, isPaid, amountPaid, tipAmount)
                    },
                    onRevokeShiftPayment = { shiftWorkerId ->
                        viewModel.revokeShiftPayment(shiftWorkerId)
                        application.container.triggerDashboardRefresh()
                    },
                    onRevokeEventPayment = { eventWorkerId ->
                        viewModel.revokeEventPayment(eventWorkerId)
                        application.container.triggerDashboardRefresh()
                    },
                    onMarkAllAsPaid = {
                        viewModel.markAllAsPaid()
                        application.container.triggerDashboardRefresh()
                    },
                    onToggleShowPaidItems = {
                        viewModel.toggleShowPaidItems()
                    },
                    onDateRangeSelected = { startDate, endDate ->
                        viewModel.setDateFilter(startDate, endDate)
                    },
                    onClearDateFilter = {
                        viewModel.clearDateFilter()
                    }
                )
            }
            composable(
                route = Screen.EditWorker.route,
                arguments = listOf(navArgument("workerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val workerId = backStackEntry.arguments?.getLong("workerId") ?: 0L
                val viewModel: WorkerDetailViewModel = viewModel {
                    WorkerDetailViewModel(
                        application.container.workerRepository,
                        application.container.projectRepository,
                        application.container.eventRepository,
                        application.container.shiftRepository
                    )
                }
                val worker by viewModel.worker.collectAsState()
                val allWorkers by viewModel.allWorkers.collectAsState()
                val updateSuccess by viewModel.updateSuccess.collectAsState()
                val error by viewModel.error.collectAsState()

                LaunchedEffect(workerId) {
                    viewModel.loadWorker(workerId)
                }

                LaunchedEffect(updateSuccess) {
                    if (updateSuccess) {
                        viewModel.clearUpdateSuccess()
                        navController.popBackStack()
                    }
                }

                EditWorkerScreen(
                    worker = worker,
                    availableWorkers = allWorkers,
                    error = error,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onUpdateWorker = { name, phoneNumber, referenceId ->
                        viewModel.updateWorker(name, phoneNumber, referenceId)
                    },
                    onClearError = {
                        viewModel.clearError()
                    }
                )
            }
            composable(
                route = Screen.WorkerPhotoGallery.route,
                arguments = listOf(
                    navArgument("workerId") { type = NavType.LongType },
                    navArgument("workerName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val workerId = backStackEntry.arguments?.getLong("workerId") ?: 0L
                val workerName = backStackEntry.arguments?.getString("workerName") ?: ""
                
                WorkerPhotoGalleryScreen(
                    workerId = workerId,
                    workerName = workerName,
                    onNavigateUp = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Events.route) {
                val viewModel: EventsViewModel = viewModel {
                    EventsViewModel(application.container.eventRepository)
                }
                val events by viewModel.events.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                
                EventsScreen(
                    events = events,
                    isLoading = isLoading,
                    onAddEvent = {
                        navController.navigate(Screen.AddEvent.route)
                    },
                    onEventClick = { event ->
                        navController.navigate(Screen.EventDetail.createRoute(event.id))
                    },
                    onDeleteEvent = { event ->
                        viewModel.deleteEvent(event)
                        application.container.triggerDashboardRefresh()
                    }
                )
            }
            composable(Screen.AddEvent.route) {
                val viewModel: AddEventViewModel = viewModel {
                    AddEventViewModel(application.container.eventRepository)
                }
                val employersViewModel: EmployersViewModel = viewModel {
                    EmployersViewModel(application.container.employerRepository)
                }
                val saveSuccess by viewModel.saveSuccess.collectAsState()
                val availableEmployers by employersViewModel.employers.collectAsState()
                
                LaunchedEffect(saveSuccess) {
                    if (saveSuccess) {
                        viewModel.clearSaveSuccess()
                        navController.popBackStack()
                    }
                }
                
                AddEventScreen(
                    availableEmployers = availableEmployers,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveEvent = { name, date, startTime, endTime, hours, income, employerId ->
                        viewModel.saveEvent(name, date, startTime, endTime, hours, income, employerId)
                    }
                )
            }
            composable(
                route = Screen.EventDetail.route,
                arguments = listOf(navArgument("eventId") { type = NavType.LongType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
                val viewModel: EventDetailViewModel = viewModel {
                    EventDetailViewModel(
                        application.container.eventRepository,
                        application.container.workerRepository
                    )
                }
                val event by viewModel.event.collectAsState()
                val eventWorkers by viewModel.eventWorkers.collectAsState()
                val allWorkers by viewModel.allWorkers.collectAsState()
                val totalCost by viewModel.totalCost.collectAsState()
                val deleteSuccess by viewModel.deleteSuccess.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                
                LaunchedEffect(eventId) {
                    viewModel.loadEvent(eventId)
                }
                
                LaunchedEffect(deleteSuccess) {
                    if (deleteSuccess) {
                        viewModel.clearDeleteSuccess()
                        navController.popBackStack()
                    }
                }
                
                EventDetailScreen(
                    event = event,
                    eventWorkers = eventWorkers,
                    allWorkers = allWorkers,
                    totalCost = totalCost,
                    isLoading = isLoading,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditEvent = {
                        navController.navigate(Screen.EditEvent.createRoute(eventId))
                    },
                    onDeleteEvent = {
                        viewModel.deleteEvent()
                    },
                    onAddWorkerToEvent = { eId, workerId, hours, isHourlyRate, payRate, referencePayRate ->
                        viewModel.addWorkerToEvent(eId, workerId, hours, isHourlyRate, payRate, referencePayRate)
                    },
                    onRemoveWorker = { eventWorker ->
                        viewModel.removeWorkerFromEvent(eventWorker)
                    },
                    onUpdatePayment = { eventWorkerId, isPaid, amountPaid, tipAmount ->
                        viewModel.updateEventWorkerPayment(eventWorkerId, isPaid, amountPaid, tipAmount)
                    },
                    onUpdateReferencePayment = { eventWorkerId, isPaid, amountPaid, tipAmount ->
                        viewModel.updateEventWorkerReferencePayment(eventWorkerId, isPaid, amountPaid, tipAmount)
                    },
                    onUpdateWorker = { eventWorker ->
                        viewModel.updateWorkerInEvent(eventWorker)
                    }
                )
            }
            composable(
                route = Screen.EditEvent.route,
                arguments = listOf(navArgument("eventId") { type = NavType.LongType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
                val viewModel: EventDetailViewModel = viewModel {
                    EventDetailViewModel(
                        application.container.eventRepository,
                        application.container.workerRepository
                    )
                }
                val event by viewModel.event.collectAsState()
                val updateSuccess by viewModel.updateSuccess.collectAsState()
                
                LaunchedEffect(eventId) {
                    viewModel.loadEvent(eventId)
                }
                
                LaunchedEffect(updateSuccess) {
                    if (updateSuccess) {
                        viewModel.clearUpdateSuccess()
                        navController.popBackStack()
                    }
                }
                
                EditEventScreen(
                    event = event,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onUpdateEvent = { name, date, startTime, endTime, hours, income ->
                        viewModel.updateEvent(name, date, startTime, endTime, hours, income)
                    }
                )
            }
            composable(
                route = Screen.AddShift.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val viewModel: AddShiftViewModel = viewModel {
                    AddShiftViewModel(
                        application.container.shiftRepository
                    )
                }
                val saveSuccess by viewModel.saveSuccess.collectAsState()
                
                LaunchedEffect(saveSuccess) {
                    if (saveSuccess) {
                        viewModel.clearSaveSuccess()
                        navController.popBackStack()
                    }
                }
                
                AddShiftScreen(
                    projectId = projectId,
                    projectName = "פרויקט", // TODO: Get actual project name
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveShift = { pId, name, date, startTime, endTime, hours ->
                        viewModel.saveShift(pId, name, date, startTime, endTime, hours)
                    }
                )
            }
            composable(
                route = Screen.EditShift.route,
                arguments = listOf(navArgument("shiftId") { type = NavType.LongType })
            ) { backStackEntry ->
                val shiftId = backStackEntry.arguments?.getLong("shiftId") ?: 0L
                val viewModel: ShiftDetailViewModel = viewModel {
                    ShiftDetailViewModel(
                        application.container.shiftRepository,
                        application.container.workerRepository
                    )
                }
                val shift by viewModel.shift.collectAsState()
                val updateSuccess by viewModel.updateSuccess.collectAsState()
                
                LaunchedEffect(shiftId) {
                    viewModel.loadShiftDetails(shiftId)
                }
                
                LaunchedEffect(updateSuccess) {
                    if (updateSuccess) {
                        viewModel.clearUpdateSuccess()
                        navController.popBackStack()
                    }
                }
                
                shift?.let { currentShift ->
                    EditShiftScreen(
                        shift = currentShift,
                        projectName = "פרויקט", // TODO: Get actual project name
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onUpdateShift = { name, date, startTime, endTime, hours ->
                            viewModel.updateShift(name, date, startTime, endTime, hours)
                        }
                    )
                }
            }
            composable(
                route = Screen.ShiftDetail.route,
                arguments = listOf(navArgument("shiftId") { type = NavType.LongType })
            ) { backStackEntry ->
                val shiftId = backStackEntry.arguments?.getLong("shiftId") ?: 0L
                val viewModel: ShiftDetailViewModel = viewModel {
                    ShiftDetailViewModel(
                        application.container.shiftRepository,
                        application.container.workerRepository
                    )
                }
                val shift by viewModel.shift.collectAsState()
                val shiftWorkers by viewModel.shiftWorkers.collectAsState()
                val allWorkers by viewModel.allWorkers.collectAsState()

                LaunchedEffect(shiftId) {
                    viewModel.loadShiftDetails(shiftId)
                }
                
                shift?.let { 
                    ShiftDetailScreen(
                        shift = it,
                        shiftWorkers = shiftWorkers,
                        allWorkers = allWorkers,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onEditShift = {
                            navController.navigate(Screen.EditShift.createRoute(shiftId))
                        },
                        onDeleteShift = {
                            viewModel.deleteShift(shift!!)
                            navController.popBackStack()
                        },
                        onAddWorkerToShift = { sId, wId, isHourly, payRate, referencePayRate ->
                            viewModel.addWorkerToShift(sId, wId, isHourly, payRate, referencePayRate)
                        },
                        onRemoveWorkerFromShift = { sId, wId ->
                            viewModel.removeWorkerFromShift(sId, wId)
                        },
                        onUpdateWorkerPayment = { shiftWorker ->
                            viewModel.updateWorkerPayment(shiftWorker)
                        },
                        onMarkAsPaid = { shiftWorkerId ->
                            viewModel.markShiftWorkerAsPaid(shiftWorkerId)
                        }
                    )
                }
            }
            composable(
                route = Screen.AddIncome.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val viewModel: AddIncomeViewModel = viewModel {
                    AddIncomeViewModel(application.container.projectRepository)
                }
                val saveSuccess by viewModel.saveSuccess.collectAsState()
                
                LaunchedEffect(saveSuccess) {
                    if (saveSuccess) {
                        viewModel.clearSaveSuccess()
                        navController.popBackStack()
                    }
                }
                
                AddIncomeScreen(
                    projectId = projectId,
                    projectName = "פרויקט", // TODO: Get actual project name
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveIncome = { pId, date, description, amount, units ->
                        viewModel.saveIncome(pId, date, description, amount, units)
                    }
                )
            }
            composable(
                route = Screen.ProjectIncomeList.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val viewModel: ProjectDetailViewModel = viewModel {
                    ProjectDetailViewModel(
                        application.container.projectRepository,
                        application.container.shiftRepository
                    )
                }
                
                LaunchedEffect(projectId) {
                    viewModel.loadProject(projectId)
                }
                
                val project by viewModel.project.collectAsState()
                val incomeEntries by viewModel.incomeEntries.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                
                ProjectIncomeListScreen(
                    projectName = project?.name ?: "",
                    incomeEntries = incomeEntries,
                    isLoading = isLoading,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onAddIncome = {
                        navController.navigate(Screen.AddIncome.createRoute(projectId))
                    },
                    onEditIncome = { income ->
                        navController.navigate(Screen.EditIncome.createRoute(income.id))
                    },
                    onDeleteIncome = { income ->
                        viewModel.deleteIncome(income)
                    }
                )
            }
            composable(
                route = Screen.EditIncome.route,
                arguments = listOf(navArgument("incomeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val incomeId = backStackEntry.arguments?.getLong("incomeId") ?: 0L
                val viewModel: ProjectDetailViewModel = viewModel {
                    ProjectDetailViewModel(
                        application.container.projectRepository,
                        application.container.shiftRepository
                    )
                }
                
                LaunchedEffect(incomeId) {
                    viewModel.loadIncomeById(incomeId)
                }
                
                val income by viewModel.currentIncome.collectAsState()
                val project by viewModel.project.collectAsState()
                
                income?.let { incomeEntry ->
                    EditIncomeScreen(
                        income = incomeEntry,
                        projectName = project?.name ?: "",
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onUpdateIncome = { updatedIncome ->
                            viewModel.updateIncome(updatedIncome)
                            navController.popBackStack()
                        }
                    )
                }
            }
            composable(Screen.MoneyOwed.route) {
                val viewModel: MoneyOwedViewModel = viewModel {
                    MoneyOwedViewModel.Factory(application.container.workerRepository, application.container).create(MoneyOwedViewModel::class.java)
                }
                
                MoneyOwedScreen(
                    onNavigateBack = {
                        application.container.triggerDashboardRefresh()
                        navController.popBackStack()
                    },
                    onWorkerClick = { workerId ->
                        navController.navigate(Screen.WorkerDetail.createRoute(workerId))
                    },
                    viewModel = viewModel
                )
            }
            composable(Screen.Employers.route) {
                val viewModel: EmployersViewModel = viewModel {
                    EmployersViewModel(application.container.employerRepository)
                }
                val employers by viewModel.employers.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                
                EmployersScreen(
                    employers = employers,
                    isLoading = isLoading,
                    onAddEmployer = {
                        navController.navigate(Screen.AddEmployer.route)
                    },
                    onEmployerClick = { employer ->
                        navController.navigate(Screen.EmployerDetail.createRoute(employer.id))
                    },
                    onDeleteEmployer = { employer ->
                        viewModel.deleteEmployer(employer)
                    }
                )
            }
            composable(Screen.AddEmployer.route) {
                val viewModel: AddEmployerViewModel = viewModel {
                    AddEmployerViewModel(application.container.employerRepository)
                }
                val saveSuccess by viewModel.saveSuccess.collectAsState()
                
                LaunchedEffect(saveSuccess) {
                    if (saveSuccess) {
                        viewModel.clearSaveSuccess()
                        navController.popBackStack()
                    }
                }
                
                AddEmployerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveEmployer = { name, phoneNumber ->
                        viewModel.saveEmployer(name, phoneNumber)
                    }
                )
            }
            composable(
                route = Screen.EditEmployer.route,
                arguments = listOf(navArgument("employerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val employerId = backStackEntry.arguments?.getLong("employerId") ?: 0L
                val viewModel: EditEmployerViewModel = viewModel {
                    EditEmployerViewModel(application.container.employerRepository)
                }
                val employer by viewModel.employer.collectAsState()
                val updateSuccess by viewModel.updateSuccess.collectAsState()
                
                LaunchedEffect(employerId) {
                    viewModel.loadEmployer(employerId)
                }
                
                LaunchedEffect(updateSuccess) {
                    if (updateSuccess) {
                        viewModel.clearUpdateSuccess()
                        navController.popBackStack()
                    }
                }
                
                EditEmployerScreen(
                    employer = employer,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onUpdateEmployer = { name, phoneNumber ->
                        viewModel.updateEmployer(name, phoneNumber)
                    }
                )
            }
            composable(
                route = Screen.EmployerDetail.route,
                arguments = listOf(navArgument("employerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val employerId = backStackEntry.arguments?.getLong("employerId") ?: 0L
                val viewModel: EmployerDetailViewModel = viewModel {
                    EmployerDetailViewModel(application.container.employerRepository)
                }
                val employer by viewModel.employer.collectAsState()
                val projects by viewModel.projects.collectAsState()
                val events by viewModel.events.collectAsState()
                val totalProfit by viewModel.totalProfit.collectAsState()
                val totalIncome by viewModel.totalIncome.collectAsState()
                val totalExpenses by viewModel.totalExpenses.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val deleteSuccess by viewModel.deleteSuccess.collectAsState()
                
                LaunchedEffect(employerId) {
                    viewModel.loadEmployer(employerId)
                }
                
                LaunchedEffect(deleteSuccess) {
                    if (deleteSuccess) {
                        viewModel.clearDeleteSuccess()
                        navController.popBackStack()
                    }
                }
                
                EmployerDetailScreen(
                    employer = employer,
                    projects = projects,
                    events = events,
                    isLoading = isLoading,
                    totalProfit = totalProfit,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditEmployer = {
                        navController.navigate(Screen.EditEmployer.createRoute(employerId))
                    },
                    onDeleteEmployer = {
                        viewModel.deleteEmployer()
                    },
                    onProjectClick = { project ->
                        navController.navigate(Screen.ProjectDetail.createRoute(project.id))
                    },
                    onEventClick = { event ->
                        navController.navigate(Screen.EventDetail.createRoute(event.id))
                    }
                )
            }
        }
    }
}