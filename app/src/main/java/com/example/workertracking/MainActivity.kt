package com.example.workertracking

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import java.util.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
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
import com.example.workertracking.ui.screens.workers.WorkersScreen
import com.example.workertracking.ui.screens.workers.AddWorkerScreen
import com.example.workertracking.ui.screens.workers.EditWorkerScreen
import com.example.workertracking.ui.screens.workers.WorkerDetailScreen
import com.example.workertracking.ui.screens.events.EventsScreen
import com.example.workertracking.ui.screens.events.AddEventScreen
import com.example.workertracking.ui.screens.events.EditEventScreen
import com.example.workertracking.ui.screens.shifts.AddShiftScreen
import com.example.workertracking.ui.screens.shifts.EditShiftScreen
import com.example.workertracking.ui.screens.shifts.ShiftDetailScreen
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
                Screen.Events.route
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
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
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
                DashboardScreen()
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
                val saveSuccess by viewModel.saveSuccess.collectAsState()
                
                LaunchedEffect(saveSuccess) {
                    if (saveSuccess) {
                        viewModel.clearSaveSuccess()
                        navController.popBackStack()
                    }
                }
                
                AddProjectScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveProject = { name, location, startDate ->
                        viewModel.saveProject(name, location, startDate)
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
                val isLoading by viewModel.isLoading.collectAsState()
                
                WorkersScreen(
                    workers = workers,
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
                
                LaunchedEffect(saveSuccess) {
                    if (saveSuccess) {
                        viewModel.clearSaveSuccess()
                        navController.popBackStack()
                    }
                }
                
                AddWorkerScreen(
                    availableWorkers = availableWorkers,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveWorker = { name, phoneNumber, referenceId ->
                        viewModel.saveWorker(name, phoneNumber, referenceId)
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
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onUpdateWorker = { name, phoneNumber, referenceId ->
                        viewModel.updateWorker(name, phoneNumber, referenceId)
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
                val projects by viewModel.projects.collectAsState()
                val events by viewModel.events.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                
                LaunchedEffect(workerId) {
                    viewModel.loadWorker(workerId)
                }
                
                WorkerDetailScreen(
                    worker = worker,
                    projects = projects,
                    events = events,
                    isLoading = isLoading,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditWorker = {
                        navController.navigate(Screen.EditWorker.createRoute(workerId))
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
                        // TODO: Navigate to event detail screen when implemented
                    }
                )
            }
            composable(Screen.AddEvent.route) {
                val viewModel: AddEventViewModel = viewModel {
                    AddEventViewModel(application.container.eventRepository)
                }
                val saveSuccess by viewModel.saveSuccess.collectAsState()
                
                LaunchedEffect(saveSuccess) {
                    if (saveSuccess) {
                        viewModel.clearSaveSuccess()
                        navController.popBackStack()
                    }
                }
                
                AddEventScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveEvent = { name, date, time ->
                        viewModel.saveEvent(name, date, time)
                    }
                )
            }
            composable(
                route = Screen.EditEvent.route,
                arguments = listOf(navArgument("eventId") { type = NavType.LongType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
                val viewModel: EventDetailViewModel = viewModel {
                    EventDetailViewModel(application.container.eventRepository)
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
                    onUpdateEvent = { name, date, time ->
                        viewModel.updateEvent(name, date, time)
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
                        application.container.workerRepository,
                        application.container.projectRepository
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
                        application.container.workerRepository,
                        application.container.projectRepository
                    )
                }
                val shift by viewModel.shift.collectAsState()
                val shiftWorkers by viewModel.shiftWorkers.collectAsState()
                val allWorkers by viewModel.allWorkers.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                
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
                        onAddWorkerToShift = { sId, wId, isHourly, payRate ->
                            viewModel.addWorkerToShift(sId, wId, isHourly, payRate)
                        },
                        onRemoveWorkerFromShift = { sId, wId ->
                            viewModel.removeWorkerFromShift(sId, wId)
                        },
                        onUpdateWorkerPayment = { shiftWorker ->
                            viewModel.updateWorkerPayment(shiftWorker)
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
        }
    }
}