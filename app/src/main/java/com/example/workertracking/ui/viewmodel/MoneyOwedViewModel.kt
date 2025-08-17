package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import com.example.workertracking.repository.WorkerRepository
import com.example.workertracking.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MoneyOwedUiState(
    val isLoading: Boolean = true,
    val unpaidShifts: List<UnpaidShiftWorkerInfo> = emptyList(),
    val unpaidEvents: List<UnpaidEventWorkerInfo> = emptyList(),
    val totalDebt: Double = 0.0
)

class MoneyOwedViewModel(
    private val workerRepository: WorkerRepository,
    private val appContainer: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoneyOwedUiState())
    val uiState: StateFlow<MoneyOwedUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val unpaidShifts = workerRepository.getUnpaidShiftWorkers()
                val unpaidEvents = workerRepository.getUnpaidEventWorkers()
                
                val shiftTotal = unpaidShifts.sumOf { unpaidShift ->
                    if (unpaidShift.shiftWorker.isHourlyRate) {
                        unpaidShift.shiftWorker.payRate * unpaidShift.shiftHours
                    } else {
                        unpaidShift.shiftWorker.payRate
                    }
                }
                
                val eventTotal = unpaidEvents.sumOf { unpaidEvent ->
                    unpaidEvent.eventWorker.hours * unpaidEvent.eventWorker.payRate
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    unpaidShifts = unpaidShifts,
                    unpaidEvents = unpaidEvents,
                    totalDebt = shiftTotal + eventTotal
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun markShiftAsPaid(shiftWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markShiftWorkerAsPaid(shiftWorkerId)
                loadData() // Refresh the data
                appContainer.triggerDashboardRefresh() // Trigger dashboard refresh
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun markEventAsPaid(eventWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markEventWorkerAsPaid(eventWorkerId)
                loadData() // Refresh the data
                appContainer.triggerDashboardRefresh() // Trigger dashboard refresh
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    class Factory(
        private val workerRepository: WorkerRepository,
        private val appContainer: AppContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MoneyOwedViewModel::class.java)) {
                return MoneyOwedViewModel(workerRepository, appContainer) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}