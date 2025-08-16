package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Event
import com.example.workertracking.data.entity.Project
import com.example.workertracking.repository.EventRepository
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.ShiftRepository
import com.example.workertracking.repository.WorkerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

data class DashboardUiState(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val activeProjects: List<Project> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val totalOwed: Double = 0.0,
    val filteredStartDate: Date? = null,
    val filteredEndDate: Date? = null,
    val isLoading: Boolean = true
)

data class ProjectSummary(
    val project: Project,
    val totalIncome: Double,
    val totalCosts: Double,
    val profit: Double
)

data class EventSummary(
    val event: Event,
    val totalCosts: Double,
    val profit: Double
)

class DashboardViewModel(
    private val projectRepository: ProjectRepository,
    private val shiftRepository: ShiftRepository,
    private val eventRepository: EventRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _dateFilter = MutableStateFlow<Pair<Date?, Date?>>(null to null)
    val dateFilter: StateFlow<Pair<Date?, Date?>> = _dateFilter.asStateFlow()

    init {
        loadDashboardData()
    }

    fun setDateFilter(startDate: Date?, endDate: Date?) {
        _dateFilter.value = startDate to endDate
        _uiState.value = _uiState.value.copy(
            filteredStartDate = startDate,
            filteredEndDate = endDate
        )
        loadDashboardData()
    }

    fun clearDateFilter() {
        setDateFilter(null, null)
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                combine(
                    calculateTotalIncome(),
                    calculateTotalExpenses(),
                    getActiveProjects(),
                    getUpcomingEvents(),
                    calculateTotalOwed()
                ) { income, expenses, projects, events, owed ->
                    DashboardUiState(
                        totalIncome = income,
                        totalExpenses = expenses,
                        netProfit = income - expenses,
                        activeProjects = projects,
                        upcomingEvents = events,
                        totalOwed = owed,
                        filteredStartDate = _dateFilter.value.first,
                        filteredEndDate = _dateFilter.value.second,
                        isLoading = false
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun calculateTotalIncome(): Flow<Double> = flow {
        val (startDate, endDate) = _dateFilter.value
        
        // Get project income
        val projectIncome = projectRepository.getTotalProjectIncome()
        
        // Get event income
        val events = eventRepository.getAllEvents().first()
        val eventIncome = events
            .filter { event ->
                isWithinDateRange(event.date, startDate, endDate)
            }
            .sumOf { it.income }
        
        emit(projectIncome + eventIncome)
    }

    private fun calculateTotalExpenses(): Flow<Double> = flow {
        val (startDate, endDate) = _dateFilter.value
        
        // Get shift costs
        val shifts = shiftRepository.getAllShifts().first()
        val shiftCosts = shifts
            .filter { shift ->
                isWithinDateRange(shift.date, startDate, endDate)
            }
            .sumOf { shift ->
                shiftRepository.getTotalCostForShift(shift.id) ?: 0.0
            }
        
        // Get event costs
        val events = eventRepository.getAllEvents().first()
        val eventCosts = events
            .filter { event ->
                isWithinDateRange(event.date, startDate, endDate)
            }
            .sumOf { event ->
                eventRepository.getTotalEventCost(event.id) ?: 0.0
            }
        
        emit(shiftCosts + eventCosts)
    }

    private fun getActiveProjects(): Flow<List<Project>> = 
        projectRepository.getAllProjects().map { projects ->
            val (startDate, endDate) = _dateFilter.value
            projects.filter { project ->
                project.endDate == null && // Active projects only
                isWithinDateRange(project.startDate, startDate, endDate)
            }.take(5) // Limit to 5 most recent
        }

    private fun getUpcomingEvents(): Flow<List<Event>> =
        eventRepository.getAllEvents().map { events ->
            val now = Date()
            val (startDate, endDate) = _dateFilter.value
            events.filter { event ->
                event.date.after(now) && // Future events only
                isWithinDateRange(event.date, startDate, endDate)
            }.sortedBy { it.date }.take(5) // Next 5 events
        }

    private fun calculateTotalOwed(): Flow<Double> = flow {
        // This would calculate total money owed to workers
        // For now, we'll use shift costs as unpaid amounts
        val totalShiftCosts = shiftRepository.getAllShifts().first()
            .sumOf { shift ->
                shiftRepository.getTotalCostForShift(shift.id) ?: 0.0
            }
        
        val totalEventCosts = eventRepository.getAllEvents().first()
            .sumOf { event ->
                eventRepository.getTotalEventCost(event.id) ?: 0.0
            }
        
        emit(totalShiftCosts + totalEventCosts)
    }

    suspend fun getProjectSummaries(): List<ProjectSummary> {
        val projects = projectRepository.getAllProjects().first()
        return projects.map { project ->
            val income = projectRepository.getTotalIncomeForProject(project.id)
            val costs = shiftRepository.getTotalCostForProject(project.id) ?: 0.0
            ProjectSummary(
                project = project,
                totalIncome = income,
                totalCosts = costs,
                profit = income - costs
            )
        }
    }

    suspend fun getEventSummaries(): List<EventSummary> {
        val events = eventRepository.getAllEvents().first()
        return events.map { event ->
            val costs = eventRepository.getTotalEventCost(event.id) ?: 0.0
            EventSummary(
                event = event,
                totalCosts = costs,
                profit = event.income - costs
            )
        }
    }

    private fun isWithinDateRange(date: Date, startDate: Date?, endDate: Date?): Boolean {
        if (startDate == null && endDate == null) return true
        if (startDate != null && date.before(startDate)) return false
        if (endDate != null && date.after(endDate)) return false
        return true
    }
}