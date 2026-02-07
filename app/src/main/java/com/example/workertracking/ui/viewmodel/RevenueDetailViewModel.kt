package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.repository.EventRepository
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.ShiftRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

data class MonthlyRevenue(
    val month: Int,
    val year: Int,
    val income: Double,
    val expenses: Double,
    val profit: Double
)

data class RevenueDetailUiState(
    val currentMonthRevenue: MonthlyRevenue? = null,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val monthlyData: List<MonthlyRevenue> = emptyList(),
    val isLoading: Boolean = true
)

class RevenueDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val shiftRepository: ShiftRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevenueDetailUiState())
    val uiState: StateFlow<RevenueDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadRevenueData()
    }

    fun selectMonth(month: Int, year: Int) {
        _uiState.value = _uiState.value.copy(
            selectedMonth = month,
            selectedYear = year
        )
        loadRevenueData()
    }

    private fun loadRevenueData() {
        // Cancel previous load to avoid race condition
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val (month, year) = _uiState.value.selectedMonth to _uiState.value.selectedYear
                _uiState.value = buildUiState(month, year)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun buildUiState(month: Int, year: Int): RevenueDetailUiState {
        val selectedRange = getMonthRange(month, year)
        val allEvents = eventRepository.getAllEvents().first()
        val allShifts = shiftRepository.getAllShifts().first()

        // Resolve per-item costs once per refresh.
        val eventCostById = allEvents.associate { event ->
            event.id to (eventRepository.getTotalEventCost(event.id) ?: 0.0)
        }
        val shiftCostById = allShifts.associate { shift ->
            shift.id to (shiftRepository.getTotalCostForShift(shift.id) ?: 0.0)
        }

        val selectedIncome = calculateMonthIncome(selectedRange.first, selectedRange.second, allEvents)
        val selectedExpenses = calculateMonthExpenses(
            selectedRange.first,
            selectedRange.second,
            allEvents,
            allShifts,
            eventCostById,
            shiftCostById
        )

        val monthlyData = loadAllMonthlyData(
            selectedMonth = month,
            selectedYear = year,
            allEvents = allEvents,
            allShifts = allShifts,
            eventCostById = eventCostById,
            shiftCostById = shiftCostById
        )

        return RevenueDetailUiState(
            currentMonthRevenue = MonthlyRevenue(
                month = month,
                year = year,
                income = selectedIncome,
                expenses = selectedExpenses,
                profit = selectedIncome - selectedExpenses
            ),
            selectedMonth = month,
            selectedYear = year,
            monthlyData = monthlyData,
            isLoading = false
        )
    }

    private suspend fun calculateMonthIncome(startDate: Date, endDate: Date, events: List<com.example.workertracking.data.entity.Event>): Double {
        val projectIncome = projectRepository.getTotalProjectIncome(startDate, endDate)
        val eventIncome = events
            .asSequence()
            .filter { event -> isWithinDateRange(event.date, startDate, endDate) }
            .sumOf { it.income }
        return projectIncome + eventIncome
    }

    private fun calculateMonthExpenses(
        startDate: Date,
        endDate: Date,
        events: List<com.example.workertracking.data.entity.Event>,
        shifts: List<com.example.workertracking.data.entity.Shift>,
        eventCostById: Map<Long, Double>,
        shiftCostById: Map<Long, Double>
    ): Double {
        val shiftCosts = shifts
            .asSequence()
            .filter { shift -> isWithinDateRange(shift.date, startDate, endDate) }
            .sumOf { shift -> shiftCostById[shift.id] ?: 0.0 }

        val eventCosts = events
            .asSequence()
            .filter { event -> isWithinDateRange(event.date, startDate, endDate) }
            .sumOf { event -> eventCostById[event.id] ?: 0.0 }

        return shiftCosts + eventCosts
    }

    private suspend fun loadAllMonthlyData(
        selectedMonth: Int,
        selectedYear: Int,
        allEvents: List<com.example.workertracking.data.entity.Event>,
        allShifts: List<com.example.workertracking.data.entity.Shift>,
        eventCostById: Map<Long, Double>,
        shiftCostById: Map<Long, Double>
    ): List<MonthlyRevenue> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val monthlyRevenueList = mutableListOf<MonthlyRevenue>()

        // Calculate for each month
        for (i in 11 downTo 0) {
            val monthCalendar = calendar.clone() as Calendar
            monthCalendar.add(Calendar.MONTH, -i)
            val month = monthCalendar.get(Calendar.MONTH)
            val year = monthCalendar.get(Calendar.YEAR)
            val monthRange = getMonthRange(month, year)

            val totalIncome = calculateMonthIncome(monthRange.first, monthRange.second, allEvents)
            val totalExpenses = calculateMonthExpenses(
                monthRange.first,
                monthRange.second,
                allEvents,
                allShifts,
                eventCostById,
                shiftCostById
            )

            monthlyRevenueList.add(
                MonthlyRevenue(
                    month = month,
                    year = year,
                    income = totalIncome,
                    expenses = totalExpenses,
                    profit = totalIncome - totalExpenses
                )
            )
        }

        return monthlyRevenueList
    }

    private fun getMonthRange(month: Int, year: Int): Pair<Date, Date> {
        val calendar = Calendar.getInstance()

        // Start of month
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.time

        // End of month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.time

        return startOfMonth to endOfMonth
    }

    private fun isWithinDateRange(date: Date, startDate: Date, endDate: Date): Boolean {
        return !date.before(startDate) && !date.after(endDate)
    }
}
