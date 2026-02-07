package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Event
import com.example.workertracking.data.entity.Project
import com.example.workertracking.repository.EventRepository
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.ShiftRepository
import com.example.workertracking.repository.WorkerRepository
import com.example.workertracking.di.AppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import java.util.*
import java.util.Calendar

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

class DashboardViewModel(
    private val projectRepository: ProjectRepository,
    private val shiftRepository: ShiftRepository,
    private val eventRepository: EventRepository,
    private val workerRepository: WorkerRepository,
    private val appContainer: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadDashboardData()
        startPeriodicRefresh()
        listenToRefreshTrigger()
    }

    private fun getCurrentMonthRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()

        // Start of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.time

        // End of current month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.time

        return startOfMonth to endOfMonth
    }
    
    private fun listenToRefreshTrigger() {
        viewModelScope.launch {
            appContainer.dashboardRefreshTrigger.collect {
                refreshData()
            }
        }
    }
    
    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(10000) // Refresh every 10 seconds
                if (!_uiState.value.isLoading) {
                    loadDashboardData()
                }
            }
        }
    }

    private fun loadDashboardData() {
        // Cancel previous load to avoid stacking collectors
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val monthRange = getCurrentMonthRange()
                combine(
                    calculateTotalIncome(monthRange.first, monthRange.second),
                    calculateTotalExpenses(monthRange.first, monthRange.second),
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
                        filteredStartDate = monthRange.first,
                        filteredEndDate = monthRange.second,
                        isLoading = false
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun calculateTotalIncome(startDate: Date, endDate: Date): Flow<Double> = flow {
        // Get project income with date filter applied
        val projectIncome = projectRepository.getTotalProjectIncome(startDate, endDate)

        // Get event income
        val events = eventRepository.getAllEvents().first()
        val eventIncome = events
            .filter { event ->
                isWithinDateRange(event.date, startDate, endDate)
            }
            .sumOf { it.income }

        emit(projectIncome + eventIncome)
    }

    private fun calculateTotalExpenses(startDate: Date, endDate: Date): Flow<Double> = flow {
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
            projects.filter { project ->
                project.endDate == null // Active projects only (no date filtering)
            }.take(5) // Limit to 5 most recent
        }

    private fun getUpcomingEvents(): Flow<List<Event>> =
        eventRepository.getAllEvents().map { events ->
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            events.filter { event ->
                !event.date.before(today) // Today and future events (no date filtering)
            }.sortedBy { it.date }.take(5) // Next 5 events
        }

    private fun calculateTotalOwed(): Flow<Double> = flow {
        // Calculate actual unpaid amounts from WorkerRepository
        val totalOwed = workerRepository.getTotalPaymentsOwed()
        emit(totalOwed)
    }
    
    fun refreshData() {
        loadDashboardData()
    }

    private fun isWithinDateRange(date: Date, startDate: Date?, endDate: Date?): Boolean {
        if (startDate == null && endDate == null) return true
        if (startDate != null && date.before(startDate)) return false
        if (endDate != null && date.after(endDate)) return false
        return true
    }
}
